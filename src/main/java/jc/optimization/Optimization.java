package jc.optimization;

import petter.cfg.*;
import petter.cfg.edges.*;
import petter.cfg.expression.FunctionCall;

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
        drawGraph(caller, "caller");
        drawGraph(callee, "callee");

        State callBegin = assignment.getSource();
        State callEnd = assignment.getDest();

        State calleeEnter = callee.getBegin();
        State calleeExit = callee.getEnd();

        // todo: mimic procedure inlining
        // todo: rename callee's local variables in caller
        // todo: use renamed callee result instead of the original expression

        drawGraph(caller, "caller_inlined");
    }

    private void inlineProcedure(ProcedureCall call, Procedure caller, Procedure callee) {
        drawGraph(caller, "caller");
        drawGraph(callee, "callee");

        State callBegin = call.getSource();
        State callEnd = call.getDest();

        State calleeEnter = callee.getBegin();
        State calleeExit = callee.getEnd();

        StringGenerator generator = new StringGenerator();
        String prefix = "__in_" + generator.generate(6) + "_";

        // todo: copy arguments

        RenamingVisitor visitor = new RenamingVisitor(prefix);
        calleeEnter.forwardAccept(visitor, true);
        visitor.fullAnalysis();

        call.removeEdge();
        new Nop(callBegin, calleeEnter);
        new Nop(calleeExit, callEnd);

        caller.refreshStates();

        drawGraph(caller, "caller_inlined");
    }

    private static void drawGraph(Procedure procedure, String name) {
        try {
            new DotLayout("png", name + ".png").callDot(procedure);
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
