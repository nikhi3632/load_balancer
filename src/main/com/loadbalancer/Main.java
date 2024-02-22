// Main.java
package com.loadbalancer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Usage: java Main <lb_ip> <lb_port> <server_urls_file>");
            System.exit(1);
        }

        String lbIpAddress = args[0];
        int lbPort = Integer.parseInt(args[1]);
        String serverUrlsFile = args[2];

        List<String> serverUrls;
        try {
            serverUrls = Files.readAllLines(Paths.get(serverUrlsFile));
        } catch (IOException e) {
            System.err.println("Error reading server URLs file: " + e.getMessage());
            return;
        }

        LoadBalancer loadBalancer = new LoadBalancer(lbIpAddress, lbPort, serverUrls);
        loadBalancer.start();
    }
}