Requirements
---

Github projects listed pulled locally and referenced in workspace:

* [red5-server](https://github.com/Red5/red5-server)
* [red5-client](https://github.com/Red5/red5-client)
* [red5-io](https://github.com/Red5/red5-io)

Built using **Java 6 JDK**

To Run
---
In [Red5Bee.java](https://github.com/infrared5/rtmpbees/blob/master/src/main/java/com/infrared5/rtmpbee/Red5Bee.java#L28) the host, port and application are passed to the *Bee* - change them to whatever you need to test with.

Issue(s)
---
No runtime or compile-time exceptions are thrown, however, when run as **Java Application**, it goes through connection request and immediately closes.

Please refer to the [LOG](https://github.com/infrared5/rtmpbees/blob/master/LOG.txt) for further details.