package jc.optimization;

import jc.optimization.CallGraph.Node;

import petter.cfg.*;
import petter.cfg.edges.*;
import petter.cfg.expression.Expression;
import petter.cfg.expression.FunctionCall;
import petter.cfg.expression.Variable;
import petter.simplec.Compiler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class Optimization {

    CompilationUnit compilationUnit;

    CallGraph callGraph;

    public Optimization(CompilationUnit compilationUnit) {
        this.compilationUnit = compilationUnit;

        this.callGraph = new CallGraph(compilationUnit);
    }

    public void inlineLeafFunctions() {
        for (Node leafNode : callGraph.getLeafNodes()) {
            Procedure callee = leafNode.getProcedure();

            for (Procedure caller : leafNode.getCallerProcedures()) {
                inlineCallsFromTo(caller, callee);
            }
        }
    }

    public void inlineCallsFromTo(Procedure caller, Procedure callee) {
        GatherCallsVisitor visitor = new GatherCallsVisitor(callee);
        caller.forwardAccept(visitor, true);
        visitor.fullAnalysis();

        for (ProcedureCall procedureCall : visitor.getProcedureCalls()) {
            Util.drawGraph(caller, "caller");
            Util.drawGraph(callee, "callee");

            inlineProcedure(procedureCall, procedureCall.getCallExpression(), caller, callee, generatePrefix());

            Util.drawGraph(caller, "caller_inlined");
        }

        for (Tuple<Assignment, FunctionCall> functionCall : visitor.getFunctionCalls()) {
            Util.drawGraph(caller, "caller");
            Util.drawGraph(callee, "callee");

            inlineFunction(functionCall.first, functionCall.second, caller, callee, generatePrefix());

            Util.drawGraph(caller, "caller_inlined");
        }
    }

    private void inlineProcedure(Transition trans, FunctionCall expr, Procedure caller, Procedure callee, String prefix) {
        State callBegin = trans.getSource();
        State callEnd = trans.getDest();

        trans.removeEdge();

        callBegin = transformArgumentsToVariables(expr, callee, callBegin, prefix);

        Tuple<State, State> copy = copyBody(callee);

        renameCalleeVariables(prefix, copy.first);

        new Nop(callBegin, copy.first);
        new Nop(copy.second, callEnd);

        caller.refreshStates();
    }

    private Tuple<State, State> copyBody(Procedure procedure) {
        List<Transition> oldTransitions = new ArrayList<>(procedure.getTransitions());

        CopyingVisitor copier = new CopyingVisitor(oldTransitions);

        List<Transition> copiedBody = copier.getNewTransitions();

        return Util.findBeginEnd(copiedBody);
    }

    private void inlineFunction(Assignment ass, FunctionCall call, Procedure caller, Procedure callee, String prefix) {
        inlineProcedure(ass, call, caller, callee, prefix);

        assignReturn(ass, caller, prefix);
    }

    private void assignReturn(Assignment ass, Procedure caller, String prefix) {
        State callEnd = ass.getDest();

        List<Transition> incomings = StreamSupport.stream(callEnd.getIn().spliterator(), false).collect(Collectors.toList());

        assert incomings.size() == 1;

        Transition incoming = incomings.get(0);

        State newCallEnd = new State();

        incoming.setDest(newCallEnd);

        Expression returnedVariable = new Variable(1001, prefix + "return", ass.getRhs().getType());

        new Assignment(newCallEnd, callEnd, ass.getLhs(), returnedVariable);

        caller.refreshStates();
    }

    private State transformArgumentsToVariables(FunctionCall expr, Procedure callee, State callBegin, String prefix) {
        List<Expression> arguments = expr.getParamsUnchanged();
        List<String> parameters = callee
                .getFormalParameters()
                .stream()
                .map(integer -> compilationUnit.getVariableName(integer))
                .collect(Collectors.toList());

        assert arguments.size() == parameters.size();

        for (int i = 0; i < arguments.size(); i++) {
            State newCallBegin = new State();

            Expression argument = new Variable(1000, prefix + parameters.get(i), arguments.get(i).getType());

            new Assignment(callBegin, newCallBegin, argument, arguments.get(i));

            callBegin = newCallBegin;
        }

        return callBegin;
    }

    private void renameCalleeVariables(String prefix, State calleeEnter) {
        RenamingVisitor visitor = new RenamingVisitor(prefix);

        calleeEnter.forwardAccept(visitor, true);

        visitor.fullAnalysis();
    }

    private String generatePrefix() {
        return "__in_" + Util.randomString(3) + "_";
    }

    public static void main(String[] args) throws Exception {
        CompilationUnit compilationUnit = Compiler.parse(new File("examples/function.c"));

        Optimization optimization = new Optimization(compilationUnit);

        optimization.inlineLeafFunctions();
    }
}
