package jc.cli;

import jc.CallGraph;
import jc.Util;
import jc.optimization.CallInliner;
import jc.optimization.RecursionOptimizer;
import petter.cfg.CompilationUnit;
import petter.cfg.Procedure;
import petter.simplec.Compiler;

import java.io.File;
import java.util.ArrayList;

// todo: use PicoCLI to create proper CLI interface
public class App {
    public static void inline() throws Exception {
        String filename = "inline_call_0.c";

        File file = new File("examples/" + filename);

        CompilationUnit compilationUnit = Compiler.parse(file);

        Procedure main = compilationUnit.getProcedure("main");

        Util.drawCFG(main, "graphs/" + filename + "_before");

        CallInliner inliner = new CallInliner(compilationUnit, 5, 10);

        inliner.inlineLeafFunctions();

        Util.drawCFG(main, "graphs/" + filename + "_after");
    }

    public static void unrollRecursion() throws Exception {
        String filename = "optimize_tail.c";

        File file = new File("examples/" + filename);

        CompilationUnit compilationUnit = Compiler.parse(file);

        CallGraph callGraph = new CallGraph(compilationUnit);

        Procedure recursive = new ArrayList<>(callGraph.getDirectlyRecursive()).get(0);

        Util.drawCFG(recursive, "graphs/" + filename);

        RecursionOptimizer optimizer = new RecursionOptimizer(compilationUnit, callGraph);

        optimizer.eliminateTailRecursion();

        optimizer.unrollRecursion(1);

        Util.drawCFG(recursive, "graphs/" + filename);
    }

    public static void optimizeTailCall() throws Exception {
        String filename = "optimize_tail.c";

        File file = new File("examples/" + filename);

        CompilationUnit compilationUnit = Compiler.parse(file);

        CallGraph callGraph = new CallGraph(compilationUnit);

        Procedure recursive = new ArrayList<>(callGraph.getDirectlyRecursive()).get(0);

        Util.drawCFG(recursive, "graphs/" + filename);

        RecursionOptimizer optimizer = new RecursionOptimizer(compilationUnit, callGraph);

        optimizer.eliminateTailRecursion();

        Util.drawCFG(recursive, "graphs/" + filename);
    }

    public static void createCallGraph() throws Exception {
        String filename = "call_graph.c";

        File file = new File("examples/" + filename);

        CompilationUnit compilationUnit = Compiler.parse(file);

        CallGraph callGraph = new CallGraph(compilationUnit);

        Util.drawCallGraph(callGraph, "graphs/" + filename);
    }

    public static void main(String[] args) {
        try {
            inline();
            unrollRecursion();
            optimizeTailCall();
            createCallGraph();
        } catch (Exception exception) {
            System.err.println("Oops, something went wrong.");
        }
    }
}
