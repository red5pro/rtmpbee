Requirements
===

Github projects listed pulled locally and referenced in workspace:

* [red5-client](https://github.com/Red5/red5-client)
* [red5-io](https://github.com/Red5/red5-io)
* [red5-server-common](https://github.com/Red5/red5-server-common)

Built using **Java 7 JDK**

Building
===

##Eclipse

To create the executable jar in Eclipse, use export (~86Mb):

1. Import `rtmpbee` in Eclipse workspace
2. Right-click on the `rtmpbee` project in the Project Explorer
3. Select __Export...__
4. Select __Java / Runnable JAR File__
5. Point to the desired output directory
6. Make sure __Package required libraries...__ is ticked ON
7. Click Finish

##Maven

Creates an executable jar with all the needed dependencies (~17Mb)

```sh
mvn clean compile assembly:single
```

To Run
===

```sh
$ java -jar rtmpbee.jar [red5pro-server-IP] [port] [app-name] [stream-name] [count] [timeout]
```

Options
---

### red5pro-server-IP
The IP of the Red5 Pro Server that you want the bee to subscribe to (attack)

### port
The port on the Red5 Pro Server that you want the bee to subscribe to (attack)

### app-name
The application name that provides the streaming capabilities

### stream-name
The name of the stream you want the bee to subscribe to (attack)

### count
The amount of bullets (stingers, a.k.a. stream connections) for the bee to have in the attack

### timeout
The amount of time to subscribe to stream. _The actual subscription time may differ from this amount. This is really the time lapse of start of subscription until end._

Example
---
```
java -jar rtmpbee.jar 104.196.37.72 1935 live G2UQDUxfZD9PpPJaDA 100 10
```

Issue(s)
===
No runtime or compile-time exceptions are thrown, however, when run as **Java Application**, it goes through connection request and immediately closes.

Please refer to the [LOG](https://github.com/infrared5/rtmpbee/blob/master/LOG.txt) for further details.
[![Analytics](https://ga-beacon.appspot.com/UA-59819838-3/red5pro/rtmpbee?pixel)](https://github.com/igrigorik/ga-beacon)