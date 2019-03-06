package jc.cli;

import jc.CallGraph;
import jc.Util;
import jc.cli.helpers.InputCommand;
import petter.cfg.CompilationUnit;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.io.IOException;

@Command(name = "call-graph", aliases = {"cg"}, description = "Create call graph.")
public class DrawCallGraphCommand extends InputCommand {
    @Option(names = {"-o", "--output"}, paramLabel = "<file>", required = true, description = "The output file.")
    private File outputFile;

    protected void doWork(CompilationUnit compilationUnit) throws Exception {
        CallGraph callGraph = new CallGraph(compilationUnit);

        Util.drawCallGraph(callGraph, outputFile.getCanonicalPath());
    }

    protected void checkArgs() throws IOException {
        super.checkArgs();

        if (!outputFile.exists() && !outputFile.createNewFile()) {
            throw new IOException("The output file does not exist and cannot be created.");
        }

        if (outputFile.exists() && outputFile.isDirectory()) {
            throw new IOException("The output file cannot be a directory.");
        }
    }
}
