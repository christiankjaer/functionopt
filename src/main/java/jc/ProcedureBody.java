package jc;

import petter.cfg.State;
import petter.cfg.edges.Transition;

import java.util.List;

/**
 * Represents the body (states, transitions, expressions) of a procedure.
 */
public class ProcedureBody {

    private List<Transition> transitions;

    private State begin;

    private State end;

    public ProcedureBody(List<Transition> transitions, State begin, State end) {
        this.transitions = transitions;

        this.begin = begin;

        this.end = end;
    }

    public List<Transition> getTransitions() {
        return transitions;
    }

    public State getBegin() {
        return begin;
    }

    public State getEnd() {
        return end;
    }
}
