package ua.olena.tracing;

import ua.olena.tracing.file.FileProcessor;

import static java.lang.System.out;

/**
 * Main class processing requests.
 *
 * @author Olena Openko
 * 13.10.2020
 */
public class Main {
    public static void main(String[] args) {
        if (args.length != 1) {
            out.println("Wrong filename, please enter filename and repeat");
        } else {
            try {
                String path = args[0];
                FileProcessor processor = new FileProcessor();
                processor.processFile(path);
            } catch (Exception e) {
                out.println(e.getMessage());
            }
        }
    }
}
