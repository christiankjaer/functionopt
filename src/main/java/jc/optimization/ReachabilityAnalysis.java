package jc.optimization;

import petter.cfg.*;
import petter.cfg.edges.ProcedureCall;

import java.io.File;

public class ReachabilityAnalysis extends AbstractPropagatingVisitor<Boolean> {

    static Boolean lub(Boolean b1, Boolean b2) {
        if (b1 == null) return b2;
        if (b2 == null) return b1;
        return b1 || b2;
    }

    static boolean leq(Boolean b1, Boolean b2) {
        if (b1 == null) return true;
        if (b2 == null) return false;
        return !b1 || b2;
    }

    CompilationUnit cu;

    public ReachabilityAnalysis(CompilationUnit cu) {
        super(true);
        this.cu = cu;
    }

    @Override
    public Boolean visit(ProcedureCall ae, Boolean d) {
        enter(cu.getProcedure(ae.getCallExpression().getName()), true);
        return d;
    }

    public Boolean visit(State s, Boolean newflow) {
        Boolean oldFlow = dataflowOf(s);
        if (!leq(newflow, oldFlow)) {
            Boolean newVal = lub(oldFlow, newflow);
            dataflowOf(s, newVal);
            return newVal;
        }
        return null;
    }

    public static void main(String[] args) throws Exception {
        CompilationUnit cu = petter.simplec.Compiler.parse(new File("input.c"));
        ReachabilityAnalysis ra = new ReachabilityAnalysis(cu);
        Procedure foo = cu.getProcedure("main");
        DotLayout layout = new DotLayout("png", "main.png");
        ra.enter(foo, true);
        ra.fullAnalysis();
        for (State s : foo.getStates()) {
            layout.highlight(s, ra.dataflowOf(s).toString());
        }

    }
}
