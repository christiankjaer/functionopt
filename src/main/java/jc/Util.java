package jc;

import petter.cfg.CompilationUnit;
import petter.cfg.DotLayout;
import petter.cfg.Procedure;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Simple utility functions.
 */
public class Util {
    /**
     * Ensures there is only one element in the provided iterable and returns it.
     */
    public static <T> T getTheOnly(Iterable<T> iterable) {
        List<T> list = StreamSupport.stream(iterable.spliterator(), false).collect(Collectors.toList());

        assert list.size() == 1;

        return list.get(0);
    }

    /**
     * Returns parameter names of the given procedure.
     */
    public static List<String> getParameterNames(Procedure procedure, CompilationUnit compilationUnit) {
        return procedure
                .getFormalParameters()
                .stream()
                .map(compilationUnit::getVariableName)
                .collect(Collectors.toList());
    }

    /**
     * Generates a random alphanumeric string of the given length.
     */
    public static String randomString(Integer length) {
        Random random = new Random();

        String symbols = "1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

        StringBuilder randomString = new StringBuilder(length);

        for (int i = 0; i < length; i += 1) {
            randomString.append(symbols.charAt(random.nextInt(symbols.length())));
        }

        return randomString.toString();
    }

    /**
     * Combines two lists to a single list of tuples.
     */
    public static <T1, T2> List<Tuple<T1, T2>> zip(List<T1> first, List<T2> second) {
        assert first.size() == second.size();

        List<Tuple<T1, T2>> both = new ArrayList<>();

        for (int i = 0; i < first.size(); i++) {
            both.add(new Tuple<>(first.get(i), second.get(i)));
        }

        return both;
    }

    /**
     * Draws a CFG of the provided procedure to a file using dot.
     */
    public static void drawCFG(Procedure procedure, String name) {
        try {
            new DotLayout("png", name + ".png").callDot(procedure);
        } catch (Exception e) {
            System.err.println("Could not create CFG dot file.");
        }
    }

    /**
     * Draws the provided call graph to a file using dot.
     */
    public static void drawCallGraph(CallGraph callGraph, String name) {
        try {
            new DotLayout("png", name + ".png").callDot(callGraph);
        } catch (Exception e) {
            System.err.println("Could not create CG dot file.");
        }
    }
}
