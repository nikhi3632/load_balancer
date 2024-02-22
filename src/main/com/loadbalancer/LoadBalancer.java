// LoadBalancer.java
package com.loadbalancer;

import com.loadbalancer.strategies.RoundRobin;
import com.loadbalancer.strategies.Strategy;

import spark.Spark;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

public class LoadBalancer {
    private static final Logger logger = LogManager.getLogger(LoadBalancer.class);
    private String ip;
    private int port;
    private List<String> serverUrls;
    private Strategy strategy;

    public LoadBalancer(String ip, int port, List<String> serverUrls) {
        this.ip = ip;
        this.port = port;
        this.serverUrls = serverUrls;
        this.strategy = new RoundRobin();
    }

    public void start() {
        // Set log level
        String logLevel = System.getProperty("log.level", "INFO");
        Configurator.setRootLevel(Level.valueOf(logLevel));
        
        // Spark
        Spark.ipAddress(ip);
        Spark.port(port);
        logger.info("Load balancer started on {}:{}", ip, port);

        Spark.get("/", (req, res) -> {
            String nextServer = getNextServer();
            if (nextServer != null) {
                String requestUrl = nextServer + "/";
                logger.info("For request {} Redirecting request to server: {}", req, requestUrl);
                res.redirect(requestUrl);
            } else {
                logger.warn("No healthy server available to handle the request");
                res.status(503); // Service Unavailable
                return "No healthy server available";
            }
            return null;
        });

        // Register a shutdown hook to gracefully shutdown the load balancer
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down load balancer on {}:{}", ip, port);
            stopSparkServer();
            logger.info("Load balancer on {}:{} shutdown complete.", ip, port);
        }));
    }

    private void stopSparkServer() {
        Spark.stop();
    }

    private String getNextServer() {
        List<String> healthyServers = getHealthyServers();
        String nextServer = strategy.getNextServer(healthyServers);
        logger.info("Selected next server: {}", nextServer);
        return nextServer;
    }

    private List<String> getHealthyServers() {
        return serverUrls.stream()
                .filter(this::isServerHealthy)
                .collect(Collectors.toList());
    }

    private boolean isServerHealthy(String serverUrl) {
        try {
            URI uri = new URI(serverUrl + "/health");
            HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000); // 5 seconds
            connection.connect();
            int responseCode = connection.getResponseCode();
            boolean isHealthy = responseCode == 200; // Assuming 200 means the server is healthy
            logger.debug("Server {} is {}", serverUrl, isHealthy ? "healthy" : "unhealthy");
            return isHealthy;
        } catch (IOException | URISyntaxException e) {
            logger.error("Error checking server health for {}", serverUrl, e);
            return false;
        }
    }
}
