package com.server;

import java.lang.String;
import java.lang.System;

public class Main {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java Main <ipAddress> <port>");
            System.exit(1);
        }

        String ipAddress = args[0];
        int port = Integer.parseInt(args[1]);

        Server server = new Server(ipAddress, port);
        server.start();
    }
}
