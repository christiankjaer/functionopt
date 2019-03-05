# Function inlining

## Requirements

* Java Development Kit (JDK) 8+
* Maven 2+

## Build 

    mvn package

## Usage

    java -jar target/opt.jar 
    
```
Usage: opt [--inline-call-limit=<inlineCallLimit>]
           [--inline-code-limit=<inlineCodeSize>]
           [--recursion-unroll-limit=<recursionUnrollLimit>]
           [-a=<afterCFG>] [-b=<beforeCFG>] [-cg=<callGraphName>]
               <analysis> <sourceFile>
               
      <analysis>             Analysis and transformation to perform. One of
                               CallGraph, Inline, TailCall and Unroll
      <sourceFile>           Input source file to analyze
      --inline-call-limit=<inlineCallLimit>
                             Static call limit for inliner
      --inline-code-limit=<inlineCodeSize>
                             Size of largest procedure to inline
      --recursion-unroll-limit=<recursionUnrollLimit>
                             Number of recursive calls to unroll
  -a, --after=<afterCFG>     Name of output file for the CFG after tranformation
  -b, --before=<beforeCFG>   Name of output file for the CFG before tranformation
      -cg, --callgraph=<callGraphName>
                             Name of output file for the call graph

```
    


