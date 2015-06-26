package com.infrared5.rtmpbee;

import java.util.Timer;
import java.util.TimerTask;

import org.red5.client.net.rtmp.INetStreamEventHandler;
import org.red5.client.net.rtmp.RTMPClient;
import org.red5.io.utils.ObjectMap;
import org.red5.server.api.event.IEvent;
import org.red5.server.api.event.IEventDispatcher;
import org.red5.server.api.service.IPendingServiceCall;
import org.red5.server.api.service.IPendingServiceCallback;
import org.red5.server.api.stream.IStreamPacket;
import org.red5.server.net.rtmp.event.Notify;

public class Bullet {
	
	private RTMPClient client;
	public String streamName;
	private String url;
	private int port;
	private String application;
	
	private Timer timer;
	private int timeout = 10; // seconds
	
	class QuitTask extends TimerTask {
		public void run() {
			System.exit(1);
			timer.cancel();
		}
	}
	
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
				
				timer = new Timer();
				timer.schedule(new QuitTask(), timeout*1000);
			}
		}
		
	};
	
	private IPendingServiceCallback connectCallback = new IPendingServiceCallback() {
		
		public void resultReceived(IPendingServiceCall call) {
			ObjectMap<?, ?> map = (ObjectMap<?, ?>) call.getResult();
			String code = (String) map.get("code");
			if ("NetConnection.Connect.Rejected".equals(code)) {
				// TODO: Notify of failure.
				client.disconnect();
			} else if ("NetConnection.Connect.Success".equals(code)) {
				client.createStream(streamCallback);
			} else {
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
	public Bullet(String url, int port, String application, String streamName) {
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
	public Bullet(String url, int port, String application, String streamName, int timeout) {
		this.url = url;
		this.port = port;
		this.application = application;
		this.streamName = streamName;
		this.timeout = timeout;
	}

	/**
	 * Fires off the RTMPClient's that connect to the stream.
	 */
	public void fire() {
		client = new RTMPClient();
		client.setServiceProvider(this);		
		client.setStreamEventDispatcher(new IEventDispatcher() {
			public void dispatchEvent(IEvent event) {
				IStreamPacket data = (IStreamPacket) event;
//				System.out.println("dispatchEvent: " + event);
			}			
		});
		client.setStreamEventHandler(new INetStreamEventHandler() {
			public void onStreamEvent(Notify arg0) {
//				System.out.print("onStreamEvent: " + arg0);
			}
		});
		
    	client.connect(url, port, client.makeDefaultConnectionParams(url, port, application), connectCallback);
	}
	
}
