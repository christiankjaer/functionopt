package jc.cli;

import jc.Util;
import jc.cli.helpers.InputBeforeAfterCommand;
import jc.optimization.CallInliner;
import petter.cfg.CompilationUnit;
import petter.cfg.Procedure;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "inline", aliases = {"in"}, description = "Inline function calls.")
public class InlineCallCommand extends InputBeforeAfterCommand {
    @Option(names = {"-s", "--size-limit"}, paramLabel = "<int>", description = "Max. number of transitions a function can have to be inlined.")
    private Integer sizeLimit = Integer.MAX_VALUE;

    @Option(names = {"-c", "--call-limit"}, paramLabel = "<int>", description = "Max. number of calls a function can receive to be inlined.")
    private Integer callLimit = Integer.MAX_VALUE;

    protected void doWork(CompilationUnit compilationUnit) throws Exception {
        if (beforeDir != null) {
            for (Procedure procedure : Util.getRelevantProcedures(compilationUnit)) {
                Util.drawCFG(procedure, beforeDir.getCanonicalPath() + "/" + procedure.getName());
            }
        }

        new CallInliner(compilationUnit, callLimit, sizeLimit).inlineLeafFunctions();

        for (Procedure procedure : Util.getRelevantProcedures(compilationUnit)) {
            Util.drawCFG(procedure, afterDir.getCanonicalPath() + "/" + procedure.getName());
        }
    }
}
