package jc.optimization;

import jc.CallGraph;
import jc.ProcedureBody;
import jc.Tuple;
import jc.Util;
import jc.visitor.CopyingVisitor;
import jc.visitor.RenamingVisitor;
import petter.cfg.*;
import petter.cfg.edges.*;
import petter.cfg.expression.Expression;
import petter.cfg.expression.FunctionCall;
import petter.cfg.expression.Variable;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// todo: fix bug in example 4

/**
 * Inlines function calls to avoid the call overhead.
 */
public class CallInliner {

    private final CompilationUnit compilationUnit;

    private final CallGraph callGraph;

    /**
     * The maximum number of calls a function can receive to be inlined.
     */
    private final int maxAllowedCalls;

    /**
     * The maximum number of transition a function can have to be inlined.
     */
    private final int maxAllowedSize;

    public CallInliner(CompilationUnit compilationUnit, int maxAllowedCalls, int maxAllowedSize) {
        this.compilationUnit = compilationUnit;

        this.callGraph = new CallGraph(compilationUnit);

        this.maxAllowedCalls = maxAllowedCalls;

        this.maxAllowedSize = maxAllowedSize;
    }

    /**
     * Inlines all leaf functions that satisfy the heuristics provided in constructor.
     */
    public void inlineLeafFunctions() {
        // todo: abort if the callee is in a loop in the call graph

        // Filter with heuristic
        Set<Procedure> toInline = callGraph
                .getLeaves()
                .stream()
                .filter(this::shouldInline)
                .collect(Collectors.toSet());

        for (Procedure callee : toInline) {
            for (Procedure caller : callGraph.getCallers(callee)) {
                inlineCallsFromTo(caller, callee);
            }
        }
    }

    /**
     * Decides whether the given procedure should be inlined considering the heuristics provided in constructor.
     */
    private boolean shouldInline(Procedure p) {
        return p.getTransitions().size() <= maxAllowedSize && callGraph.getCallers(p).size() <= maxAllowedCalls;
    }

    /**
     * Inlines all calls from the given caller to the callee.
     */
    public void inlineCallsFromTo(Procedure caller, Procedure callee) {
        callGraph.getProcedureCalls(caller, callee)
                .forEach(call -> inlineProcedure(call, call.getCallExpression(), caller, callee, generatePrefix()));

        callGraph.getFunctionCalls(caller, callee)
                .forEach(call -> inlineFunction(call.first, call.second, caller, callee, generatePrefix()));
    }

    /**
     * Inlines a simple procedure call. Copies the callee body while avoiding variable shadowing and prepares arguments.
     */
    private void inlineProcedure(Transition trans, FunctionCall expr, Procedure caller, Procedure callee, String prefix) {

        ProcedureBody copy = copyBody(callee);

        State callBegin = trans.getSource();
        State callEnd = trans.getDest();

        trans.removeEdge();

        callBegin = inlineArguments(callBegin, expr, callee, prefix);

        prefixVariables(copy, prefix);

        insertTransitionsToAndFrom(callBegin, callEnd, copy);

        caller.refreshStates();
    }

    /**
     * Inlines a function call in the same manner as a procedure, but also propagates the callee return into the caller.
     */
    private void inlineFunction(Assignment ass, FunctionCall call, Procedure caller, Procedure callee, String prefix) {
        inlineProcedure(ass, call, caller, callee, prefix);

        assignReturn(ass, caller, prefix);
    }

    /**
     * Creates new variable assignments that mimic argument passing during function call from the caller to the callee.
     * Assumes that local variables (including parameters) of the callee were prefixed with the given string.
     * Returns a state that is guaranteed to have all arguments inlined - can be used for a transition from caller to callee.
     */
    private State inlineArguments(State callBegin, FunctionCall call, Procedure callee, String prefix) {
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

    /**
     * Copies the body of the provided procedure.
     */
    private ProcedureBody copyBody(Procedure procedure) {
        return new CopyingVisitor(procedure).copyBody();
    }

    /**
     * Prefixes all variable names in the body with the given string.
     */
    private void prefixVariables(ProcedureBody body, String prefix) {
        new RenamingVisitor(body, prefix).renameVariables();
    }

    /**
     * Inserts transitions to and from the procedure body to mimic a function call.
     */
    private void insertTransitionsToAndFrom(State callBegin, State callEnd, ProcedureBody copy) {
        new Nop(callBegin, copy.getBegin());
        new Nop(copy.getEnd(), callEnd);
    }

    /**
     * Creates new assignment in the caller body that propagates the return value from an inlined procedure.
     * It is assumed that local variables of the callee were prefixed with the provided string.
     */
    private void assignReturn(Assignment ass, Procedure caller, String prefix) {
        Expression lhs = ass.getLhs();
        Expression rhs = ass.getRhs();

        Tuple<State, State> call = new Tuple<>(ass.getSource(), ass.getDest());

        Transition incoming = Util.getTheOnly(call.second.getIn());

        State extra = new State();

        incoming.setDest(extra);

        new Assignment(extra, call.second, lhs, new Variable(1001, prefix + "return", rhs.getType()));

        caller.refreshStates();
    }

    /**
     * Generate an unique prefix for a procedure to be inlined.
     */
    private String generatePrefix() {
        return "__" + Util.randomString(3) + "_";
    }
}
