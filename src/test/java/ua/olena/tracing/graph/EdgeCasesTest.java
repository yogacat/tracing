package ua.olena.tracing.graph;

import jdk.jfr.Description;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Checks tricky cases such as loops or no path between services.
 *
 * @author Olena Openko
 * 17.10.2020
 */
@DisplayName("Graph Edge Cases Test")
class EdgeCasesTest {
    @Test
    @Description("Average latency between <from> and <to>")
    void testAverageLatency() {
        Graph graph = getGraph();

        String latency = graph.getAverageLatency("B-D");
        assertEquals("NO SUCH TRACE", latency);
    }

    @Test
    @Description("Number of traces with stops less than <number>")
    void testTracesNumberMaxStops() {
        Graph graph = getGraph();

        Integer stops = graph.getTracesByStops("A", "D", 1, Condition.MAX_STOPS);
        assertEquals(0, stops);

        stops = graph.getTracesByStops("A", "A", 3, Condition.MAX_STOPS);
        assertEquals(2, stops);
    }

    @Test
    @Description("Number of traces with stops exactly <number>")
    void testTracesNumberExactStops() {
        Graph graph = getGraph();

        Integer stops = graph.getTracesByStops("A", "D", 3, Condition.EXACT_STOPS);
        assertEquals(1, stops);

        stops = graph.getTracesByStops("A", "A", 3, Condition.EXACT_STOPS);
        assertEquals(1, stops);
    }

    @Test
    @Description("Shortest traces in terms of latency between <from> and <to>")
    void testShortestTraces() {
        Graph graph = getGraph();

        Integer length = graph.getShortestTrace("A", "C");
        assertEquals(3, length);

        length = graph.getShortestTrace("A", "D");
        assertEquals(10, length);

        length = graph.getShortestTrace("C", "C");
        assertEquals(6, length);
    }

    @Test
    @Description("Traces with average latency smaller than <number>")
    void testTracesShorterThan() {
        Graph graph = getGraph();

        Integer length = graph.getShortestTraces("A", "A", 15);
        assertEquals(6, length);
    }

    private static Graph getGraph() {
        Graph graph = new Graph();
        graph.addEdge("A", "B", 1);
        graph.addEdge("B", "C", 2);
        graph.addEdge("C", "A", 3);
        graph.addEdge("A", "C", 4);
        graph.addEdge("C", "D", 7);
        return graph;
    }
}
