package jc.cli.helpers;

import petter.cfg.CompilationUnit;
import petter.simplec.Compiler;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.io.IOException;

public abstract class InputCommand implements Runnable {
    @Parameters(index = "0", paramLabel = "<input>", description = "Input SimpleC source file.")
    protected File inputFile;

    @Override
    public void run() {
        try {
            checkArgs();
        } catch (IOException exception) {
            System.err.println(exception.getMessage());
            return;
        }

        CompilationUnit compilationUnit;

        try {
            compilationUnit = Compiler.parse(inputFile);
        } catch (Exception exception) {
            System.err.println("Could not parse the input file.");
            return;
        }

        try {
            doWork(compilationUnit);
        } catch (Exception exception) {
            System.err.println("An unexpected error occurred.");
        }
    }

    protected void checkArgs() throws IOException {
        if (!inputFile.exists()) {
            throw new IOException("The specified input file does not exist.");
        }

        if (!inputFile.isFile()) {
            throw new IOException("The specified input is not a file.");
        }
    }

    protected abstract void doWork(CompilationUnit compilationUnit) throws Exception;
}
