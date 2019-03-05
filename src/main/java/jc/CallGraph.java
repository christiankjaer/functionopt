package jc;

import jc.visitor.FunctionCallGatheringVisitor;
import jc.visitor.ProcedureCallGatheringVisitor;
import petter.cfg.CompilationUnit;
import petter.cfg.Procedure;
import petter.cfg.edges.Assignment;
import petter.cfg.edges.ProcedureCall;
import petter.cfg.expression.FunctionCall;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Call graph of a single compilation unit.
 * The connections between nodes are store in nodes themselves.
 */
public class CallGraph {

    /**
     * Maps the procedure name to its corresponding graph node.
     */
    private Map<String, Node> proceduresNodes;

    public CallGraph(CompilationUnit compilationUnit) {
        proceduresNodes = new HashMap<>();

        Map<String, Procedure> namesProcedures = compilationUnit.getProcedures();

        for (Procedure caller : namesProcedures.values()) {
            proceduresNodes.putIfAbsent(caller.getName(), new Node(caller, this));
            Node callerNode = proceduresNodes.get(caller.getName());

            List<ProcedureCall> procedureCalls = new ProcedureCallGatheringVisitor(caller).gather();

            callerNode.setOutProcedureCalls(procedureCalls);

            for (ProcedureCall procedureCall : procedureCalls) {
                String calleeName = procedureCall.getCallExpression().getName();

                Procedure callee = namesProcedures.get(calleeName);

                proceduresNodes.putIfAbsent(calleeName, new Node(callee, this));
                Node calleeNode = proceduresNodes.get(calleeName);
                calleeNode.addInProcedureCall(callerNode, procedureCall);
            }

            List<Tuple<Assignment, FunctionCall>> functionCalls = new FunctionCallGatheringVisitor(caller).gather();

            callerNode.setOutFunctionCalls(functionCalls);

            for (Tuple<Assignment, FunctionCall> functionCall : functionCalls) {
                String calleeName = functionCall.second.getName();

                Procedure callee = namesProcedures.get(calleeName);

                proceduresNodes.putIfAbsent(calleeName, new Node(callee, this));
                Node calleeNode = proceduresNodes.get(calleeName);
                calleeNode.addInFunctionCall(callerNode, functionCall);
            }
        }
    }

    /**
     * Returns all leaf procedures.
     */
    public Set<Procedure> getLeaves() {
        return proceduresNodes.values().stream().filter(Node::isLeaf).map(Node::getProcedure).collect(Collectors.toSet());
    }

    /**
     * Returns all callers of the given procedure.
     */
    public Set<Procedure> getCallers(Procedure callee) {
        return proceduresNodes.get(callee.getName()).getCallers();
    }

    /**
     * Returns all directly recursive procedures.
     */
    public Set<Procedure> getDirectlyRecursive() {
        return proceduresNodes.values().stream().filter(Node::isDirectlyRecursive).map(Node::getProcedure).collect(Collectors.toSet());
    }

    /**
     * Returns all procedure calls from the first procedure to the second one.
     */
    public List<ProcedureCall> getProcedureCalls(Procedure from, Procedure to) {
        return proceduresNodes.get(from.getName()).getProcedureCallsTo(to);
    }

    /**
     * Returns all function calls from the first procedure to the second one.
     */
    public List<Tuple<Assignment, FunctionCall>> getFunctionCalls(Procedure from, Procedure to) {
        return proceduresNodes.get(from.getName()).getFunctionCallsTo(to);
    }

    /**
     * Transforms the call graph to a simple dot representation.
     */
    public String toDot() {
        StringBuilder dot = new StringBuilder("digraph CallGraph {\n");

        for (Node callerNode : proceduresNodes.values()) {
            Procedure caller = callerNode.procedure;

            for (Procedure callee : callerNode.getCallees()) {
                dot.append(caller.getName().replace('$', '_'));
                dot.append(" -> ");
                dot.append(callee.getName());
                dot.append(";\n");
            }
        }

        dot.append("}\n");

        return dot.toString();
    }

    /**
     * Single call graph node that represents a single procedure.
     */
    private class Node {

        /**
         * The call graph this node belongs to.
         */
        private CallGraph graph;

        /**
         * The procedure this node represents.
         */
        private Procedure procedure;

        /**
         * All procedure calls to this procedure.
         * This has to be a map that includes the caller node,
         * because the caller is not contained in the ProcedureCall object.
         */
        private Map<Node, List<ProcedureCall>> inProcedureCalls;

        /**
         * All function calls to this procedure.
         * This has to be a map that includes the caller node,
         * because the caller is not contained in the Assignment or FunctionCall objects.
         */
        private Map<Node, List<Tuple<Assignment, FunctionCall>>> inFunctionCalls;

        /**
         * All procedure calls from this procedure.
         * The ProcedureCall already contains the callee.
         */
        private List<ProcedureCall> outProcedureCalls;

        /**
         * All function calls from this procedure.
         * The FunctionCall already contains the callee.
         */
        private List<Tuple<Assignment, FunctionCall>> outFunctionCalls;

        public Node(Procedure procedure, CallGraph graph) {
            this.graph = graph;
            this.procedure = procedure;
            this.inProcedureCalls = new HashMap<>();
            this.inFunctionCalls = new HashMap<>();
            this.outProcedureCalls = new ArrayList<>();
            this.outFunctionCalls = new ArrayList<>();
        }

        /**
         * Adds the incoming procedure call.
         */
        public void addInProcedureCall(Node caller, ProcedureCall procedureCall) {
            this.inProcedureCalls.putIfAbsent(caller, new ArrayList<>());
            this.inProcedureCalls.get(caller).add(procedureCall);
        }

        /**
         * Adds the incoming function call.
         */
        public void addInFunctionCall(Node caller, Tuple<Assignment, FunctionCall> functionCall) {
            this.inFunctionCalls.putIfAbsent(caller, new ArrayList<>());
            this.inFunctionCalls.get(caller).add(functionCall);
        }

        /**
         * Sets all outgoing procedure calls.
         */
        public void setOutProcedureCalls(List<ProcedureCall> outProcedureCalls) {
            this.outProcedureCalls = outProcedureCalls;
        }

        /**
         * Sets all outgoing function calls.
         */
        public void setOutFunctionCalls(List<Tuple<Assignment, FunctionCall>> outFunctionCalls) {
            this.outFunctionCalls = outFunctionCalls;
        }

        /**
         * Return this procedure.
         */
        public Procedure getProcedure() {
            return procedure;
        }

        /**
         * Returns all procedures called by the this procedure.
         */
        public Set<Procedure> getCallees() {
            Set<Procedure> procedures = new HashSet<>();

            for (ProcedureCall procedureCall : outProcedureCalls) {
                String calleeName = procedureCall.getCallExpression().getName();

                Procedure callee = graph.proceduresNodes.get(calleeName).procedure;

                procedures.add(callee);
            }

            for (Tuple<Assignment, FunctionCall> functionCall : outFunctionCalls) {
                String calleeName = functionCall.second.getName();

                Procedure callee = graph.proceduresNodes.get(calleeName).procedure;

                procedures.add(callee);
            }

            return procedures;
        }

        /**
         * Returns all procedures that call this procedure.
         */
        public Set<Procedure> getCallers() {
            Set<Procedure> callers = new HashSet<>();

            Set<Procedure> procedureCallers = inProcedureCalls
                    .keySet()
                    .stream()
                    .map(Node::getProcedure)
                    .collect(Collectors.toSet());

            Set<Procedure> functionCallers = inFunctionCalls
                    .keySet()
                    .stream()
                    .map(Node::getProcedure)
                    .collect(Collectors.toSet());

            callers.addAll(procedureCallers);
            callers.addAll(functionCallers);

            return callers;
        }

        /**
         * Returns all procedure calls from this procedure to the given procedure.
         */
        public List<ProcedureCall> getProcedureCallsTo(Procedure callee) {
            return outProcedureCalls
                    .stream()
                    .filter(procedureCall -> procedureCall.getCallExpression().getName().equals(callee.getName()))
                    .collect(Collectors.toList());
        }

        /**
         * Returns all function calls from this procedure to the given procedure.
         */
        public List<Tuple<Assignment, FunctionCall>> getFunctionCallsTo(Procedure callee) {
            return outFunctionCalls
                    .stream()
                    .filter(tuple -> tuple.second.getName().equals(callee.getName()))
                    .collect(Collectors.toList());
        }

        /**
         * Determines whether this function is a leaf.
         */
        public Boolean isLeaf() {
            return getCallees().isEmpty();
        }

        /**
         * Determines whether this function is directly recursive.
         */
        public Boolean isDirectlyRecursive() {
            return getCallees().contains(procedure);
        }
    }
}
