package com.infrared5.rtmpbee;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Red5Bee implements IBulletCompleteHandler, IBulletFailureHandler {
	
	private String url;
	private int port;
	private String application;
	private String streamName;
	private int numBullets;
	private int timeout = 10; // in seconds
	private String streamManagerURL; // optional
	
	private int bulletsRemaining;
	
	Map<Integer, Bullet> machineGun = new HashMap<Integer, Bullet>();
	
	/**
	 * Entry point.
	 * 
	 * @param args
	 */
	public static void main( String[] args ) {
    	
		String url;
		int port;
		String application;
		String streamName;
		int numBullets;
		int timeout = 10;
		
		Red5Bee bee;
		
		System.out.printf("Number of arguments: %d.\n", args.length);
		
		// 3 option for client specific attack.
		if(args.length < 2) {
			System.out.printf("Incorrect number of args, please pass in the following: \n "
					+ "\narg[0] = Stream Manager Endpoint to access Stream Subscription URL"
					+ "\narg[1] = numBullets");
			return;
		}
		else if(args.length >= 2 && args.length <= 3) {
			
			System.out.printf("Determined its an invaluable attack...");
			url = args[0].toString().trim();
			numBullets = Integer.parseInt(args[1]);
			if(args.length > 2) {
				timeout = Integer.parseInt(args[2]);
			}
			
			try {
				bee = new Red5Bee(url, numBullets, timeout);
				bee.attack();
			}
			catch(Exception e) {
				System.out.printf("Could not properly parse provided endpoint from Stream Manager: %s.\n", args[0]);
				e.printStackTrace();
			}
		}
		// 5 option arguments for origin attack.
		else if(args.length < 5) {
			
			System.out.printf("Incorrect number of args, please pass in the following: \n  "
    				+ "\narg[0] = IP Address"
    				+ "\narg[1] = port"
    				+ "\narg[2] = app"
    				+ "\narg[3] = streamName"
    				+ "\narg[4] = numBullets");
    				
    		return;
    		
    	}
		else {
			
			System.out.printf("Determined its an original attack...");
			url = args[0];
			port = Integer.parseInt(args[1]);
			application = args[2];
			streamName = args[3];
			numBullets = Integer.parseInt(args[4]);
			if(args.length > 5) {
				timeout = Integer.parseInt(args[5]);
			}
			new Red5Bee(url, port, application, streamName, numBullets, timeout).attack();
			
		}
    	
    }
	
	/**
	 * Original Bee - provide all parts of stream endpoint for attack.
	 * @param url
	 * @param port
	 * @param application
	 * @param streamName
	 * @param numBullets
	 * @param timeout
	 */
    public Red5Bee(String url, int port, String application, String streamName, int numBullets, int timeout) {
    	
		this.url = url;
		this.port = port;
		this.application = application;
		this.streamName = streamName;
		this.numBullets = numBullets;
		this.timeout = timeout;
		this.streamManagerURL = null;
		
	}
    
    /**
     * Invaluable Bee - provide Stream Manager endpoint for GET or stream uri.
     * @param streamManagerURL
     * @param numBullets
     * @param timeout
     */
    public Red5Bee(String streamManagerURL, int numBullets, int timeout) throws Exception {
    	
    	this.streamManagerURL = streamManagerURL;
		this.numBullets = numBullets;
		this.timeout = timeout;
			
		modifyEndpointProperties(this.streamManagerURL);
		
    }
    
    /**
     * Updates property state based on data received from Stream Manager Endpoint request.
     * @param smURL
     * @throws Exception
     */
    public void modifyEndpointProperties(String smURL) throws Exception {
    	
    	URI uri;
		String protocol;
		String path;
		String[] paths;
		
    	System.out.printf("Access Streaming Endpoint from Stream Manager URL: %s.\n", streamManagerURL);
		String endpoint = accessStreamEndpoint(smURL).toString().trim();
		System.out.printf("Received Streaming Endpoint: %s.\n", endpoint);
		uri = new URI(endpoint);
		protocol = uri.getScheme();
		
		this.url = uri.getHost();
		this.port = uri.getPort();
		path = uri.getPath();
		paths = path.split("/");
		if(paths.length < 3) {
			throw new IllegalArgumentException("Could not properly parse provided endpoint for RTMP: " + endpoint + ".");
		}
		System.out.printf("protocol: " + protocol + ", url: " + url + ", port: " + port + ", paths: " + paths[1] + ", " + paths[2] + ".\n");
		this.application = paths[1];
		this.streamName = paths[2];
		
    }
    
    /**
     * Loads up and fires.
     */
    public void attack() {
    	
    	this.loadMachineGun();
    	this.fireMachineGun();
    	
    }
    
    /**
     * Fires bullets from machine gun (iterable).
     */
    @SuppressWarnings("rawtypes")
	private void fireMachineGun() {
		
    	bulletsRemaining = machineGun.size();
    	ExecutorService executor = Executors.newFixedThreadPool(machineGun.size());
		Iterator<Entry<Integer, Bullet>> it = machineGun.entrySet().iterator();
	    while (it.hasNext()) {
	    	
			Map.Entry pairs = (Map.Entry)it.next();
	        it.remove(); // avoids a ConcurrentModificationException
	        
	        Bullet bullet = (Bullet) pairs.getValue();
	        executor.execute(bullet.fire(this, this));
	        
	    }
	    executor.shutdown();
	    
	}
    
	/**
	 * Puts bullets into machine gun (iterable).
	 */
	private void loadMachineGun() {
		// load our bullets into the gun
		for(int i = 0; i< numBullets; i++) {
			machineGun.put(i, new Bullet((i+1), url, port, application, streamName, timeout));
		}
	}
	
	@Override
	public void OnBulletComplete() {
		bulletsRemaining = bulletsRemaining - 1;
		if(bulletsRemaining <= 0) {
			System.out.println("All bullets expended. Bye Bye.");
			System.exit(1);
		}
		System.out.println("Bullet has completed journey. Remaining Count: " + bulletsRemaining);
	}
	
	@Override
	public void OnBulletFireFail() {
		
		System.out.println("Failure for bullet to fire. Possible missing endpoint. Accessing a new endpoint from stream manager.");
		try {
			
			modifyEndpointProperties(this.streamManagerURL);
			
			Thread t1 = new Bullet(++numBullets, url, port, application, streamName, timeout).fire(this, this);
			t1.start();
			t1.join();
			
		}
		catch(Exception e) {
			System.out.printf("Could not refire bullet with Stream Manager Endpoint URL: %s\n.", this.streamManagerURL);
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Attempts to access stream endpoint uri from Stream Manager URL.
	 * @param desiredUrl
	 * @return
	 * @throws Exception
	 */
	private String accessStreamEndpoint(String desiredUrl) throws Exception {
	    URL url = null;
	    BufferedReader reader = null;
	    StringBuilder stringBuilder;
	 
	    try {
	      url = new URL(desiredUrl);
	      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	      connection.setRequestMethod("GET");
	      connection.setReadTimeout(15*1000);
	      connection.connect();
	 
	      // read the output from the server
	      reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
	      stringBuilder = new StringBuilder();
	      String line = null;
	      while ((line = reader.readLine()) != null) {
	    	  stringBuilder.append(line + "\n");
	      }
	      return stringBuilder.toString();
	    }
	    catch (Exception e) {
	    	e.printStackTrace();
	    	throw e;
	    }
	    finally {
	    	if (reader != null) {
	    		try {
	    			reader.close();
	    		}
	    		catch (IOException ioe) {
	    			ioe.printStackTrace();
	    		}
	    	}
	    }
	}
}
