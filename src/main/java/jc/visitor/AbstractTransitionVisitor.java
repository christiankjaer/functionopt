package jc.visitor;

import petter.cfg.edges.*;

import java.util.List;

abstract class AbstractTransitionVisitor {
    void visit(Assignment assignment) {
        //
    }

    void visit(GuardedTransition guard) {
        //
    }

    void visit(Nop nop) {
        //
    }

    void visit(ProcedureCall call) {
        //
    }

    void visit(List<Transition> transitions) {
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
