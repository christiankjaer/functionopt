package jc.visitor;

import petter.cfg.edges.*;

import java.util.List;

/**
 * Visits all provided transitions in the given order.
 */
abstract class AbstractTransitionVisitor {
    protected void visit(Assignment assignment) {
        //
    }

    protected void visit(GuardedTransition guard) {
        //
    }

    protected void visit(Nop nop) {
        //
    }

    protected void visit(ProcedureCall call) {
        //
    }

    protected void visit(List<Transition> transitions) {
        for (Transition transition : transitions) {
            if (transition instanceof Assignment) {
                visit((Assignment) transition);
            }

            if (transition instanceof GuardedTransition) {
                visit((GuardedTransition) transition);
            }

            if (transition instanceof Nop) {
                visit((Nop) transition);
            }

            if (transition instanceof ProcedureCall) {
                visit((ProcedureCall) transition);
            }
        }
    }
}
