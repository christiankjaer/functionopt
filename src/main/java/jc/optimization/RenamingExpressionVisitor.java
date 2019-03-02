package jc.optimization;

import petter.cfg.expression.BinaryExpression;
import petter.cfg.expression.FunctionCall;
import petter.cfg.expression.Variable;
import petter.cfg.expression.visitors.DefaultUpDownDFS;

import java.util.stream.Stream;

class RenamingExpressionVisitor extends DefaultUpDownDFS<Boolean> {

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
        s.setName(prefix + s.getName());

        return true;
    }
}
