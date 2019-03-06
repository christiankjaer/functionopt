package jc.cli;

import jc.CallGraph;
import jc.Util;
import jc.cli.helpers.InputBeforeAfterCommand;
import jc.optimization.RecursionOptimizer;
import petter.cfg.CompilationUnit;
import petter.cfg.Procedure;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "unroll", aliases = {"un"}, description = "Unrolls recursion.")
public class UnrollRecursionCommand extends InputBeforeAfterCommand {
    @Option(names = {"-l", "--limit"}, paramLabel = "<int>", description = "Max. number of calls to unroll in one function.")
    private Integer limit = 1;

    protected void doWork(CompilationUnit compilationUnit) throws Exception {
        if (beforeDir != null) {
            for (Procedure procedure : Util.getRelevantProcedures(compilationUnit)) {
                Util.drawCFG(procedure, beforeDir.getCanonicalPath() + "/" + procedure.getName());
            }
        }

        new RecursionOptimizer(compilationUnit, new CallGraph(compilationUnit)).unrollRecursion(limit);

        for (Procedure procedure : Util.getRelevantProcedures(compilationUnit)) {
            Util.drawCFG(procedure, afterDir.getCanonicalPath() + "/" + procedure.getName());
        }
    }
}
