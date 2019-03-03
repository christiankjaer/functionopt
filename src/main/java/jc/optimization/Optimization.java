package jc.optimization;

import petter.cfg.*;
import petter.cfg.edges.*;
import petter.cfg.expression.Expression;
import petter.cfg.expression.FunctionCall;
import petter.cfg.expression.Variable;
import petter.simplec.Compiler;

import java.io.File;
import java.util.List;

public class Optimization {

    CompilationUnit compilationUnit;

    CallGraph callGraph;

    public Optimization(CompilationUnit compilationUnit) {
        this.compilationUnit = compilationUnit;

        this.callGraph = new CallGraph(compilationUnit);
    }

    public void inlineLeafFunctions() {
        for (CallGraph.Node leafNode : callGraph.getLeafNodes()) {
            Procedure callee = leafNode.getProcedure();

            for (Procedure caller : leafNode.getCallerProcedures()) {
                Util.drawGraph(caller, "caller");
                Util.drawGraph(callee, "callee");

                inlineCallsFromTo(caller, callee);

                Util.drawGraph(caller, "caller_inlined");
            }
        }
    }

    public void inlineCallsFromTo(Procedure caller, Procedure callee) {
        GatherCallsVisitor visitor = new GatherCallsVisitor(callee);
        caller.forwardAccept(visitor, true);
        visitor.fullAnalysis();

        for (ProcedureCall procedureCall : visitor.getProcedureCalls()) {
            inlineProcedure(procedureCall, procedureCall.getCallExpression(), caller, callee, generatePrefix());
        }

        for (Tuple<Assignment, FunctionCall> functionCall : visitor.getFunctionCalls()) {
            inlineFunction(functionCall.first, functionCall.second, caller, callee, generatePrefix());
        }
    }

    private void inlineProcedure(Transition trans, FunctionCall expr, Procedure caller, Procedure callee, String prefix) {
        State callBegin = trans.getSource();
        State callEnd = trans.getDest();

        trans.removeEdge();

        callBegin = inlineArguments(expr, callee, callBegin, prefix);

        ProcedureBody copy = copyBody(callee);

        prefixVariables(copy, prefix);

        new Nop(callBegin, copy.getBegin());
        new Nop(copy.getEnd(), callEnd);

        caller.refreshStates();
    }

    private ProcedureBody copyBody(Procedure procedure) {
        return new CopyingVisitor(procedure).copyBody();
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

    private void prefixVariables(ProcedureBody body, String prefix) {
        new RenamingVisitor(body, prefix).renameVariables();
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
