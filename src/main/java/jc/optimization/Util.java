package jc.optimization;

import petter.cfg.DotLayout;
import petter.cfg.Procedure;
import petter.cfg.State;
import petter.cfg.edges.Transition;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

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

    public static String randomString(Integer length) {
        Random random = new Random();

        String symbols = "1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

        StringBuilder randomString = new StringBuilder(length);

        for (int i = 0; i < length; i += 1) {
            randomString.append(symbols.charAt(random.nextInt(symbols.length())));
        }

        return randomString.toString();
    }

    public static void drawGraph(Procedure procedure, String name) {
        try {
            new DotLayout("png", name + ".png").callDot(procedure);
        } catch (Exception e) {
            System.err.println("Could not create dot file.");
        }
    }
}
