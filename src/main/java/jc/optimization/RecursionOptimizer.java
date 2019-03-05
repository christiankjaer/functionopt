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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Optimizes recursive calls.
 */
public class RecursionOptimizer {

    CompilationUnit compilationUnit;

    CallGraph callGraph;

    public RecursionOptimizer(CompilationUnit compilationUnit, CallGraph callGraph) {
        this.compilationUnit = compilationUnit;

        this.callGraph = callGraph;
    }

    /**
     * Eliminates all directly tail recursive calls in all procedures.
     */
    public void eliminateTailRecursion() {
        for (Procedure procedure : callGraph.getDirectlyRecursive()) {
            callGraph.getProcedureCalls(procedure, procedure).stream()
                    .filter(call -> isTailRecursive(call, procedure, null))
                    .forEach(call -> eliminateTailRecursionImpl(call, call.getCallExpression(), procedure));

            callGraph.getFunctionCalls(procedure, procedure).stream()
                    .filter(call -> isTailRecursive(call.first, procedure, call.first.getLhs()))
                    .forEach(call -> eliminateTailRecursionImpl(call.first, call.second, procedure));
        }
    }

    /**
     * Unrolls all directly recursive calls (even non-tail calls) in all procedures.
     * The limit controls the number of unrolling operations per single procedure.
     */
    public void unrollRecursion(Integer limit) {
        CallInliner inliner = new CallInliner(compilationUnit, 0, 0);

        for (Procedure procedure : callGraph.getDirectlyRecursive()) {
            for (Integer i = 0; i < limit; i += 1) {
                inliner.inlineCallsFromTo(procedure, procedure);
            }
        }
    }

    /**
     * Eliminates the tail recursion by:
     *  * assigning the call arguments to variables representing procedure parameters and
     *  * jumping to the beginning of the procedure.
     *  Assumes that the transition contains the call.
     */
    private void eliminateTailRecursionImpl(Transition transition, FunctionCall call, Procedure procedure) {
        State callBegin = transition.getSource();

        transition.removeEdge();

        // todo: maybe? set locals to default values

        callBegin = updateArguments(callBegin, call, procedure);

        insertJumpToBeginning(callBegin, procedure);

        procedure.refreshStates();
    }

    /**
     * Determines whether the provided function/procedure call is tail recursive. (Assuming the transition is a call.)
     * That means that the given transition reaches the end of the procedure with only Nop edges or edges that assign
     * the call result to the "return" variable.
     */
    private Boolean isTailRecursive(Transition transition, Procedure procedure, Expression callResult) {
        State currentState = transition.getDest();

        Set<State> seen = new HashSet<>();

        seen.add(currentState);

        while (currentState != procedure.getEnd()) {
            if (currentState.getOutDegree() > 1) {
                // it's a branch
                return false;
            }

            Transition outgoing = Util.getTheOnly(currentState.getOut());

            if (!isNop(outgoing) && !isSimpleReturnAssignment(outgoing, callResult)) {
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

    /**
     * Creates new assignments that update the procedure parameter variables with the call arguments.
     * Returns a state that contains all of the new assignments and is therefore ready for the jump.
     */
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

    /**
     * Creates a jump to the beginning of the given procedure.
     */
    private void insertJumpToBeginning(State callBegin, Procedure procedure) {
        State procedureBegin = procedure.getBegin();

        new Nop(callBegin, procedureBegin);

        procedureBegin.setBegin(true);
    }

    /**
     * Determines whether the transition is a Nop edge.
     */
    private Boolean isNop(Transition transition) {
        return transition instanceof Nop;
    }

    /**
     * Determines whether the transition is an assignment "return = callResult".
     */
    private Boolean isSimpleReturnAssignment(Transition transition, Expression callResult) {
        if (callResult == null) {
            return false;
        }

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

        return assignment.getRhs() == callResult;
    }
}
