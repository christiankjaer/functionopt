package jc.optimization;

import petter.cfg.CompilationUnit;
import petter.cfg.DotLayout;
import petter.cfg.Procedure;
import petter.cfg.State;
import petter.cfg.edges.Assignment;
import petter.cfg.edges.Nop;
import petter.cfg.edges.ProcedureCall;
import petter.cfg.edges.Transition;
import petter.cfg.expression.FunctionCall;
import petter.cfg.expression.visitors.Substitution;

import java.io.File;
import java.util.Set;
import java.util.stream.Collectors;

public class Optimization {

    CompilationUnit compilationUnit;

    CallGraph callGraph;

    public Optimization(CompilationUnit compilationUnit) {
        this.compilationUnit = compilationUnit;

        this.callGraph = new CallGraph(compilationUnit);
    }

    public void inline() {
        for (Procedure procedure : compilationUnit) {
            Set<Procedure> calledLeafProcedures = callGraph
                    .getNode(procedure)
                    .getCallees()
                    .stream()
                    .filter(CallGraph.Node::isLeaf)
                    .map(CallGraph.Node::getProcedure)
                    .collect(Collectors.toSet());

            for (Transition transition : procedure.getTransitions()) {
                if (transition instanceof Assignment) {
                    Assignment assignment = (Assignment) transition;

                    if (assignment.getRhs() instanceof FunctionCall) {
                        FunctionCall call = ((FunctionCall) assignment.getRhs());

                        for (Procedure candidate : calledLeafProcedures) {
                            if (candidate.getName().equals(call.getName())) {
                                inlineAssignment(assignment, call, procedure, candidate);
                                break;
                            }
                        }
                    }
                }

                if (transition instanceof ProcedureCall) {
                    ProcedureCall call = (ProcedureCall) transition;

                    for (Procedure candidate : calledLeafProcedures) {
                        if (candidate.getName().equals(call.getCallExpression().getName())) {
                            inlineProcedure(call, procedure, candidate);
                            break;
                        }
                    }
                }
            }
        }
    }

    private void inlineAssignment(Assignment assignment, FunctionCall call, Procedure caller, Procedure callee) {
        drawGraph(caller);

        State callBegin = assignment.getSource();
        State callEnd = assignment.getDest();

        State calleeEnter = callee.getBegin();
        State calleeExit = callee.getEnd();

        // todo: mimic procedure inlining
        // todo: rename callee's local variables in caller
        // todo: use renamed callee result instead of the original expression

        drawGraph(caller);
    }

    private void inlineProcedure(ProcedureCall call, Procedure caller, Procedure callee) {
        drawGraph(caller);

        State callBegin = call.getSource();
        State callEnd = call.getDest();

        State calleeEnter = callee.getBegin();
        State calleeExit = callee.getEnd();

        // todo: rename callee's local variables in caller

        // replace call with jumps
        call.removeEdge();
        new Nop(callBegin, calleeEnter);
        new Nop(calleeExit, callEnd);

        caller.refreshStates();

        drawGraph(caller);
    }

    private static void drawGraph(Procedure procedure) {
        try {
            new DotLayout("png", "after.png").callDot(procedure);
        } catch (Exception e) {
            System.err.println("Could not create dot file.");
        }
    }

    public static void main(String[] args) throws Exception {
        CompilationUnit compilationUnit = petter.simplec.Compiler.parse(new File("examples/procedure.c"));

        Optimization optimization = new Optimization(compilationUnit);

        optimization.inline();
    }
}
