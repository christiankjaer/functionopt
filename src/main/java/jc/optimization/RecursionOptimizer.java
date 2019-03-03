package jc.optimization;

import jc.CallGraph;
import petter.cfg.CompilationUnit;
import petter.cfg.Procedure;
import petter.simplec.Compiler;

import java.io.File;

public class RecursionOptimizer {

    CompilationUnit compilationUnit;

    CallGraph callGraph;

    public RecursionOptimizer(CompilationUnit compilationUnit) {
        this.compilationUnit = compilationUnit;

        this.callGraph = new CallGraph(compilationUnit);
    }

    public void eliminateTailRecursion() {
        for (Procedure procedure : callGraph.getDirectlyRecursiveProcedures()) {
            System.out.println("Eliminating tail recursion in " + procedure.getName());
        }
    }

    public static void main(String[] args) throws Exception {
        CompilationUnit compilationUnit = Compiler.parse(new File("examples/recursion.c"));

        RecursionOptimizer optimizer = new RecursionOptimizer(compilationUnit);

        optimizer.eliminateTailRecursion();
    }
}
