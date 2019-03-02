package jc.optimization;

import petter.cfg.CompilationUnit;
import petter.cfg.Procedure;
import petter.cfg.edges.Assignment;
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
                                inlineFunction(call);
                                break;
                            }
                        }
                    }
                }

                if (transition instanceof ProcedureCall) {
                    ProcedureCall call = (ProcedureCall) transition;

                    for (Procedure candidate : calledLeafProcedures) {
                        if (candidate.getName().equals(call.getCallExpression().getName())) {
                            inlineProcedure(call);
                            break;
                        }
                    }
                }
            }
        }
    }

    public void inlineFunction(FunctionCall call) {
        System.out.println("Inlining function call: " + call.toString());
    }

    public void inlineProcedure(ProcedureCall call) {
        System.out.println("Inlining procedure call: " + call.toString());
    }

    public static void main(String[] args) throws Exception {
        CompilationUnit compilationUnit = petter.simplec.Compiler.parse(new File("examples/procedure.c"));

        Optimization optimization = new Optimization(compilationUnit);

        optimization.inline();
    }
}
