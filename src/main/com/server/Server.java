package com.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;

import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

import java.lang.InterruptedException;
import java.lang.management.ManagementFactory;
import java.lang.Runtime;
import java.lang.String;
import java.lang.System;
import java.lang.Thread;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Server {
    private static final Logger logger = LogManager.getLogger(Server.class);
    private String ip;
    private int port;
    private ExecutorService executorService;

    public Server(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public void start() {
        // Set log level
        String logLevel = System.getProperty("log.level", "INFO");
        Configurator.setRootLevel(Level.valueOf(logLevel));

        logger.info("Server started on {}:{}", ip, port);
        
        // Create a thread pool with a fixed number of threads
        int nThreads = 2;
        executorService = Executors.newFixedThreadPool(nThreads);

        // Initialize Spark
        Spark.ipAddress(ip);
        Spark.port(port);
        Spark.get("/", new RequestHandler());

        // Register a shutdown hook to gracefully shutdown the server
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down server on {}:{}", ip, port);
            gracefulShutdownServer();
            logger.info("Server on {}:{} shutdown complete.", ip, port);
        }));
    }

    private void gracefulShutdownServer() {
        // Shut down the thread pool
        executorService.shutdown();
        try {
            // Wait for all threads to finish or until timeout
            if (!executorService.awaitTermination(1, TimeUnit.SECONDS)) {
                // If some tasks are still running, force shutdown
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            logger.error("Error waiting for executor service shutdown:", e);
            Thread.currentThread().interrupt(); // Reset interrupted status
        }
    }

    private class RequestHandler implements Route {
        @Override
        public Object handle(Request request, Response response) throws Exception {
            executorService.submit(() -> {
                long threadId = Thread.currentThread().threadId();
                String processId = ManagementFactory.getRuntimeMXBean().getName();

                // Process the request
                String requestBody = request.body();
                logger.info("Request received: {} (Thread: {}, Process: {})", requestBody, threadId, processId);

                // Create the response
                String responseBody = String.format("Hello from Server at %s:%d (Thread: %d, Process: %s)",
                        ip, port, threadId, processId);
                response.type("text/plain");
                response.body(responseBody);
                return responseBody;
            });

            return response;
        }
    }
}
