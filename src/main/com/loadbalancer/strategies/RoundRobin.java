// RoundRobin.java
package com.loadbalancer.strategies;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobin implements Strategy {
    private AtomicInteger currentIndex = new AtomicInteger(-1); // Start from -1 to start from 0 index on first call

    @Override
    public String getCurrentServer(List<String> servers) {
        if (servers.isEmpty()) {
            throw new IllegalStateException("No servers available");
        }
        int index = currentIndex.get();
        return servers.get(index < 0 ? 0 : index % servers.size());
    }

    @Override
    public String getNextServer(List<String> servers) {
        if (servers.isEmpty()) {
            throw new IllegalStateException("No servers available");
        }
        int index = currentIndex.incrementAndGet();
        return servers.get(index % servers.size());
    }
}
