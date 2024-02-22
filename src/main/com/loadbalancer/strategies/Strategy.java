// Strategy.java
package com.loadbalancer.strategies;

import java.util.List;

public interface Strategy {
    String getCurrentServer(List<String> servers);

    String getNextServer(List<String> servers);
}
