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

@Command(name = "opt")
public class App implements Runnable {

    @Parameters(index = "0", type = Analysis.class, description = "Analysis and transformation to perform. One of CallGraph, Inline, TailCall and Unroll")
    private Analysis analysis;

    @Parameters(index = "1", description = "Input source file to analyze")
    private String sourceFile;

    @Option(names = {"-cg", "--callgraph"}, description = "Name of output file for the call graph")
    private String callGraphName;

    @Option(names = {"-b", "--before"}, description = "Name of output file for the CFG before tranformation")
    private String beforeCFG;

    @Option(names = {"-a", "--after"}, description = "Name of output file for the CFG after tranformation")
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
            createCallGraph(compilationUnit);

            switch (analysis) {
                case CallGraph:
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
