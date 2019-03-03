package jc;

import petter.cfg.CompilationUnit;
import petter.cfg.DotLayout;
import petter.cfg.Procedure;
import petter.cfg.edges.Transition;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class Util {
    public static Transition getTheOnly(Iterable<Transition> transitions) {
        List<Transition> list = StreamSupport.stream(transitions.spliterator(), false).collect(Collectors.toList());

        assert list.size() == 1;

        return list.get(0);
    }

    public static List<String> getParameterNames(Procedure procedure, CompilationUnit compilationUnit) {
        return procedure
                .getFormalParameters()
                .stream()
                .map(compilationUnit::getVariableName)
                .collect(Collectors.toList());
    }

    public static String randomString(Integer length) {
        Random random = new Random();

        String symbols = "1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

        StringBuilder randomString = new StringBuilder(length);

        for (int i = 0; i < length; i += 1) {
            randomString.append(symbols.charAt(random.nextInt(symbols.length())));
        }

        return randomString.toString();
    }

    public static <T1, T2> List<Tuple<T1, T2>> zip(List<T1> first, List<T2> second) {
        assert first.size() == second.size();

        List<Tuple<T1, T2>> both = new ArrayList<>();

        for (int i = 0; i < first.size(); i++) {
            both.add(new Tuple<>(first.get(i), second.get(i)));
        }

        return both;
    }

    public static void drawGraph(Procedure procedure, String name) {
        try {
            new DotLayout("png", name + ".png").callDot(procedure);
        } catch (Exception e) {
            System.err.println("Could not create dot file.");
        }
    }
}
