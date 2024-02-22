// Server.java
package com.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.InterruptedException;
import java.lang.management.ManagementFactory;
import java.lang.Runtime;
import java.lang.String;
import java.lang.System;
import java.lang.Thread;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Server {
    private static final Logger logger = LogManager.getLogger(Server.class);
    private String ip;
    private int port;
    private static ExecutorService executorService;

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

        // Register a shutdown hook to gracefully shutdown the server
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down server on {}:{}", ip, port);
            gracefulShutdownServer();
            logger.info("Server on {}:{} shutdown complete.", ip, port);
        }));

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                logger.info("Client connected: {}:{}",
                        clientSocket.getInetAddress().getHostAddress(),
                        clientSocket.getPort());

                // Submit the task of handling the client request to the thread pool
                executorService.submit(() -> handleClientRequest(clientSocket));
            }
        } catch (IOException e) {
            logger.error("Error starting server:", e);
        }
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

    private void handleClientRequest(Socket clientSocket) {
        long threadId = Thread.currentThread().threadId();
        String processId = ManagementFactory.getRuntimeMXBean().getName();
        
        try (InputStream inputStream = clientSocket.getInputStream();
            OutputStream outputStream = clientSocket.getOutputStream()) {

            byte[] buffer = new byte[1024];
            int bytesRead = inputStream.read(buffer);

            String request = new String(buffer, 0, bytesRead);
            logger.info("Request received from {}:{}: {} (Thread: {}, Process: {})",
                    clientSocket.getInetAddress().getHostAddress(), clientSocket.getPort(), request, threadId, processId);
            
            String responseBody = String.format("Hello from Server at %s:%d (Thread: %d, Process: %s)",
                ip, port, threadId, processId);
            String response = "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: text/plain\r\n" +
                    "Content-Length: " + responseBody.length() + "\r\n" +
                    "\r\n" +
                    responseBody;
            outputStream.write(response.getBytes());
            outputStream.flush(); // Flush response immediately
            outputStream.close();

        } catch (IOException e) {
            logger.error("Error handling client request:", e);
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                logger.error("Error closing client socket:", e);
            }
        }
    }
}
