package com.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;

import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

import java.lang.management.ManagementFactory;
import java.lang.Runtime;
import java.lang.String;
import java.lang.System;
import java.lang.Thread;

public class Server {
    private static final Logger logger = LogManager.getLogger(Server.class);
    private String ip;
    private int port;

    public Server(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public void start() {
        // Set log level
        String logLevel = System.getProperty("log.level", "INFO");
        Configurator.setRootLevel(Level.valueOf(logLevel));

        logger.info("Server started on {}:{}", ip, port);
        
        // Spark
        Spark.ipAddress(ip);
        Spark.port(port);
        Spark.threadPool(8);
        Spark.init();
        Spark.get("/", new RequestHandler());

        // Register a shutdown hook to gracefully shutdown the server
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down server on {}:{}", ip, port);
            stopSparkServer();
            logger.info("Server on {}:{} shutdown complete.", ip, port);
        }));
    }

    private void stopSparkServer() {
        Spark.stop();
    }

    private class RequestHandler implements Route {
        @Override
        public String handle(Request request, Response response) throws Exception {
            long threadId = Thread.currentThread().threadId();
            String processId = ManagementFactory.getRuntimeMXBean().getName();
    
            String clientIpAddress = request.ip();
            int clientPort = request.port();
    
            String requestBody = request.body();
            logger.info("Request received from {}:{} on {}:{}: {} (Thread: {}, Process: {})",
                    clientIpAddress, clientPort, ip, port, requestBody, threadId, processId);
            
            String responseBody = String.format("Hello from Server at %s:%d (Thread: %d, Process: %s)",
                    ip, port, threadId, processId);
            response.type("text/plain");
            response.header("Content-Length", String.valueOf(responseBody.getBytes().length));
            return responseBody;
        }
    }
}
