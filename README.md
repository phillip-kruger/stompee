# Stompee

![](https://github.com/phillip-kruger/stompee/blob/master/src/main/webapp/stompee/logo.png?raw=true)

> A web log viewer for Jave EE.

***

## Quick Start
Stompee allows you to view the log file online. All you need to do is include the stompee jar in your war file. 

In your pom.xml:
 
     <!-- Stompee -->
     <dependency>
            <groupId>com.github.phillip-kruger</groupId>
            <artifactId>stompee-core</artifactId>
            <version>1.0</version>
            <scope>runtime</scope>
     </dependency>

You can then go to the stompee ui :
> http://localhost:8080/your-application-context/stompee/

See more here: https://github.com/phillip-kruger/stompee/wiki
