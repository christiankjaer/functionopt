package jc.optimization;

import petter.cfg.*;
import petter.cfg.edges.Assignment;
import petter.cfg.edges.GuardedTransition;
import petter.cfg.edges.ProcedureCall;
import petter.cfg.edges.Transition;
import petter.cfg.expression.*;

import java.io.File;
import java.util.*;

public class CallGraph {

    static class Node {
        public int staticCalls;
        public Procedure procedure;
        public Set<Node> calls;

        public boolean isLeaf() {
            return calls.isEmpty();
        }
    }

    Map<Procedure, Node> nodes;

    public String toDot() {
        StringBuilder b = new StringBuilder();
        b.append("digraph CallGraph {\n");

        nodes.forEach((p, n) ->
                n.calls.forEach(target ->
                        b.append(p.getName() + " -> " + target.procedure.getName() + ";\n")));

        b.append("}\n");
        return b.toString();
    }

    private void addCallFrom(FunctionCall call, Procedure p) {
        Node n = nodes.get(p);
        nodes.forEach((proc, node) -> {
            // First-order approximation
            // TODO: Should also check arity.
            if (call.getName().equals(proc.getName())) {
                node.staticCalls++;
                n.calls.add(node);
            }
        });
    }

    private void addCallFrom(Expression e, Procedure p) {
        CallCollector cc = new CallCollector();
        Set<FunctionCall> fc = e.accept(cc, Collections.emptySet()).orElse(Collections.emptySet());
        for (FunctionCall call : fc) {
            addCallFrom(call, p);
        }
    }

    public CallGraph(CompilationUnit cu) {
        nodes = new HashMap<>();

        for (Procedure p : cu) {
            Node n = new Node();
            n.procedure = p;
            n.staticCalls = 0;
            n.calls = new HashSet<>();
            nodes.put(p, n);
        }
        for (Procedure p : cu) {
            for (Transition t : p.getTransitions()) {
                if (t instanceof GuardedTransition) {
                    GuardedTransition gt = (GuardedTransition)t;
                    addCallFrom(gt.getAssertion(), p);
                }
                if (t instanceof Assignment) {
                    Assignment a = (Assignment)t;
                    addCallFrom(a.getLhs(), p);
                    addCallFrom(a.getRhs(), p);
                }

                if (t instanceof ProcedureCall) {
                    ProcedureCall pc = (ProcedureCall)t;
                    addCallFrom(pc.getCallExpression(), p);
                }
            }
        }
    }


    public static void main(String[] args) throws Exception {
        CompilationUnit cu = petter.simplec.Compiler.parse(new File("input.c"));

        CallGraph cg = new CallGraph(cu);

        System.out.println(cg.toDot());

    }
}
