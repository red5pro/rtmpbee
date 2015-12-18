package com.infrared5.rtmpbee;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Red5Bee implements IBulletCompleteHandler, IBulletFailureHandler {

    // instance a scheduled executor, using a core size based on cpus
    private static ScheduledExecutorService executor = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors() * 8);

    private String url;

    private int port;

    private String application;

    private String streamName;

    private int numBullets;

    private int timeout = 10; // in seconds

    private AtomicInteger bulletsRemaining = new AtomicInteger();

    Map<Integer, Bullet> machineGun = new HashMap<Integer, Bullet>();

    /**
     * Original Bee - provide all parts of stream endpoint for attack.
     * 
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
    }

    /**
     * Submits a Runnable task to the executor.
     * 
     * @param runnable
     * @return
     */
    public static Future<?> submit(Runnable runnable) {
        return executor.submit(runnable);
    }

    /**
     * Submits a Runnable task to the executor with a scheduled delay.
     * 
     * @param runnable
     * @param delay
     * @param unit
     * @return
     */
    public static ScheduledFuture<?> submit(Runnable runnable, long delay, TimeUnit unit) {
        return executor.schedule(runnable, delay, unit);
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
    private void fireMachineGun() {
        for (Entry<Integer, Bullet> entry : machineGun.entrySet()) {
            System.out.printf("Submitting %d for execution\n", entry.getKey().intValue());
            // submit for execution
            submit(entry.getValue());
            bulletsRemaining.incrementAndGet();
        }
        System.out.printf("Active thread count at fireMachineGun: %d bullets: %d\n", Thread.activeCount(), bulletsRemaining.get());
    }

    /**
     * Puts bullets into machine gun (iterable).
     */
    private void loadMachineGun() {
        // load our bullets into the gun
        for (int i = 0; i < numBullets; i++) {
            // build a bullet
            Bullet bullet = Bullet.Builder.build((i + 1), url, port, application, streamName, timeout);
            bullet.setCompleteHandler(this);
            bullet.setFailHandler(this);
            machineGun.put(i, bullet);
        }
    }

    @Override
    public void OnBulletComplete() {
        int remaining = bulletsRemaining.decrementAndGet();
        if (remaining <= 0) {
            System.out.println("All bullets expended. Bye Bye.");
            System.exit(1);
        }
        System.out.println("Bullet has completed journey. Remaining Count: " + bulletsRemaining);
        System.out.printf("Active thread count: %d bullets remaining: %d\n", Thread.activeCount(), bulletsRemaining.get());
    }

    @Override
    public void OnBulletFireFail() {
        System.out.println("Failure for bullet to fire. Possible missing endpoint. Accessing a new endpoint from stream manager.");
    }

    /**
     * Entry point.
     * 
     * @param args
     * @throws InterruptedException
     */
    public static void main(String[] args) throws InterruptedException {
        System.out.printf("Number of arguments: %d.\n", args.length);
        // app args
        String url;
        int port;
        String application;
        String streamName;
        int numBullets;
        int timeout = 10;

        Red5Bee bee;

        // 3 option for client specific attack.
        if (args.length < 2) {
        	
            System.out.printf("Incorrect number of args, please pass in the following: \n " + "\narg[0] = Stream Manager Endpoint to access Stream Subscription URL" + "\narg[1] = numBullets");
            return;
            
        }
        // 5 option arguments for original attack.
        else if (args.length < 5) {
        	
            System.out.printf("Incorrect number of args, please pass in the following: \n  " + "\narg[0] = IP Address" + "\narg[1] = port" + "\narg[2] = app" + "\narg[3] = streamName" + "\narg[4] = numBullets");
            return;
            
        } else {
        	
            System.out.println("Determined its an original attack...");
            url = args[0];
            port = Integer.parseInt(args[1]);
            application = args[2];
            streamName = args[3];
            numBullets = Integer.parseInt(args[4]);
            if (args.length > 5) {
                timeout = Integer.parseInt(args[5]);
            }
            // create the bee
            bee = new Red5Bee(url, port, application, streamName, numBullets, timeout);
            bee.attack();
            
        }
        // put the main thread in limbo while the bees fly!
        Thread.currentThread().join();
        // shutdown the executor
        executor.shutdown();
        // wait up-to 10 seconds for tasks to complete
        executor.awaitTermination(10, TimeUnit.SECONDS);
        System.out.println("Main - exit");
    }

}
