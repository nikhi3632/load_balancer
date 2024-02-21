// Server.java
package com.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

public class Server {
    private static final Logger logger = LogManager.getLogger(Server.class);

    public static void main(String[] args) {
        String logLevel = System.getProperty("log.level", "INFO");
        Configurator.setRootLevel(org.apache.logging.log4j.Level.valueOf(logLevel));
        logger.info("Server started.");
        // Implement server logic...
    }
}
