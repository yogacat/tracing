package ua.olena.tracing.graph;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;

/**
 * Builds graph with Vertexes that are service names, Edges that represent connections between two services.
 * Calculates average latency, paths between two Vertexes etc.
 *
 * @author Olena Openko
 * 13.10.2020
 */
public class Graph {
    private static final String ERROR_MSG = "NO SUCH TRACE";
    private Map<String, Vertex> vertexes = new HashMap<>();
    private Map<String, Edge> edges = new HashMap<>();
    //1h is just to optimize, normally belongs to a configuration
    private final CacheBuilder<Object, Object> cacheBuilder = CacheBuilder.newBuilder().expireAfterAccess(Duration.ofHours(1));
    private Cache<String, Integer> avgLatencyCache = cacheBuilder.build();

    public void addEdge(String from, String to, Integer latency) {
        Vertex vertexFrom = vertexes.getOrDefault(from, new Vertex(from));
        Vertex vertexTo = vertexes.getOrDefault(to, new Vertex(to));
        vertexFrom.getNeighbors().put(to, vertexTo);
        vertexes.putIfAbsent(from, vertexFrom);
        vertexes.putIfAbsent(to, vertexTo);

        Edge edge = edges.getOrDefault(from + to, new Edge(from + to));
        edge.getLatencies().add(latency);
        edges.putIfAbsent(from + to, edge);

        resetCache(edge.getKey());
    }

    /**
     * Calculates average latency for the path like A-B-C for the provided graph.
     *
     * @param vertexes String path
     * @return number if there was such path or "NO SUCH TRACE" if there was none
     */
    public String getAverageLatency(String vertexes) {
        int idx = 0;
        int latency = 0;

        while (idx + 2 < vertexes.length()) {
            String from = vertexes.substring(idx, idx + 1);
            String to = vertexes.substring(idx + 2, idx + 3);

            Edge edge = edges.get(from + to);
            if (edge != null) {
                int average = getAverageFromCacheOrCalculate(edge);
                latency = latency + average;
            } else {
                return ERROR_MSG;
            }

            idx = idx + 2;
        }

        return String.valueOf(latency);
    }

    /**
     * Calculates how many traces there are between two services with maximum/exact number of stops.
     *
     * @param from Node to start from
     * @param to Node to finish on
     * @param stops Number of stops like 2 For A-B-C
     * @param condition Maximum or Exact
     * @return number of traces
     */
    public Integer getTracesByStops(String from, String to, Integer stops, Condition condition) {
        int traceNumber = 0;
        int depth = 0;

        Stack<String> visited = new Stack<>();
        Stack<String> stack = new Stack<>();
        Stack<Integer> depthStack = new Stack<>();
        stack.push(from);
        depthStack.push(depth);
        while (!stack.isEmpty()) {
            String node = stack.pop();
            depth = depthStack.pop() + 1;
            visited.add(node);
            Vertex vertex = vertexes.get(node);
            Map<String, Vertex> neighbors = vertex.getNeighbors();

            if (depth != stops + 1) {
                pushNeighborsToStack(depth, stack, depthStack, neighbors);
            } else {
                goBack(visited, depthStack);
            }

            if (isReachedDestination(node, to, depth, stops, condition))
                traceNumber++;
        }

        return traceNumber;
    }

    /**
     * Get shortest trace between two nodes for provided graph.
     *
     * @param from Node to start from
     * @param to Node to finish at
     * @return Minimal latency of the path between two nodes
     */
    public Integer getShortestTrace(String from, String to) {
        int minLatency = 0;
        int depth = 0;

        Stack<String> visited = new Stack<>();
        Stack<String> stack = new Stack<>();
        Stack<Integer> depthStack = new Stack<>();
        stack.push(from);
        depthStack.push(depth);
        while (!stack.isEmpty()) {
            String node = stack.pop();
            depth = depthStack.pop() + 1;
            visited.add(node);
            Vertex vertex = vertexes.get(node);
            Map<String, Vertex> neighbors = vertex.getNeighbors();

            //If I'm going from C to C and I'm in C but this is starting point and not destination
            if (!node.equals(to) || visited.size() == 1) {
                for (String neighbor : neighbors.keySet()) {
                    if (!visited.contains(neighbor) || (visited.contains(neighbor) && neighbor.equals(to))) {
                        stack.push(neighbor);
                        depthStack.push(depth);
                    }
                }
            }
            //If I reached the node that has no path to destination
            if (neighbors.isEmpty() && !node.equals(to)) {
                goBack(visited, depthStack);
            }
            //I reached destination
            else if (node.equals(to) && visited.size() > 1) {
                int averageLatency = getAverage(visited);
                if (minLatency == 0 || minLatency > averageLatency)
                    minLatency = averageLatency;

                goBack(visited, depthStack);
            }
        }

        return minLatency;
    }

    /**
     * Get all traces that are shorter than provided number between two services.
     *
     * @param from Node to start from
     * @param to Destination node
     * @param maxLatency maximal latency allowed
     * @return Number of traces that are shorter than provided number
     */
    public Integer getShortestTraces(String from, String to, Integer maxLatency) {
        int shortestTraces = 0;
        int depth = 0;

        Stack<String> visited = new Stack<>();
        Stack<String> stack = new Stack<>();
        Stack<Integer> depthStack = new Stack<>();
        stack.push(from);
        depthStack.push(depth);
        while (!stack.isEmpty()) {
            String node = stack.pop();
            depth = depthStack.pop() + 1;
            visited.add(node);
            Vertex vertex = vertexes.get(node);
            Map<String, Vertex> neighbors = vertex.getNeighbors();

            int averageLatency = getAverage(visited);
            if (averageLatency > maxLatency) {
                goBack(visited, depthStack);
            } else {
                pushNeighborsToStack(depth, stack, depthStack, neighbors);

                //If I reached the node that has no path to destination
                if (neighbors.isEmpty() && !node.equals(to)) {
                    goBack(visited, depthStack);
                }
                //I reached destination
                else if (node.equals(to) && visited.size() > 1) {
                    if (averageLatency < maxLatency)
                        shortestTraces++;

                    goBack(visited, depthStack);
                }
            }
        }

        return shortestTraces;
    }

    private void goBack(Stack<String> visited, Stack<Integer> depthStack) {
        if (!depthStack.isEmpty()) {
            int deltaDepth = depthStack.peek();
            int backDepth = visited.size() - deltaDepth;
            for (int i = 0; i < backDepth; i++)
                visited.pop();
        }
    }

    private int getAverage(Stack<String> visited) {
        int average = 0;
        Queue<String> queue = new ArrayDeque<>(visited);
        String from = queue.poll();
        while (!queue.isEmpty()) {
            String to = queue.poll();
            Edge edge = edges.get(from + to);
            int avg = getAverageFromCacheOrCalculate(edge);

            average = average + avg;

            from = to;
        }

        return average;
    }

    private int calculateAverage(List<Integer> latencies) {
        return (int) latencies.stream().mapToDouble(Integer::doubleValue).average().orElse(Double.NaN);
    }

    private void pushNeighborsToStack(int depth, Stack<String> stack, Stack<Integer> depthStack, Map<String, Vertex> neighbors) {
        for (String neighbor : neighbors.keySet()) {
            stack.push(neighbor);
            depthStack.push(depth);
        }
    }

    private boolean isReachedDestination(String node, String to, int depth, Integer stops, Condition condition) {
        if (condition.equals(Condition.MAX_STOPS) &&
                node.equals(to) && 1 < depth && depth <= stops + 1)
            return true;
        else return condition.equals(Condition.EXACT_STOPS) &&
                node.equals(to) && depth == stops + 1;
    }

    private int getAverageFromCacheOrCalculate(Edge edge) {
        if (edge.getLatencies().isEmpty()) {
            throw new IllegalStateException("Mistake in the algorithm, please check the code");
        }
        Integer avg = avgLatencyCache.getIfPresent(edge.getKey());
        if (avg == null) {
            List<Integer> latencies = edge.getLatencies();
            avg = calculateAverage(latencies);
            avgLatencyCache.put(edge.getKey(), avg);
        }
        return avg;
    }

    private void resetCache(String edgeName) {
        avgLatencyCache.invalidate(edgeName);
    }
}
