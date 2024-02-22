// Server.java
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
    private ServerHealth serverHealth;

    public Server(String ip, int port) {
        this.ip = ip;
        this.port = port;
        this.serverHealth = new ServerHealth(); // Initialize server health
    }

    public void start() {
        // Set log level
        String logLevel = System.getProperty("log.level", "INFO");
        Configurator.setRootLevel(Level.valueOf(logLevel));

        logger.info("Server started on {}:{}", ip, port);

        // Start the background health check
        startHealthCheck();

        // Spark
        Spark.ipAddress(ip);
        Spark.port(port);

        Spark.threadPool(8);
        Spark.init();

        Spark.get("/", new RequestHandler());
        Spark.get("/health", new HealthCheckHandler());
        Spark.get("/togglehealth", new ToggleHealthHandler());

        // Register a shutdown hook to gracefully shutdown the server
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down server on {}:{}", ip, port);
            stopSparkServer();
            logger.info("Server on {}:{} shutdown complete.", ip, port);
        }));
    }

    private void startHealthCheck() {
        new Thread(() -> {
            while (true) {
                try {
                    boolean previousHealth = serverHealth.isHealthy(); // Initialize with current health status
                    Thread.sleep(5000); // Check health every 5 seconds
                    boolean currentHealth = serverHealth.isHealthy();
                    if (currentHealth != previousHealth) {
                        logServerHealth(currentHealth);
                        previousHealth = currentHealth; // Update previous health status
                    }
                    // logServerHealth(currentHealth);
                } catch (InterruptedException e) {
                    logger.error("Health check thread interrupted.", e);
                    Thread.currentThread().interrupt();
                }
            }
        }).start();
    }

    private void logServerHealth(boolean isHealthy) {
        String status = isHealthy ? "healthy" : "unhealthy";
        logger.info("Health check: Server at {}:{} is {}", ip, port, status);
    }

    private void stopSparkServer() {
        Spark.stop();
    }

    private class RequestHandler implements Route {
        @Override
        public String handle(Request request, Response response) throws Exception {
            long threadId = Thread.currentThread().threadId();
            String processId = ManagementFactory.getRuntimeMXBean().getName();

            String requestIpAddress = request.ip();
            int requestPort = request.port();

            String requestBody = request.body();
            logger.info("Request received from {}:{} on {}:{}: {} (Thread: {}, Process: {})",
                requestIpAddress, requestPort, ip, port, requestBody, threadId, processId);
            
            String responseBody = String.format("Hello from Server at %s:%d (Thread: %d, Process: %s)",
                    ip, port, threadId, processId);
            response.type("text/plain");
            response.header("Content-Length", String.valueOf(responseBody.getBytes().length));

            return responseBody;
        }
    }

    private class HealthCheckHandler implements Route {
        @Override
        public String handle(Request request, Response response) throws Exception {
            if (serverHealth.isHealthy()) {
                return "Server is healthy";
            } else {
                response.status(503); // Service Unavailable
                return "Service is unavailable";
            }
        }
    }

    private class ToggleHealthHandler implements Route {
        @Override
        public String handle(Request request, Response response) throws Exception {
            // Toggle health status
            boolean currentHealth = serverHealth.isHealthy();
            if (currentHealth) {
                serverHealth.markAsUnhealthy();
                logger.info("Health toggled: Server at {}:{} marked as unhealthy", ip, port);
            } else {
                serverHealth.markAsHealthy();
                logger.info("Health toggled: Server at {}:{} marked as healthy", ip, port);
            }
            // Return updated health status
            String status = serverHealth.isHealthy() ? "healthy" : "unhealthy";
            return "Server at " + ip + ":" + port + " is now " + status;
        }
    }    
}
