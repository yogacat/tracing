package ua.olena.tracing.graph;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents connections between services with their latencies.
 *
 * @author Olena Openko
 * 16.10.2020
 */
class Edge {
    private String key;
    private List<Integer> latencies = new ArrayList<>();

    Edge(String key) {
        this.key = key;
    }

    String getKey() {
        return key;
    }

    List<Integer> getLatencies() {
        return latencies;
    }
}
