package jc.optimization;

import petter.cfg.AbstractPropagatingVisitor;
import petter.cfg.edges.Assignment;
import petter.cfg.edges.GuardedTransition;
import petter.cfg.edges.ProcedureCall;
import petter.cfg.expression.Expression;

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
}
