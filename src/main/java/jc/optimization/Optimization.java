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
        Tuple<State, State> call = new Tuple<>(trans.getSource(), trans.getDest());

        trans.removeEdge();

        call.first = inlineArguments(expr, callee, call.first, prefix);

        Tuple<State, State> copy = copyBody(callee);

        prefixVariables(copy, prefix);

        new Nop(call.first, copy.first);
        new Nop(copy.second, call.second);

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
        Expression lhs = ass.getLhs();
        Expression rhs = ass.getRhs();

        Tuple<State, State> call = new Tuple<>(ass.getSource(), ass.getDest());

        Transition incoming = Util.getIncoming(call.second);

        State extra = new State();

        incoming.setDest(extra);

        new Assignment(extra, call.second, lhs, new Variable(1001, prefix + "return", rhs.getType()));

        caller.refreshStates();
    }

    private State inlineArguments(FunctionCall call, Procedure callee, State callBegin, String prefix) {
        List<String> parameters = Util.getParameterNames(callee, compilationUnit);

        List<Expression> arguments = call.getParamsUnchanged();

        for (Tuple<String, Expression> pair : Util.zip(parameters, arguments)) {
            String name = pair.first;
            Expression expr = pair.second;

            State extra = new State();

            new Assignment(callBegin, extra, new Variable(1000, prefix + name, expr.getType()), expr);

            callBegin = extra;
        }

        return callBegin;
    }

    private void prefixVariables(Tuple<State, State> body, String prefix) {
        RenamingVisitor visitor = new RenamingVisitor(prefix);

        body.first.forwardAccept(visitor, true);

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
