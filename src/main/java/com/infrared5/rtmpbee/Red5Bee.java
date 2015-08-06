package com.infrared5.rtmpbee;

import java.io.Console;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class Red5Bee {
	
	Map<Integer, Bullet> machineGun = new HashMap<Integer, Bullet>();
	
	public static Bullet bullet;
	private static Red5Bee bee;	
	
	private String url;
	private int port;
	private String application;
	private String streamName;
	private int numBullets;
	private int timeout = 10; // in seconds
	
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
		
		System.out.printf("Number of arguments: %d.\n", args.length);
		
		// 3 option for client specific attack.
		if(args.length < 2) {
			System.out.printf("Incorrect number of args, please pass in the following: \n "
					+ "\narg[0] = RTMP URL"
					+ "\narg[1] = numBullets");
			return;
		}
		else if(args.length >= 2 && args.length <= 3) {
			
			System.out.printf("Determined its an invaluable attack...");
			
			URI uri;
			String protocol;
			String path;
			String[] paths;
			try {
				System.out.printf("Found URL: %s.\n", args[0]);
				uri = new URI(args[0]);
				protocol = uri.getScheme();
				System.out.printf("Protocol: %s.\n", protocol);
				url = uri.getHost();
				System.out.printf("Host: %s.\n", url);
				port = uri.getPort();
				System.out.printf("Port: %s.\n", port);
				path = uri.getPath();
				System.out.printf("Path: %s.\n", path);
				paths = path.split("/");
				if(paths.length < 3) {
					System.out.printf("Could not properly parse provided endpoint for RTMP: " + args[0] + ".");
					return;
				}
				System.out.printf("protocol: " + protocol + ", url: " + url + ", port: " + port + ", paths: " + paths[1] + ", " + paths[2] + ".\n");
				application = paths[1];
				streamName = paths[2];
			}
			catch(Exception e) {
				System.out.printf("Could not properly parse provided endpoint for RTMP: " + args[0] + ".");
				e.printStackTrace();
				return;
			}
			
			numBullets = Integer.parseInt(args[1]);
			if(args.length > 2) {
				timeout = Integer.parseInt(args[2]);
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
			
		}
		
		bee = new Red5Bee(url, port, application, streamName, numBullets, timeout);
		bee.loadMachineGun();
    	bee.fireMachineGun();
    	
    }
	
    public Red5Bee(String url, int port, String application, String streamName, int numBullets, int timeout) {
		this.url = url;
		this.port = port;
		this.application = application;
		this.streamName = streamName;
		this.numBullets = numBullets;
		this.timeout = timeout;
	}

    @SuppressWarnings("rawtypes")
	private void fireMachineGun() {
		
		Iterator<Entry<Integer, Bullet>> it = machineGun.entrySet().iterator();
	    while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry)it.next();
	        System.out.println(pairs.getKey() + " = " + pairs.getValue());
	        it.remove(); // avoids a ConcurrentModificationException
	        
	        Bullet bullet = (Bullet) pairs.getValue();
	        bullet.fire();
	    }
	}

	/**
	 * 
	 */
	private void loadMachineGun() {
		
		// load our bullets into the gun
		for(int i=0; i<numBullets; i++) {
			machineGun.put(i, new Bullet(url, port, application, streamName, timeout));
		}
	}
	
}
