package jc.visitor;

import petter.cfg.Procedure;
import petter.cfg.edges.ProcedureCall;
import petter.cfg.edges.Transition;

import java.util.ArrayList;
import java.util.List;

public class ProcedureCallGatheringVisitor extends AbstractTransitionVisitor {

    List<Transition> callerTransitions;

    List<ProcedureCall> procedureCalls;

    public ProcedureCallGatheringVisitor(Procedure caller) {
        callerTransitions = new ArrayList<>(caller.getTransitions());

        procedureCalls = new ArrayList<>();
    }

    public List<ProcedureCall> gather() {
        visit(callerTransitions);

        return procedureCalls;
    }

    @Override
    public void visit(ProcedureCall procedureCall) {
        procedureCalls.add(procedureCall);
    }
}
