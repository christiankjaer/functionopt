package jc.optimization;

import petter.cfg.AbstractPropagatingVisitor;
import petter.cfg.edges.Assignment;
import petter.cfg.edges.GuardedTransition;
import petter.cfg.edges.ProcedureCall;
import petter.cfg.expression.BinaryExpression;
import petter.cfg.expression.Expression;
import petter.cfg.expression.FunctionCall;
import petter.cfg.expression.Variable;
import petter.cfg.expression.visitors.DefaultUpDownDFS;

import java.util.stream.Stream;

class RenamingVisitor extends AbstractPropagatingVisitor<Boolean> {

    RenamingExpressionVisitor expressionVisitor;

    public RenamingVisitor(String prefix) {
        super(true);

        expressionVisitor = new RenamingExpressionVisitor(prefix);
    }

    @Override
    public Boolean visit(ProcedureCall ae, Boolean d) {
        Expression expression = ae.getCallExpression();

        expression.accept(expressionVisitor, true);

        return true;
    }

    @Override
    public Boolean visit(Assignment s, Boolean d) {
        Expression left = s.getLhs();
        Expression right = s.getRhs();

        left.accept(expressionVisitor, true);
        right.accept(expressionVisitor, true);

        return true;
    }

    @Override
    public Boolean visit(GuardedTransition s, Boolean d) {
        Expression assertion = s.getAssertion();

        assertion.accept(expressionVisitor, true);

        return true;
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
