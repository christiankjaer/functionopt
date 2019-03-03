package jc;

import petter.cfg.expression.BinaryExpression;
import petter.cfg.expression.FunctionCall;
import petter.cfg.expression.visitors.DefaultUpDownDFS;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

// todo: consider merging with the other visitors
public class CallCollector extends DefaultUpDownDFS<Set<FunctionCall>> {
    @Override
    public Set<FunctionCall> postVisit(FunctionCall m, Set<FunctionCall> s, Stream<Set<FunctionCall>> it) {
        Set<FunctionCall> result = new HashSet<>();
        result.add(m);
        result.addAll(s);
        it.forEach(result::addAll);
        return result;
    }

    @Override
    public Set<FunctionCall> postVisit(BinaryExpression s, Set<FunctionCall> lhs, Set<FunctionCall> rhs) {
        Set<FunctionCall> result = new HashSet<>();
        result.addAll(lhs);
        result.addAll(rhs);
        return result;
    }
}
