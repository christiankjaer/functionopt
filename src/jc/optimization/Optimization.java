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

    CompilationUnit cu;
    CallGraph cg;

    public Optimization(CompilationUnit cu) {
        this.cu = cu;
        this.cg = new CallGraph(cu);
    }

    public void inlineFunction(FunctionCall fc, Procedure p) {
        System.out.println("inlining function call " + fc.toString());
    }

    public void inlineProcedure(ProcedureCall pc, Procedure p) {
        System.out.println("inlining procedure call " + pc.toString());
    }

    public void inline() {

        for (Procedure p : cu) {

            Set<Procedure> inlineCandidates = cg.nodes.get(p).calls.stream()
                    .filter(CallGraph.Node::isLeaf).map(n -> n.procedure).collect(Collectors.toSet());

            for (Transition t : p.getTransitions()) {
                if (t instanceof Assignment) {
                    Assignment a = (Assignment) t;

                    if (a.getRhs() instanceof FunctionCall) {
                        FunctionCall fc = ((FunctionCall)a.getRhs());
                        for (Procedure cand : inlineCandidates) {
                            if (cand.getName().equals(fc.getName())) {
                                inlineFunction(fc, cand);
                                break;
                            }
                        }
                    }

                }
                if (t instanceof ProcedureCall) {
                    FunctionCall fc = ((ProcedureCall) t).getCallExpression();

                    for (Procedure cand : inlineCandidates) {
                        if (cand.getName().equals(fc.getName())) {
                            inlineProcedure((ProcedureCall) t, cand);
                            break;
                        }
                    }
                }
            }
        }

    }


    public static void main(String[] args) throws Exception {
        CompilationUnit cu = petter.simplec.Compiler.parse(new File("input.c"));

        Optimization optimization = new Optimization(cu);

        optimization.inline();

    }

}
