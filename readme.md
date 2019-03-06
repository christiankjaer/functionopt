# Function Call Optimization

Function call optimization tool for SimpleC.

## Requirements

* Java Development Kit (JDK) 8+
* Maven
* Graphviz

## Build 

Run the following command in the root of this repository:

    mvn package

## Usage

Use Java to run the archive:

    java -jar target/opt.jar 

Specify what do you want to do:

    Usage: opt [COMMAND]
    Commands:
      call-graph, cg  Create call graph.
      inline, in      Inline function calls.
      tail-call, tc   Optimize tail calls.
      unroll, un      Unrolls recursion.
      
### Call Graph

    Usage: opt call-graph -o=<file> <input>
    Create call graph.
          <input>           Input SimpleC source file.
      -o, --output=<file>   The output file.

### Inline Calls

    Usage: opt inline -a=<dir> [-b=<dir>] [-c=<int>] [-s=<int>] <input>
    Inline function calls.
          <input>              Input SimpleC source file.
      -a, --after=<dir>        Output directory for CFGs of the optimized input.
      -b, --before=<dir>       Output directory for CFGs of the original input.
      -c, --call-limit=<int>   Max. number of calls a function can receive to be inlined.
      -s, --size-limit=<int>   Max. number of transitions a function can have to be inlined.
      
### Optimize Tail Calls

    Usage: opt tail-call -a=<dir> [-b=<dir>] <input>
    Optimize tail calls.
          <input>          Input SimpleC source file.
      -a, --after=<dir>    Output directory for CFGs of the optimized input.
      -b, --before=<dir>   Output directory for CFGs of the original input.
      
### Unroll Recursion

    Usage: opt unroll -a=<dir> [-b=<dir>] [-l=<int>] <input>
    Unrolls recursion.
          <input>          Input SimpleC source file.
      -a, --after=<dir>    Output directory for CFGs of the optimized input.
      -b, --before=<dir>   Output directory for CFGs of the original input.
      -l, --limit=<int>    Max. number of calls to unroll in one function.

## Libraries

This project uses:
* [SimpleC front-end](https://versioncontrolseidl.in.tum.de/petter/simpleC) from Dr. Michael Petter of Technical University of Munich,
* [picocli](https://picocli.info/) for command-line parsing.

## Authors

* Jan Svoboda
* Christian Kjær Larsen
