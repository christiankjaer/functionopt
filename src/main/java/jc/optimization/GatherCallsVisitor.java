package jc.optimization;

import petter.cfg.AbstractPropagatingVisitor;
import petter.cfg.edges.Assignment;
import petter.cfg.edges.ProcedureCall;
import petter.cfg.expression.BinaryExpression;
import petter.cfg.expression.Expression;
import petter.cfg.expression.FunctionCall;
import petter.cfg.expression.visitors.DefaultUpDownDFS;

import java.util.*;
import java.util.stream.Stream;

class Tuple<T1, T2> {

    public T1 first;

    public T2 second;

    Tuple(T1 first, T2 second) {
        this.first = first;

        this.second = second;
    }
}

class GatherFunctionCallsVisitor extends AbstractPropagatingVisitor<Boolean> {

    GatherFunctionCallsExpressionVisitor expressionVisitor;

    List<Tuple<Assignment, FunctionCall>> functionCalls;

    List<ProcedureCall> procedureCalls;

    public GatherFunctionCallsVisitor() {
        super(true);

        expressionVisitor = new GatherFunctionCallsExpressionVisitor();

        functionCalls = new ArrayList<>();

        procedureCalls = new ArrayList<>();
    }

    public List<Tuple<Assignment, FunctionCall>> getFunctionCalls() {
        return functionCalls;
    }

    public List<ProcedureCall> getProcedureCalls() {
        return procedureCalls;
    }

    @Override
    public Boolean visit(ProcedureCall procedureCall, Boolean d) {
        procedureCalls.add(procedureCall);

        return true;
    }

    @Override
    public Boolean visit(Assignment assignment, Boolean d) {
        Expression rhs = assignment.getRhs();

        Optional<List<FunctionCall>> optionalCalls = rhs.accept(expressionVisitor, new ArrayList<>());

        if (optionalCalls.isEmpty()) {
            return true;
        }

        List<FunctionCall> calls = optionalCalls.get();

        if (calls.isEmpty()) {
            return true;
        }

        FunctionCall call = calls.get(0);

        functionCalls.add(new Tuple<>(assignment, call));

        return true;
    }
}

class GatherFunctionCallsExpressionVisitor extends DefaultUpDownDFS<List<FunctionCall>> {
    @Override
    public List<FunctionCall> postVisit(FunctionCall call, List<FunctionCall> parent, Stream<List<FunctionCall>> children) {
        List<FunctionCall> merged = new ArrayList<>();

        merged.add(call);
        merged.addAll(parent);
        children.forEach(merged::addAll);

        return merged;
    }

    // This is not specific to call gathering, just propagating results of sub-expressions...
    @Override
    public List<FunctionCall> postVisit(BinaryExpression expression, List<FunctionCall> lhs, List<FunctionCall> rhs) {
        List<FunctionCall> merged = new ArrayList<>();

        merged.addAll(lhs);
        merged.addAll(rhs);

        return merged;
    }
}
