package jc.cli;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "opt", subcommands = {
        DrawCallGraphCommand.class,
        InlineCallCommand.class,
        OptimizeTailCallCommand.class,
        UnrollRecursionCommand.class
})
public class App implements Runnable {
    public static void main(String[] args) {
        new CommandLine(new App())
                .setUsageHelpWidth(120)
                .parseWithHandler(new CommandLine.RunLast(), args);
    }

    @Override
    public void run() {
        // if we got here, none of the registered sub-commands were recognized
        new CommandLine(this).usage(System.err);
    }
}
