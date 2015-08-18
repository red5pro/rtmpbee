package com.infrared5.rtmpbee;

import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.lang3.StringUtils;
import org.red5.client.net.rtmp.ClientExceptionHandler;
import org.red5.client.net.rtmp.INetStreamEventHandler;
import org.red5.client.net.rtmp.RTMPClient;
import org.red5.io.utils.ObjectMap;
import org.red5.server.api.event.IEvent;
import org.red5.server.api.event.IEventDispatcher;
import org.red5.server.api.service.IPendingServiceCall;
import org.red5.server.api.service.IPendingServiceCallback;
import org.red5.server.api.stream.IStreamPacket;
import org.red5.server.net.rtmp.event.Notify;
import org.red5.server.net.rtmp.status.StatusCodes;

public class Bullet {
	
	private int order;
	private RTMPClient client;
	public String streamName;
	private String url;
	private int port;
	private String application;
	
	private Thread thread;
	private Timer timer;
	private int timeout = 10; // seconds
	
	private IBulletCompleteHandler completeHandler;
	public boolean hasCompleted = false;
	volatile boolean connectionException = false;
	
	public void onBWCheck(Object params) {
		System.out.println("onBWCheck: " + params);
	}
	
	/**
	 * Called when bandwidth has been configured.
	 */
	public void onBWDone(Object params) {
		System.out.println("onBWDone: " + params);
	}
	
	public void onStatus(Object params) {
		System.out.println("onStatus: " + params);
	}
	
	private IPendingServiceCallback streamCallback = new IPendingServiceCallback() {
		
		public void resultReceived(IPendingServiceCall call) {
			if (call.getServiceMethodName().equals("createStream")) {
				Integer streamId = (Integer) call.getResult();
				
				// -2: live then recorded, -1: live, >=0: recorded
				// streamId, streamName, mode, length
				client.play(streamId, streamName, -2, 0);
				
				final String description = "(bullet #" + order + ")";
				final TimerTask timerHandler = new TimerTask() {
					
					@Override
					public void run() {
						System.out.println("Successful subscription from bullet. Will end. " + description);
						hasCompleted = true;
						dispose();
						timer.cancel();
						thread.interrupt();
						if(completeHandler != null) {
							completeHandler.OnBulletComplete();
						}
					}
				};
				
				timer = new Timer();
				timer.schedule(timerHandler, timeout*1000);
			}
		}
		
	};
	
	private IPendingServiceCallback connectCallback = new IPendingServiceCallback() {
		
		public void resultReceived(IPendingServiceCall call) {
			ObjectMap<?, ?> map = (ObjectMap<?, ?>) call.getResult();
			String code = (String) map.get("code");
			// Server connection established, but issue in connection.
			if (StatusCodes.NC_CONNECT_FAILED.equals(code) ||
					StatusCodes.NC_CONNECT_REJECTED.equals(code) ||
					StatusCodes.NC_CONNECT_INVALID_APPLICATION.equals(code)) {
				dispose();
				if(completeHandler != null) {
					completeHandler.OnBulletComplete();
				}
			} 
			// If connection successful, establish a stream
			else if (StatusCodes.NC_CONNECT_SUCCESS.equals(code)) {
				client.createStream(streamCallback);
			} 
			else {
				// TODO: Notify of failure.
				System.out.print("ERROR code:" + code);
			}
		}
	};

	/**
	 * Constructs a bullet which represents an RTMPClient.
	 * 
	 * @param url
	 * @param port
	 * @param application
	 * @param streamName
	 */
	public Bullet(int order, String url, int port, String application, String streamName) {
		this.order = order;
		this.url = url;
		this.port = port;
		this.application = application;
		this.streamName = streamName;
	}
	
	/**
	 * Constructs a bullet which represents an RTMPClient.
	 * 
	 * @param url
	 * @param port
	 * @param application
	 * @param streamName
	 * @param timeout
	 */
	public Bullet(int order, String url, int port, String application, String streamName, int timeout) {
		this.order = order;
		this.url = url;
		this.port = port;
		this.application = application;
		this.streamName = streamName;
		this.timeout = timeout;
	}

	public void dispose() {

		if(client != null) {
			client.setServiceProvider(null);
			client.setExceptionHandler(null);
			client.setStreamEventDispatcher(null);
			client.setStreamEventHandler(null);
			client.disconnect();
			client = null;
		}
		
	}
	
	public String toString() {
		return StringUtils.join(new String[]{
				"(bullet #" + this.order + ")",
				"URL: " + this.url,
                "PORT: " + this.port,
                "APP: " + this.application,
                "NAME: " + this.streamName}, "\n");
	}
	
	/**
	 * Fires off the RTMPClient's that connect to the stream.
	 */
	public Thread fire(IBulletCompleteHandler completeHandler, IBulletFailureHandler failHandler) {
		
		this.completeHandler = completeHandler;
		
		final String description = this.toString();
		final IBulletFailureHandler failureHandler = failHandler;
		
		System.out.println("<<fire>> : " + description);
		
		hasCompleted = false;
		connectionException = false;
		
		client = new RTMPClient();
		client.setServiceProvider(this);	
		client.setExceptionHandler(new ClientExceptionHandler() {
			@Override
			public void handleException(Throwable throwable) {
				connectionException = true;
				dispose();
			}
		});
		client.setStreamEventDispatcher(new IEventDispatcher() {
			@Override
			public void dispatchEvent(IEvent event) {
				IStreamPacket data = (IStreamPacket) event;
				System.out.println("dispatchEvent: " + event);
			}			
		});
		client.setStreamEventHandler(new INetStreamEventHandler() {
			@Override
			public void onStreamEvent(Notify notification) {
				System.out.println("<<event>>: " + description);
				System.out.println(notification.toString());
			}
		});
		
		this.thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				client.connect(url, port, client.makeDefaultConnectionParams(url, port, application), connectCallback);
				while(!hasCompleted && !Thread.currentThread().isInterrupted()) {
					if(connectionException && failureHandler != null) {
						System.out.println("Failure in Bullet: " + description);
						hasCompleted = true;
						failureHandler.OnBulletFireFail();
					}
				}
			}
			
		}, "Bullet " + order);
		return this.thread;
	}
	
}
