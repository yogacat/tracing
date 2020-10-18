package ua.olena.tracing.graph;

import jdk.jfr.Description;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Checks each method for a graph where path between services does not exist.
 *
 * @author Olena Openko
 * 17.10.2020
 */
@DisplayName("No Such Trace Test")
class NoSuchTraceTest {
    @Test
    @Description("Average latency between <from> and <to>")
    void testAverageLatency() {
        Graph graph = getGraph();

        String latency = graph.getAverageLatency("B-C");
        assertEquals("NO SUCH TRACE", latency);
    }

    @Test
    @Description("Number of traces with stops less than <number>")
    void testTracesNumberMaxStops() {
        Graph graph = getGraph();

        Integer stops = graph.getTracesByStops("B", "C", 3, Condition.MAX_STOPS);
        assertEquals(0, stops);
    }

    @Test
    @Description("Number of traces with stops exactly <number>")
    void testTracesNumberExactStops() {
        Graph graph = getGraph();

        Integer stops = graph.getTracesByStops("B", "C", 3, Condition.EXACT_STOPS);
        assertEquals(0, stops);
    }

    @Test
    @Description("Shortest traces in terms of latency between <from> and <to>")
    void testShortestTraces() {
        Graph graph = getGraph();

        Integer length = graph.getShortestTrace("B", "C");
        assertEquals(0, length);
    }

    @Test
    @Description("Traces with average latency smaller than <number>")
    void testTracesShorterThan() {
        Graph graph = getGraph();

        Integer length = graph.getShortestTraces("B", "C", 15);
        assertEquals(0, length);
    }

    private static Graph getGraph() {
        Graph graph = new Graph();
        graph.addEdge("A", "B", 1);
        graph.addEdge("A", "C", 2);
        return graph;
    }
}
