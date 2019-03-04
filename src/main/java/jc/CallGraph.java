package jc;

import jc.visitor.FunctionCallGatheringVisitor;
import jc.visitor.ProcedureCallGatheringVisitor;
import petter.cfg.CompilationUnit;
import petter.cfg.Procedure;
import petter.cfg.edges.Assignment;
import petter.cfg.edges.ProcedureCall;
import petter.cfg.expression.FunctionCall;
import petter.simplec.Compiler;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class CallGraph {
    private class N {
        private CallGraph graph;

        private Procedure procedure;

        private Map<N, List<ProcedureCall>> inProcedureCalls;

        private Map<N, List<Tuple<Assignment, FunctionCall>>> inFunctionCalls;

        private List<ProcedureCall> outProcedureCalls;

        private List<Tuple<Assignment, FunctionCall>> outFunctionCalls;

        public N(Procedure procedure, CallGraph graph) {
            this.graph = graph;
            this.procedure = procedure;
            this.inProcedureCalls = new HashMap<>();
            this.inFunctionCalls = new HashMap<>();
            this.outProcedureCalls = new ArrayList<>();
            this.outFunctionCalls = new ArrayList<>();
        }

        public void addInProcedureCall(N caller, ProcedureCall procedureCall) {
            this.inProcedureCalls.putIfAbsent(caller, new ArrayList<>());
            this.inProcedureCalls.get(caller).add(procedureCall);
        }

        public void addInFunctionCall(N caller, Tuple<Assignment, FunctionCall> functionCall) {
            this.inFunctionCalls.putIfAbsent(caller, new ArrayList<>());
            this.inFunctionCalls.get(caller).add(functionCall);
        }

        public void addOutProcedureCalls(List<ProcedureCall> outProcedureCalls) {
            this.outProcedureCalls.addAll(outProcedureCalls);
        }

        public void addOutFunctionCalls(List<Tuple<Assignment, FunctionCall>> outFunctionCalls) {
            this.outFunctionCalls.addAll(outFunctionCalls);
        }

        public Procedure getProcedure() {
            return procedure;
        }

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

        public Set<Procedure> getCallers() {
            Set<Procedure> callers = new HashSet<>();

            Set<Procedure> procedureCallers = inProcedureCalls
                    .keySet()
                    .stream()
                    .map(N::getProcedure)
                    .collect(Collectors.toSet());

            Set<Procedure> functionCallers = inFunctionCalls
                    .keySet()
                    .stream()
                    .map(N::getProcedure)
                    .collect(Collectors.toSet());

            callers.addAll(procedureCallers);
            callers.addAll(functionCallers);

            return callers;
        }

        public List<ProcedureCall> getProcedureCallsTo(Procedure callee) {
            return outProcedureCalls
                    .stream()
                    .filter(procedureCall -> procedureCall.getCallExpression().getName().equals(callee.getName()))
                    .collect(Collectors.toList());
        }

        public List<Tuple<Assignment, FunctionCall>> getFunctionCallsTo(Procedure callee) {
            return outFunctionCalls
                    .stream()
                    .filter(tuple -> tuple.second.getName().equals(callee.getName()))
                    .collect(Collectors.toList());
        }

        public Boolean isLeaf() {
            return getCallees().isEmpty();
        }

        public Boolean isDirectlyRecursive() {
            return getCallees().contains(procedure);
        }
    }

    private Map<String, N> proceduresNodes;

    public CallGraph(CompilationUnit compilationUnit) {
        proceduresNodes = new HashMap<>();

        Map<String, Procedure> namesProcedures = compilationUnit.getProcedures();

        for (Procedure caller : namesProcedures.values()) {
            proceduresNodes.putIfAbsent(caller.getName(), new N(caller, this));
            N callerNode = proceduresNodes.get(caller.getName());

            List<ProcedureCall> procedureCalls = new ProcedureCallGatheringVisitor(caller).gather();

            callerNode.addOutProcedureCalls(procedureCalls);

            for (ProcedureCall procedureCall : procedureCalls) {
                String calleeName = procedureCall.getCallExpression().getName();

                Procedure callee = namesProcedures.get(calleeName);

                proceduresNodes.putIfAbsent(calleeName, new N(callee, this));
                N calleeNode = proceduresNodes.get(calleeName);
                calleeNode.addInProcedureCall(callerNode, procedureCall);
            }

            List<Tuple<Assignment, FunctionCall>> functionCalls = new FunctionCallGatheringVisitor(caller).gather();

            callerNode.addOutFunctionCalls(functionCalls);

            for (Tuple<Assignment, FunctionCall> functionCall : functionCalls) {
                String calleeName = functionCall.second.getName();

                Procedure callee = namesProcedures.get(calleeName);

                proceduresNodes.putIfAbsent(calleeName, new N(callee, this));
                N calleeNode = proceduresNodes.get(calleeName);
                calleeNode.addInFunctionCall(callerNode, functionCall);
            }
        }
    }

    public Set<Procedure> getLeaves() {
        return proceduresNodes.values().stream().filter(N::isLeaf).map(N::getProcedure).collect(Collectors.toSet());
    }

    public Set<Procedure> getCallers(Procedure callee) {
        return proceduresNodes.get(callee.getName()).getCallers();
    }

    public Set<Procedure> getDirectlyRecursive() {
        return proceduresNodes.values().stream().filter(N::isDirectlyRecursive).map(N::getProcedure).collect(Collectors.toSet());
    }

    public List<ProcedureCall> getProcedureCalls(Procedure from, Procedure to) {
        return proceduresNodes.get(from.getName()).getProcedureCallsTo(to);
    }

    public List<Tuple<Assignment, FunctionCall>> getFunctionCalls(Procedure from, Procedure to) {
        return proceduresNodes.get(from.getName()).getFunctionCallsTo(to);
    }

    public String toDot() {
        StringBuilder dot = new StringBuilder("digraph CallGraph {\n");

        for (N callerNode : proceduresNodes.values()) {
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

    public static void main(String[] args) throws Exception {
        String filename = "examples/inline_functions_0.c";

        File file = new File(filename);

        CompilationUnit compilationUnit = Compiler.parse(file);

        CallGraph callGraph = new CallGraph(compilationUnit);

        Util.drawCallGraph(callGraph, filename);
    }
}
