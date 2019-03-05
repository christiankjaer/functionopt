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

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

enum Analysis {
    CallGraph, Inline, TailCall, Unroll
}

@Command(name = "functionopt")
public class App implements Runnable {

    @Parameters(index = "0", type = Analysis.class)
    private Analysis analysis;

    @Parameters(index = "1")
    private String sourceFile;

    @Option(names = {"-cg", "--callgraph"})
    private String callGraphName;

    @Option(names = {"-b", "--before"})
    private String beforeCFG;

    @Option(names = {"-a", "--after"})
    private String afterCFG;

    @Option(names = {"--inline-code-limit"}, description = "Size of largest procedure to inline")
    private int inlineCodeSize = Integer.MAX_VALUE;

    @Option(names = {"--inline-call-limit"}, description = "Static call limit for inliner")
    private int inlineCallLimit = Integer.MAX_VALUE;

    @Option(names = {"--recursion-unroll-limit"}, description = "Number of recursive calls to unroll")
    private int recursionUnrollLimit = 1;


    public void run()  {
        System.out.println("Running...");

        try {
            File source = new File(sourceFile);

            CompilationUnit compilationUnit = Compiler.parse(source);

            switch (analysis) {
                case CallGraph:
                    createCallGraph(compilationUnit);
                    break;
                case Inline:
                    inline(compilationUnit);
                    break;
                case TailCall:
                    optimizeTailCall(compilationUnit);
                    break;
                case Unroll:
                    unrollRecursion(compilationUnit);
                    break;
            }
        } catch (Exception exception) {
            System.err.println("Oops, something went wrong.");
        }

    }

    private void inline(CompilationUnit compilationUnit) {

        Procedure main = compilationUnit.getProcedure("main");

        if (beforeCFG != null)
            Util.drawCFG(main, beforeCFG);

        System.out.println(inlineCallLimit);

        CallInliner inliner = new CallInliner(compilationUnit, inlineCallLimit, inlineCodeSize);

        inliner.inlineLeafFunctions();

        if (afterCFG != null)
            Util.drawCFG(main, afterCFG);
    }

    private void unrollRecursion(CompilationUnit compilationUnit) {

        CallGraph callGraph = new CallGraph(compilationUnit);

        Procedure recursive = new ArrayList<>(callGraph.getDirectlyRecursive()).get(0);

        if (beforeCFG != null)
            Util.drawCFG(recursive, beforeCFG);

        RecursionOptimizer optimizer = new RecursionOptimizer(compilationUnit, callGraph);

        optimizer.eliminateTailRecursion();

        optimizer.unrollRecursion(recursionUnrollLimit);

        if (afterCFG != null)
            Util.drawCFG(recursive, afterCFG);
    }

    private void optimizeTailCall(CompilationUnit compilationUnit) {

        CallGraph callGraph = new CallGraph(compilationUnit);

        Procedure recursive = new ArrayList<>(callGraph.getDirectlyRecursive()).get(0);

        if (beforeCFG != null)
            Util.drawCFG(recursive, beforeCFG);

        RecursionOptimizer optimizer = new RecursionOptimizer(compilationUnit, callGraph);

        optimizer.eliminateTailRecursion();

        if (afterCFG != null)
            Util.drawCFG(recursive, afterCFG);
    }

    private void createCallGraph(CompilationUnit compilationUnit) throws Exception {

        CallGraph callGraph = new CallGraph(compilationUnit);

        if (callGraph != null)
            Util.drawCallGraph(callGraph, callGraphName);
    }

    public static void main(String[] args) {
        CommandLine.run(new App(), args);

    }
}
