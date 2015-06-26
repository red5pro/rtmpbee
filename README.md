Requirements
---

Github projects listed pulled locally and referenced in workspace:

* [red5-server](https://github.com/Red5/red5-server)
* [red5-client](https://github.com/Red5/red5-client)
* [red5-io](https://github.com/Red5/red5-io)

Built using **Java 6 JDK**

Building
---
Unfortunately, due to the dependency versions of the Red5 libraries, they are not exposed through maven and therefore building is not possible using the POM.

As such, this project should be imported into Eclipse (or similar IDE) and build exported:

1. Import `rtmpbee` in Eclipse workspace
2. Right-click on the `rtmpbee` project in the Project Explorer
3. Select __Export...__
4. Select __Java / Runnable JAR File__
5. Point to the desired output directory
6. Make sure __Package required libraries...__ is ticked ON
7. Click Finish


To Run
---
In [Red5Bee.java](https://github.com/infrared5/rtmpbee/blob/master/src/main/java/com/infrared5/rtmpbee/Red5Bee.java#L28) the host, port and application are passed to the *Bee* - change them to whatever you need to test with.

Issue(s)
---
No runtime or compile-time exceptions are thrown, however, when run as **Java Application**, it goes through connection request and immediately closes.

Please refer to the [LOG](https://github.com/infrared5/rtmpbee/blob/master/LOG.txt) for further details.