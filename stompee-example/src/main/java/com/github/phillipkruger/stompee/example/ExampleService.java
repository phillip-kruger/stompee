package com.github.phillipkruger.stompee.example;

import java.util.UUID;
import java.util.logging.Level;
import javax.annotation.PostConstruct;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import lombok.extern.java.Log;

@Log
@Singleton
@Startup
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class ExampleService {
    
    @PostConstruct
    public void init(){
        log.info("Example service started up !");
    }
    
    @Schedule(persistent=false, second = "*/10",minute="*",hour="*")
    public void createSomeLogging() {
        log.log(Level.SEVERE, "Here some random severe {0}", UUID.randomUUID());
        log.log(Level.INFO, "Here some random info {0}", UUID.randomUUID());
        log.log(Level.WARNING, "Here some random warning {0}", UUID.randomUUID());
        log.log(Level.FINE, "Here some random fine {0}", UUID.randomUUID());
        log.log(Level.FINER, "Here some random finer {0}", UUID.randomUUID());
        log.log(Level.FINEST, "Here some random finest {0}", UUID.randomUUID());
        log.log(Level.SEVERE, "And here an exception", new Exception("Something bad happened"));
    }
}