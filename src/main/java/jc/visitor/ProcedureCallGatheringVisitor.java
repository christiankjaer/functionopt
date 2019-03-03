package jc.visitor;

import petter.cfg.Procedure;
import petter.cfg.edges.ProcedureCall;
import petter.cfg.edges.Transition;

import java.util.ArrayList;
import java.util.List;

public class ProcedureCallGatheringVisitor extends AbstractTransitionVisitor {

    Procedure callee;

    List<Transition> callerTransitions;

    List<ProcedureCall> procedureCalls;

    public ProcedureCallGatheringVisitor(Procedure caller, Procedure callee) {
        this.callee = callee;

        callerTransitions = new ArrayList<>(caller.getTransitions());

        procedureCalls = new ArrayList<>();
    }

    public List<ProcedureCall> gather() {
        return procedureCalls;
    }

    @Override
    public void visit(ProcedureCall procedureCall) {
        if (procedureCall.getCallExpression().getName().equals(callee.getName())) {
            procedureCalls.add(procedureCall);
        }
    }
}
