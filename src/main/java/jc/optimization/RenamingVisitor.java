package jc.optimization;

import petter.cfg.edges.Assignment;
import petter.cfg.edges.GuardedTransition;
import petter.cfg.edges.ProcedureCall;
import petter.cfg.expression.BinaryExpression;
import petter.cfg.expression.Expression;
import petter.cfg.expression.FunctionCall;
import petter.cfg.expression.Variable;
import petter.cfg.expression.visitors.DefaultUpDownDFS;

import java.util.stream.Stream;

class RenamingVisitor extends AbstractTransitionVisitor {

    ProcedureBody body;

    RenamingExpressionVisitor expressionVisitor;

    public RenamingVisitor(ProcedureBody body, String prefix) {
        this.body = body;

        this.expressionVisitor = new RenamingExpressionVisitor(prefix);
    }

    @Override
    public void visit(Assignment assignment) {
        Expression left = assignment.getLhs();
        Expression right = assignment.getRhs();

        left.accept(expressionVisitor, true);
        right.accept(expressionVisitor, true);
    }

    @Override
    public void visit(GuardedTransition guard) {
        Expression assertion = guard.getAssertion();

        assertion.accept(expressionVisitor, true);
    }

    @Override
    public void visit(ProcedureCall call) {
        Expression expression = call.getCallExpression();

        expression.accept(expressionVisitor, true);
    }

    public void renameVariables() {
        super.visit(body.getTransitions());
    }

    private class RenamingExpressionVisitor extends DefaultUpDownDFS<Boolean> {

        private String prefix;

        public RenamingExpressionVisitor(String prefix) {
            super();

            this.prefix = prefix;
        }

        @Override
        public Boolean postVisit(FunctionCall m, Boolean s, Stream<Boolean> it) {
            return true;
        }

        @Override
        public Boolean postVisit(BinaryExpression s, Boolean lhs, Boolean rhs) {
            return true;
        }

        @Override
        public Boolean postVisit(Variable s, Boolean fromParent) {
            String originalName = s.getName();

            if (!originalName.startsWith(prefix)) {
                s.setName(prefix + s.getName());
            }

            return true;
        }
    }
}
