package ua.olena.tracing.graph;

import jdk.jfr.Description;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests checking that average latency, number of traces between two endpoints, number of traces with minimal latency
 * is calculated correctly.
 *
 * @author Olena Openko
 * 13.10.2020
 */
@DisplayName("Graph Test")
class GraphTest {

    @Test
    @Description("Average latency between <from> and <to>")
    void testAverageLatency() {
        Graph graph = new Graph();
        graph.addEdge("A", "B", 5);
        graph.addEdge("B", "C", 4);
        graph.addEdge("C", "D", 8);
        graph.addEdge("D", "C", 8);
        graph.addEdge("D", "E", 6);
        graph.addEdge("A", "D", 5);
        graph.addEdge("C", "E", 2);
        graph.addEdge("E", "B", 3);
        graph.addEdge("A", "E", 7);

        String latency = graph.getAverageLatency("A-B-C");
        assertEquals("9", latency);

        latency = graph.getAverageLatency("A-D");
        assertEquals("5", latency);

        latency = graph.getAverageLatency("A-D-C");
        assertEquals("13", latency);

        latency = graph.getAverageLatency("A-E-B-C-D");
        assertEquals("22", latency);

        latency = graph.getAverageLatency("A-E-D");
        assertEquals("NO SUCH TRACE", latency);

        graph = new Graph();
        graph.addEdge("A", "B", 5);
        graph.addEdge("A", "B", 6);
        graph.addEdge("A", "B", 7);
        graph.addEdge("A", "B", 8);
        graph.addEdge("A", "B", 4);
        graph.addEdge("A", "D", 5);
        graph.addEdge("B", "C", 4);
        latency = graph.getAverageLatency("A-B-C");
        assertEquals("10", latency);

        graph.addEdge("B", "C", 5);
        graph.addEdge("B", "C", 6);
        latency = graph.getAverageLatency("A-B-C");
        assertEquals("11", latency);
    }

    @Test
    @Description("Number of traces with stops less than <number>")
    void testTracesNumberMaxStops() {
        Graph graph = new Graph();
        graph.addEdge("A", "B", 5);
        graph.addEdge("B", "C", 4);
        graph.addEdge("C", "D", 8);
        graph.addEdge("D", "C", 8);
        graph.addEdge("D", "E", 6);
        graph.addEdge("A", "D", 5);
        graph.addEdge("C", "E", 2);
        graph.addEdge("E", "B", 3);
        graph.addEdge("A", "E", 7);
        Integer stops = graph.getTracesByStops("C", "C", 3, Condition.MAX_STOPS);
        assertEquals(2, stops);
    }

    @Test
    @Description("Number of traces with stops exactly <number>")
    void testTracesNumberExactStops() {
        Graph graph = new Graph();
        graph.addEdge("A", "B", 5);
        graph.addEdge("B", "C", 4);
        graph.addEdge("C", "D", 8);
        graph.addEdge("D", "C", 8);
        graph.addEdge("D", "E", 6);
        graph.addEdge("A", "D", 5);
        graph.addEdge("C", "E", 2);
        graph.addEdge("E", "B", 3);
        graph.addEdge("A", "E", 7);

        Integer stops = graph.getTracesByStops("A", "C", 4, Condition.EXACT_STOPS);
        assertEquals(3, stops);
    }

    @Test
    @Description("Shortest traces in terms of latency between <from> and <to>")
    void testShortestTraces() {
        Graph graph = new Graph();
        graph.addEdge("A", "B", 5);
        graph.addEdge("B", "C", 4);
        graph.addEdge("C", "D", 8);
        graph.addEdge("D", "C", 8);
        graph.addEdge("D", "E", 6);
        graph.addEdge("A", "D", 5);
        graph.addEdge("C", "E", 2);
        graph.addEdge("E", "B", 3);
        graph.addEdge("A", "E", 7);
        Integer length = graph.getShortestTrace("A", "C");
        assertEquals(9, length);

        length = graph.getShortestTrace("B", "B");
        assertEquals(9, length);

        graph = new Graph();
        graph.addEdge("B", "C", 1);
        graph.addEdge("C", "A", 2);
        graph.addEdge("A", "D", 3);
        graph.addEdge("D", "C", 4);
        graph.addEdge("C", "B", 5);
        length = graph.getShortestTrace("B", "B");
        assertEquals(6, length);
    }

    @Test
    @Description("Traces with average latency smaller than <number>")
    void testTracesShorterThan() {
        Graph graph = new Graph();
        graph.addEdge("A", "B", 5);
        graph.addEdge("B", "C", 4);
        graph.addEdge("C", "D", 8);
        graph.addEdge("D", "C", 8);
        graph.addEdge("D", "E", 6);
        graph.addEdge("A", "D", 5);
        graph.addEdge("C", "E", 2);
        graph.addEdge("E", "B", 3);
        graph.addEdge("A", "E", 7);
        Integer length = graph.getShortestTraces("C", "C", 30);
        assertEquals(7, length);
    }
}