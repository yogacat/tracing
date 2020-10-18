package ua.olena.tracing.file;

import ua.olena.tracing.graph.Condition;
import ua.olena.tracing.graph.Graph;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.lang.System.out;

/**
 * Reads file, puts each line into a separate list that contains a list of nodes.
 *
 * @author Olena Openko
 * 13.10.2020
 */
public class FileProcessor {
    private Pattern tracePattern = Pattern.compile("\\w\\w[0-9]+");

    /**
     * For each line read creates a graph and calculates all the values. Prints them to System.out
     *
     * @param path Full path to the file
     */
    public boolean processFile(String path) {
        List<List<String>> lines = readLines(path);
        for (List<String> line : lines) {
            Graph graph = buildGraph(line);

            //1. The average latency of the trace A-B-C.
            out.println(graph.getAverageLatency("A-B-C"));

            //2. The average latency of the trace A-D.
            out.println(graph.getAverageLatency("A-D"));

            //3. The average latency of the trace A-D-C.
            out.println(graph.getAverageLatency("A-D-C"));

            //4. The average latency of the trace A-E-B-C-D.
            out.println(graph.getAverageLatency("A-E-B-C-D"));

            //5. The average latency of the trace A-E-D.
            out.println(graph.getAverageLatency("A-E-D"));

            //6. The number of traces originating in service C and ending in service C with a maximum of 3 hops.
            out.println(graph.getTracesByStops("C", "C", 3, Condition.MAX_STOPS));

            //7. The number of traces originating in A and ending in C with exactly 4 hops.
            out.println(graph.getTracesByStops("A", "C", 4, Condition.EXACT_STOPS));

            //8. The length of the shortest trace (in terms of latency) between A and C.
            out.println(graph.getShortestTrace("A", "C"));

            //9. The length of the shortest trace (in terms of latency) between B and B.
            out.println(graph.getShortestTrace("B", "B"));

            //10. The number of different traces from C to C with an average latency of less than 30.
            out.println(graph.getShortestTraces("C", "C", 30));
            out.println();
        }
        return true;
    }

    /**
     * Reads file, puts each line into a separate list that contains a list of nodes.
     *
     * @param path String full file path
     * @return List of lines, each line is a List of String nodes
     */
    private List<List<String>> readLines(String path) {
        if (Files.exists(Paths.get(path))) {
            try {
                List<String> lines = Files.readAllLines(Paths.get(path), StandardCharsets.UTF_8);
                return lines.stream()
                        .map(line -> List.of(line.split("\\s*,\\s*")))
                        .collect(Collectors.toList());
            } catch (IOException e) {
                throw new IllegalArgumentException("Unable to read file with path: " + path, e);
            }
        } else {
            throw new IllegalArgumentException("Wrong file path: " + path);
        }

    }

    private Graph buildGraph(List<String> line) {
        Graph graph = new Graph();
        for (String trace : line) {
            if (isValid(trace)) {
                String from = trace.substring(0, 1);
                String to = trace.substring(1, 2);
                Integer latency = Integer.parseInt(trace.substring(2));

                graph.addEdge(from, to, latency);
            } else {
                throw new IllegalArgumentException("One of the traces does not follow format of LetterLetterNumber " + trace);
            }
        }

        return graph;
    }

    private boolean isValid(String trace) {
        return tracePattern.matcher(trace).matches();
    }
}
