package ua.olena.tracing.graph;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a node of the Graph, contains service name, neighbors of the service (not all of them, but those
 * that have a connection from the current one to them).
 *
 * @author Olena Openko
 * 16.10.2020
 */
class Vertex {
    private String name;
    private Map<String, Vertex> neighbors = new HashMap<>();

    Vertex(String name) {
        this.name = name;
    }

    Map<String, Vertex> getNeighbors() {
        return neighbors;
    }
}
