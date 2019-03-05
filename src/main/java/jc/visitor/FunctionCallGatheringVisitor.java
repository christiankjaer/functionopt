package jc.visitor;

import jc.Tuple;
import petter.cfg.Procedure;
import petter.cfg.edges.Assignment;
import petter.cfg.edges.Transition;
import petter.cfg.expression.BinaryExpression;
import petter.cfg.expression.Expression;
import petter.cfg.expression.FunctionCall;
import petter.cfg.expression.visitors.DefaultUpDownDFS;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Gathers all function calls along with the corresponding assignment in the given procedure.
 */
public class FunctionCallGatheringVisitor extends AbstractTransitionVisitor {

    List<Transition> callerTransitions;

    GatherCallsExprVisitor expressionVisitor;

    List<Tuple<Assignment, FunctionCall>> functionCalls;


    public FunctionCallGatheringVisitor(Procedure caller) {
        callerTransitions = new ArrayList<>(caller.getTransitions());

        expressionVisitor = new GatherCallsExprVisitor();

        functionCalls = new ArrayList<>();
    }

    public List<Tuple<Assignment, FunctionCall>> gather() {
        visit(callerTransitions);

        return functionCalls;
    }

    @Override
    protected void visit(Assignment assignment) {
        Expression rhs = assignment.getRhs();

        Optional<List<FunctionCall>> optionalCalls = rhs.accept(expressionVisitor, new ArrayList<>());

        if (!optionalCalls.isPresent()) {
            return;
        }

        List<FunctionCall> calls = optionalCalls.get();

        if (calls.isEmpty()) {
            return;
        }

        FunctionCall call = calls.get(0);

        functionCalls.add(new Tuple<>(assignment, call));
    }

    private class GatherCallsExprVisitor extends DefaultUpDownDFS<List<FunctionCall>> {
        @Override
        public List<FunctionCall> postVisit(FunctionCall call, List<FunctionCall> parent, Stream<List<FunctionCall>> children) {
            List<FunctionCall> merged = new ArrayList<>();

            merged.add(call);

            // merge existing calls
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
}
