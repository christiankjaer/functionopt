package jc.optimization;

import petter.cfg.*;
import petter.cfg.edges.Assignment;
import petter.cfg.edges.ProcedureCall;
import petter.cfg.edges.Transition;
import petter.cfg.expression.*;
import petter.cfg.expression.visitors.DefaultUpDownDFS;

import java.io.File;
import java.util.*;
import java.util.stream.Stream;

// Collects function calls from an expression.
class CallCollector extends DefaultUpDownDFS<Set<FunctionCall>> {
    @Override
    public Set<FunctionCall> postVisit(FunctionCall m, Set<FunctionCall> s, Stream<Set<FunctionCall>> it) {
        Set<FunctionCall> result = new HashSet<>();
        result.add(m);
        result.addAll(s);
        it.forEach(result::addAll);
        return result;
    }

    @Override
    public Set<FunctionCall> postVisit(BinaryExpression s, Set<FunctionCall> lhs, Set<FunctionCall> rhs) {
        Set<FunctionCall> result = new HashSet<>();
        result.addAll(lhs);
        result.addAll(rhs);
        return result;
    }
}

public class CallGraph {

    static class Node {
        public int staticCalls;
        public Procedure procedure;
        public Set<Node> calls;
    }

    Map<Procedure, Node> nodes;

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
            Node n = nodes.get(p);
            for (Transition t : p.getTransitions()) {
                if (t instanceof Assignment) {
                    Assignment a = (Assignment)t;

                    CallCollector cc = new CallCollector();
                    Set<FunctionCall> fc = a.getRhs().accept(cc, Collections.emptySet()).orElse(Collections.emptySet());

                    for (FunctionCall call : fc) {
                        nodes.forEach((proc, node) -> {
                            // First-order approximation
                            // TODO: Should also check arity.
                            if (call.getName().equals(proc.getName())) {
                                node.staticCalls++;
                                n.calls.add(node);
                            }
                        });
                    }
                }

                if (t instanceof ProcedureCall) {
                    ProcedureCall pc = (ProcedureCall)t;
                    nodes.forEach((proc, node) -> {
                        // First-order approximation
                        // TODO: Should also check arity.
                        if (pc.getCallExpression().getName().equals(proc.getName())) {
                            node.staticCalls++;
                            n.calls.add(node);
                        }
                    });
                }

            }
        }
    }


    public static void main(String[] args) throws Exception {
        CompilationUnit cu = petter.simplec.Compiler.parse(new File("input.c"));

        CallGraph cg = new CallGraph(cu);

        cg.nodes.forEach((p, n) -> {
            System.out.print(p.getName() + " called " + n.staticCalls + " time(s) and calls \n\t");
            n.calls.forEach(nb -> {
                System.out.print(nb.procedure.getName() + ", ");
            });
            System.out.println();
        });

    }
}
