package ua.olena.tracing.graph;

/**
 * Represents connections between services with their latencies.
 *
 * @author Olena Openko
 * 16.10.2020
 */
class Edge {
    private String key;
    private int latency;

    public Edge(String key, int latency) {
        this.key = key;
        this.latency = latency;
    }

    public String getKey() {
        return key;
    }

    public int getLatency() {
        return latency;
    }
}

