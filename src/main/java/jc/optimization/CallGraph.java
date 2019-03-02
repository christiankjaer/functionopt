package jc.optimization;

import petter.cfg.*;
import petter.cfg.edges.Assignment;
import petter.cfg.edges.GuardedTransition;
import petter.cfg.edges.ProcedureCall;
import petter.cfg.edges.Transition;
import petter.cfg.expression.*;
import petter.simplec.Compiler;

import java.io.File;
import java.util.*;

public class CallGraph {

    static class Node {
        private Procedure procedure;

        // Nodes that call the procedure of this node.
        private Set<Node> callers;

        // Nodes whose procedure is called from this node.
        private Set<Node> callees;

        public Procedure getProcedure() {
            return procedure;
        }

        public Set<Node> getCallers() {
            return callers;
        }

        public Set<Node> getCallees() {
            return callees;
        }

        public boolean isLeaf() {
            return callees.isEmpty();
        }
    }

    private Map<Procedure, Node> nodes;

    public CallGraph(CompilationUnit compilationUnit) {
        nodes = new HashMap<>();
        createNodes(compilationUnit);
        connectNodes(compilationUnit);
    }

    private void createNodes(CompilationUnit compilationUnit) {
        for (Procedure procedure : compilationUnit) {
            Node node = new Node();

            node.procedure = procedure;
            node.callers = new HashSet<>();
            node.callees = new HashSet<>();

            nodes.put(procedure, node);
        }
    }

    private void connectNodes(CompilationUnit compilationUnit) {
        for (Procedure procedure : compilationUnit) {
            for (Transition transition : procedure.getTransitions()) {
                if (transition instanceof GuardedTransition) {
                    addCallFrom(procedure, ((GuardedTransition) transition).getAssertion());
                }

                if (transition instanceof Assignment) {
                    Assignment assignment = (Assignment) transition;

                    addCallFrom(procedure, assignment.getLhs());
                    addCallFrom(procedure, assignment.getRhs());
                }

                if (transition instanceof ProcedureCall) {
                    addCallFrom(procedure, ((ProcedureCall) transition).getCallExpression());
                }
            }
        }
    }

    private void addCallFrom(Procedure procedure, FunctionCall call) {
        Node callerNode = nodes.get(procedure);
        Node calleeNode = findProcedureNode(call.getName());

        assert calleeNode != null;

        callerNode.callees.add(calleeNode);
        calleeNode.callers.add(callerNode);
    }

    private void addCallFrom(Procedure procedure, Expression expression) {
        CallCollector collector = new CallCollector();

        Set<FunctionCall> calls = expression.accept(collector, Collections.emptySet()).orElse(Collections.emptySet());

        for (FunctionCall call : calls) {
            addCallFrom(procedure, call);
        }
    }

    private Node findProcedureNode(String procedureName) {
        for (Map.Entry<Procedure, Node> entry : nodes.entrySet()) {
            if (entry.getKey().getName().equals(procedureName)) {
                return entry.getValue();
            }
        }

        // should be unreachable
        return null;
    }

    public Node getNode(Procedure procedure) {
        return nodes.get(procedure);
    }

    public String toDot() {
        StringBuilder dot = new StringBuilder("digraph CallGraph {\n");

        for (Node caller : nodes.values()) {
            for (Node callee : caller.callees) {
                dot.append(caller.procedure.getName());
                dot.append(" -> ");
                dot.append(callee.procedure.getName());
                dot.append(";\n");
            }
        }

        dot.append("}\n");

        return dot.toString();
    }

    public static void main(String[] args) throws Exception {
        CompilationUnit compilationUnit = Compiler.parse(new File("examples/recursion.c"));

        CallGraph callGraph = new CallGraph(compilationUnit);

        System.out.println(callGraph.toDot());
    }
}
