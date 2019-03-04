package jc.optimization;

import jc.CallGraph;
import jc.Tuple;
import jc.Util;
import petter.cfg.CompilationUnit;
import petter.cfg.Procedure;
import petter.cfg.State;
import petter.cfg.edges.Assignment;
import petter.cfg.edges.Nop;
import petter.cfg.edges.Transition;
import petter.cfg.expression.Expression;
import petter.cfg.expression.FunctionCall;
import petter.cfg.expression.Variable;
import petter.simplec.Compiler;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RecursionOptimizer {

    CompilationUnit compilationUnit;

    CallGraph callGraph;

    public RecursionOptimizer(CompilationUnit compilationUnit, CallGraph callGraph) {
        this.compilationUnit = compilationUnit;

        this.callGraph = callGraph;
    }

    public void eliminateTailRecursion() {
        for (Procedure procedure : callGraph.getDirectlyRecursive()) {
            callGraph.getProcedureCalls(procedure, procedure).stream()
                    .filter(call -> isTailRecursive(call, procedure, null))
                    .forEach(call -> eliminateProcedureRecursion(call, call.getCallExpression(), procedure));

            callGraph.getFunctionCalls(procedure, procedure).stream()
                    .filter(call -> isTailRecursive(call.first, procedure, call.first.getLhs()))
                    .forEach(call -> eliminateProcedureRecursion(call.first, call.second, procedure));
        }
    }

    public void unrollRecursion(Integer limit) {
        CallInliner inliner = new CallInliner(compilationUnit);

        for (Procedure procedure : callGraph.getDirectlyRecursive()) {
            for (Integer i = 0; i < limit; i += 1) {
                inliner.inlineCallsFromTo(procedure, procedure);
            }
        }
    }

    private Boolean isTailRecursive(Transition transition, Procedure procedure, Expression assignmentTarget) {
        State currentState = transition.getDest();

        Set<State> seen = new HashSet<>();

        seen.add(currentState);

        while (currentState != procedure.getEnd()) {
            if (currentState.getOutDegree() > 1) {
                // it's a branch
                return false;
            }

            Transition outgoing = Util.getTheOnly(currentState.getOut());

            if (!isNop(outgoing) && !isSimpleReturnAssignment(outgoing, assignmentTarget)) {
                return false;
            }

            currentState = outgoing.getDest();

            if (seen.contains(currentState)) {
                // we detected a cycle
                return false;
            }

            seen.add(currentState);
        }

        return true;
    }

    private Boolean isNop(Transition transition) {
        return transition instanceof Nop;
    }

    private Boolean isSimpleReturnAssignment(Transition transition, Expression assignmentTarget) {
        if (!(transition instanceof Assignment)) {
            return false;
        }

        Assignment assignment = (Assignment) transition;

        Expression lhs = assignment.getLhs();

        if (!(lhs instanceof Variable)) {
            return false;
        }

        Variable variable = (Variable) lhs;

        if (!variable.getName().equals("return")) {
            return false;
        }

        if (assignmentTarget != null && assignment.getRhs() != assignmentTarget) {
            // we have `return = ???`, where ??? is something else than direct result of the recursive function call
            return false;
        }

        return true;
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
        String filename = "optimize_tail.c";

        File file = new File("examples/" + filename);

        CompilationUnit compilationUnit = Compiler.parse(file);

        CallGraph callGraph = new CallGraph(compilationUnit);

        Procedure recursive = new ArrayList<>(callGraph.getDirectlyRecursive()).get(0);

        Util.drawCFG(recursive, "graphs/" + filename);

        RecursionOptimizer optimizer = new RecursionOptimizer(compilationUnit, callGraph);

        optimizer.eliminateTailRecursion();

        optimizer.unrollRecursion(1);

        Util.drawCFG(recursive, "graphs/" + filename);
    }
}
