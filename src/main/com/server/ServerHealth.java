// ServerHealth.java
package com.server;

public class ServerHealth {
    private boolean isHealthy;

    public ServerHealth() {
        this.isHealthy = true; // Initially assume the server is healthy
    }

    public boolean isHealthy() {
        return isHealthy;
    }

    public void markAsUnhealthy() {
        isHealthy = false;
    }

    public void markAsHealthy() {
        isHealthy = true;
    }
}
