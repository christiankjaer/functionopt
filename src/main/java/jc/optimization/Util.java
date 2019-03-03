package jc.optimization;

import petter.cfg.CompilationUnit;
import petter.cfg.DotLayout;
import petter.cfg.Procedure;
import petter.cfg.State;
import petter.cfg.edges.Transition;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class Util {
    public static Tuple<State, State> findBeginEnd(List<Transition> transitions) {
        List<State> begins = transitions
                .stream()
                .map(Transition::getSource)
                .filter(state -> state.getInDegree() == 0)
                .collect(Collectors.toList());

        List<State> ends = transitions
                .stream()
                .map(Transition::getDest)
                .filter(state -> state.getOutDegree() == 0)
                .collect(Collectors.toList());

        assert !begins.isEmpty();
        assert !ends.isEmpty();

        return new Tuple<>(begins.get(0), ends.get(0));
    }

    public static Transition getIncoming(State state) {
        List<Transition> transition = StreamSupport
                .stream(state.getIn().spliterator(), false)
                .collect(Collectors.toList());

        assert transition.size() == 1;

        return transition.get(0);
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
