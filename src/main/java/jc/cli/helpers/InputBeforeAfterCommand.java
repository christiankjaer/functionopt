package jc.cli.helpers;

import picocli.CommandLine.Option;

import java.io.File;
import java.io.IOException;

public abstract class InputBeforeAfterCommand extends InputCommand {
    @Option(names = {"-b", "--before"}, paramLabel = "<dir>", description = "Output directory for CFGs of the original input.")
    protected File beforeDir;

    @Option(names = {"-a", "--after"}, paramLabel = "<dir>", required = true, description = "Output directory for CFGs of the optimized input.")
    protected File afterDir;

    protected void checkArgs() throws IOException {
        super.checkArgs();

        if (!afterDir.exists() && !afterDir.mkdir()) {
            throw new IOException("The 'after' directory does not exist and could not be created.");
        }

        if (!afterDir.isDirectory()) {
            throw new IOException("The 'after' argument does not name a directory.");
        }

        if (beforeDir != null) {
            if (!beforeDir.exists() && !beforeDir.mkdir()) {
                throw new IOException("The 'before' directory does not exist and could not be created.");
            }

            if (!beforeDir.isDirectory()) {
                throw new IOException("The 'before' argument does not name a directory.");
            }
        }
    }
}
