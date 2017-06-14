# Stompee

![](https://github.com/phillip-kruger/stompee/blob/master/src/main/webapp/stompee/logo.png?raw=true)

> A web log viewer for Jave EE.

***

## Quick Start
Stompee allows you to view the log file online. All you need to do is include the stompee jar in your war file. 
Stompee is not meant to replace your log file, but rather assist you to easily look at certain parts of the log file online. Stompee can not handle the load of a very busy log file, so make sure to select the correct logger and level.
 
The stompee-core library is published to [maven central](http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22stompee-core%22) and artefacts is available in [Nexus OSS](https://oss.sonatype.org/#nexus-search;quick~stompee-core)

In your pom.xml:
 
     <!-- Stompee -->
     <dependency>
            <groupId>com.github.phillip-kruger</groupId>
            <artifactId>stompee-core</artifactId>
            <version>1.1.1</version>
            <scope>runtime</scope>
     </dependency>

You can then go to the stompee ui :
> http://localhost:8080/your-application-context/stompee/

## Security
Stompee does not have it's own security. You should secure the the `/stompee` context within your own security model.

## Application Servers
### Java EE 7
Stompee has been tested using the following application servers:

* [Wildfly 10.0.1](http://wildfly.org/)
* [Payara 172](http://www.payara.fish/)
* [Liberty 17.0.0.1](https://developer.ibm.com/assets/wasdev/#asset/runtimes-wlp-javaee7)
* [TomEE 7.0.3](http://tomee.apache.org/)

### Java EE 6
At this time stompee does not work on Java EE 6. I might add a backport in later.

## Screenshots
![](https://raw.githubusercontent.com/phillip-kruger/stompee/master/Screenshot1.png)
![](https://raw.githubusercontent.com/phillip-kruger/stompee/master/Screenshot2.png)


wiki: https://github.com/phillip-kruger/stompee/wiki

issues: https://github.com/phillip-kruger/stompee/issues
