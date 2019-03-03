package jc.optimization;

import jc.CallGraph;
import jc.Tuple;
import jc.Util;
import jc.visitor.FunctionCallGatheringVisitor;
import jc.visitor.ProcedureCallGatheringVisitor;
import petter.cfg.CompilationUnit;
import petter.cfg.Procedure;
import petter.cfg.State;
import petter.cfg.edges.Assignment;
import petter.cfg.edges.Nop;
import petter.cfg.edges.ProcedureCall;
import petter.cfg.edges.Transition;
import petter.cfg.expression.Expression;
import petter.cfg.expression.FunctionCall;
import petter.cfg.expression.Variable;
import petter.simplec.Compiler;

import java.io.File;
import java.util.List;

public class RecursionOptimizer {

    CompilationUnit compilationUnit;

    CallGraph callGraph;

    public RecursionOptimizer(CompilationUnit compilationUnit) {
        this.compilationUnit = compilationUnit;

        this.callGraph = new CallGraph(compilationUnit);
    }

    public void eliminateTailRecursion() {
        for (Procedure procedure : callGraph.getDirectlyRecursiveProcedures()) {
            Util.drawGraph(procedure, "recursive_" + procedure.getName());

            // todo: check it is really *tail* recursive
            // probably means that the call transition can have only
            // Nop and Assignment (return = ???) edges before reaching the end state

            System.out.println("Eliminating tail recursion in " + procedure.getName());

            List<ProcedureCall> procedureCalls = new ProcedureCallGatheringVisitor(procedure, procedure).gather();

            List<Tuple<Assignment, FunctionCall>> functionCalls = new FunctionCallGatheringVisitor(procedure, procedure).gather();

            for (ProcedureCall procedureCall : procedureCalls) {
                eliminateProcedureRecursion(procedureCall, procedureCall.getCallExpression(), procedure);
            }

            for (Tuple<Assignment, FunctionCall> functionCall : functionCalls) {
                eliminateProcedureRecursion(functionCall.first, functionCall.second, procedure);
            }

            Util.drawGraph(procedure, "recursive_done_" + procedure.getName());
        }
    }

    private void eliminateProcedureRecursion(Transition transition, FunctionCall call, Procedure procedure) {
        State callBegin = transition.getSource();

        transition.removeEdge();

        // todo: maybe? set locals to default values

        callBegin = updateArguments(callBegin, call, procedure);

        insertJumpToBeginning(callBegin, procedure);

        procedure.refreshStates();
    }

    private State updateArguments(State callBegin, FunctionCall call, Procedure procedure) {
        List<String> parameters = Util.getParameterNames(procedure, compilationUnit);

        List<Expression> arguments = call.getParamsUnchanged();

        for (Tuple<String, Expression> pair : Util.zip(parameters, arguments)) {
            String name = pair.first;
            Expression expr = pair.second;

            State extra = new State();

            new Assignment(callBegin, extra, new Variable(1000, name, expr.getType()), expr);

            callBegin = extra;
        }

        return callBegin;
    }

    private void insertJumpToBeginning(State callBegin, Procedure procedure) {
        State procedureBegin = procedure.getBegin();

        new Nop(callBegin, procedureBegin);

        procedureBegin.setBegin(true);
    }

    public static void main(String[] args) throws Exception {
        CompilationUnit compilationUnit = Compiler.parse(new File("examples/recursion.c"));

        RecursionOptimizer optimizer = new RecursionOptimizer(compilationUnit);

        optimizer.eliminateTailRecursion();
    }
}
