package com.infrared5.rtmpbee;

import java.io.Console;
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
    	
    	if(args.length < 5) {
    		Console console = System.console();
    		console.printf("Incorrect number of args, please pass in the following: \n  "
    				+ "\narg[0] = IP Address"
    				+ "\narg[1] = port"
    				+ "\narg[2] = app"
    				+ "\narg[3] = streamName"
    				+ "\narg[4] = numBullets");
    				
    		return;
    	}
    	
    	// we can setup our object
    	bee = new Red5Bee(args);
    	bee.loadMachineGun();
    	bee.fireMachineGun();
    }
	
    public Red5Bee(String[] args) {
		url = args[0];
		port = Integer.parseInt(args[1]);
		application = args[2];
		streamName = args[3];
		numBullets = Integer.parseInt(args[4]);
		if(args.length > 5) {
			timeout = Integer.parseInt(args[5]);
		}
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
