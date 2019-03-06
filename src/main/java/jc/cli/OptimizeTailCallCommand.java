package jc.cli;

import jc.CallGraph;
import jc.Util;
import jc.cli.helpers.InputBeforeAfterCommand;
import jc.optimization.RecursionOptimizer;
import petter.cfg.CompilationUnit;
import petter.cfg.Procedure;
import picocli.CommandLine.Command;

@Command(name = "tail-call", aliases = {"tc"}, description = "Optimize tail calls.")
public class OptimizeTailCallCommand extends InputBeforeAfterCommand {
    protected void doWork(CompilationUnit compilationUnit) throws Exception {
        if (beforeDir != null) {
            for (Procedure procedure : Util.getRelevantProcedures(compilationUnit)) {
                Util.drawCFG(procedure, beforeDir.getCanonicalPath() + "/" + procedure.getName());
            }
        }

        new RecursionOptimizer(compilationUnit, new CallGraph(compilationUnit)).eliminateTailRecursion();

        for (Procedure procedure : Util.getRelevantProcedures(compilationUnit)) {
            Util.drawCFG(procedure, afterDir.getCanonicalPath() + "/" + procedure.getName());
        }
    }
}
