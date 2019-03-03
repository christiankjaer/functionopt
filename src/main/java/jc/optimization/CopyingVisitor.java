package jc.optimization;

import petter.cfg.Procedure;
import petter.cfg.State;
import petter.cfg.edges.*;
import petter.cfg.expression.*;
import petter.cfg.expression.visitors.DefaultUpDownDFS;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class CopyingVisitor extends AbstractTransitionVisitor {

    Procedure procedure;

    List<Transition> oldTransitions;

    List<Transition> newTransitions;

    Map<State, State> oldNewStates;

    CopyingExprVisitor exprVisitor;

    CopyingVisitor(Procedure procedure) {
        this.procedure = procedure;

        this.oldTransitions = new ArrayList<>(procedure.getTransitions());

        this.newTransitions = new ArrayList<>();

        this.oldNewStates = new HashMap<>();

        this.exprVisitor = new CopyingExprVisitor();
    }

    @Override
    void visit(Assignment a) {
        Tuple<State, State> sd = getNewStates(a);

        Assignment newAss = new Assignment(sd.first, sd.second, copyExpr(a.getLhs()), copyExpr(a.getRhs()));

        this.newTransitions.add(newAss);
    }

    @Override
    void visit(GuardedTransition g) {
        Tuple<State, State> sd = getNewStates(g);

        GuardedTransition newGuard = new GuardedTransition(sd.first, sd.second, copyExpr(g.getAssertion()), g.getOperator());

        this.newTransitions.add(newGuard);
    }

    @Override
    void visit(Nop n) {
        Tuple<State, State> sd = getNewStates(n);

        Nop newNop = new Nop(sd.first, sd.second);

        this.newTransitions.add(newNop);
    }

    @Override
    void visit(ProcedureCall c) {
        Tuple<State, State> sd = getNewStates(c);

        ProcedureCall newCall = new ProcedureCall(sd.first, sd.second, copyExpr(c.getCallExpression()));

        this.newTransitions.add(newCall);
    }

    public ProcedureBody copyBody() {
        super.visit(this.oldTransitions);

        State newBegin = oldNewStates.get(procedure.getBegin());
        State newEnd = oldNewStates.get(procedure.getEnd());

        return new ProcedureBody(newTransitions, newBegin, newEnd);
    }

    private Tuple<State, State> getNewStates(Transition transition) {
        oldNewStates.putIfAbsent(transition.getSource(), new State());
        oldNewStates.putIfAbsent(transition.getDest(), new State());

        State newSource = oldNewStates.get(transition.getSource());
        State oldSource = oldNewStates.get(transition.getDest());

        return new Tuple<>(newSource, oldSource);
    }

    // todo: try to fix this mess
    private <T extends Expression> T copyExpr(T expr) {
        Optional<Expression> optCopied = expr.accept(exprVisitor, new UnknownExpression(expr.getType()));

        assert optCopied.isPresent();

        Expression copied = optCopied.get();

        assert copied.getClass() == expr.getClass();

        return (T) copied;
    }

    private class CopyingExprVisitor extends DefaultUpDownDFS<Expression> {
        @Override
        public Expression postVisit(FunctionCall m, Expression s, Stream<Expression> it) {
            return new FunctionCall(m.getName(), m.getType(), it.collect(Collectors.toList()));
        }

        @Override
        public Expression postVisit(BinaryExpression s, Expression lhs, Expression rhs) {
            return new BinaryExpression(lhs, s.getOperator(), rhs);
        }

        @Override
        public Expression postVisit(IntegerConstant s, Expression fromParent) {
            return new IntegerConstant(s.getIntegerConst());
        }

        @Override
        public Expression postVisit(StringLiteral s, Expression fromParent) {
            return new StringLiteral(s.getStringLiteral());
        }

        @Override
        public Expression postVisit(Variable s, Expression fromParent) {
            return new Variable(s.getId() * 1000, s.getName(), s.getType());
        }

        @Override
        public Expression postVisit(UnknownExpression s, Expression fromParent) {
            return new UnknownExpression(s.getType());
        }

        @Override
        public Expression postVisit(UnaryExpression s, Expression fromChild) {
            return new UnaryExpression(s.getExpression(), s.getOperator());
        }
    }
}
