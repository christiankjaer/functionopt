package jc.optimization;

import petter.cfg.*;
import petter.cfg.edges.*;
import petter.cfg.expression.Expression;
import petter.cfg.expression.FunctionCall;
import petter.cfg.expression.Variable;
import petter.simplec.Compiler;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class Optimization {

    CompilationUnit compilationUnit;

    CallGraph callGraph;

    StringGenerator generator;

    public Optimization(CompilationUnit compilationUnit) {
        this.compilationUnit = compilationUnit;

        this.callGraph = new CallGraph(compilationUnit);

        this.generator = new StringGenerator();
    }

    public void inline() {
        for (Procedure procedure : compilationUnit) {
            Set<Procedure> calledLeafProcedures = callGraph
                    .getNode(procedure)
                    .getCallees()
                    .stream()
                    .filter(CallGraph.Node::isLeaf)
                    .map(CallGraph.Node::getProcedure)
                    .collect(Collectors.toSet());

            for (Transition transition : procedure.getTransitions()) {
                if (transition instanceof Assignment) {
                    Assignment assignment = (Assignment) transition;

                    if (assignment.getRhs() instanceof FunctionCall) {
                        FunctionCall call = ((FunctionCall) assignment.getRhs());

                        for (Procedure candidate : calledLeafProcedures) {
                            if (candidate.getName().equals(call.getName())) {
                                inlineFunction(assignment, call, procedure, candidate);
                                break;
                            }
                        }
                    }
                }

                if (transition instanceof ProcedureCall) {
                    FunctionCall callExpression = ((ProcedureCall) transition).getCallExpression();

                    for (Procedure candidate : calledLeafProcedures) {
                        if (candidate.getName().equals(callExpression.getName())) {
                            inlineProcedure(transition, callExpression, procedure, candidate, generatePrefix());
                            break;
                        }
                    }
                }
            }
        }
    }

    private void inlineFunction(Assignment assignment, FunctionCall call, Procedure caller, Procedure callee) {
        String variablePrefix = generatePrefix();

        inlineProcedure(assignment, call, caller, callee, variablePrefix);

        State callEnd = assignment.getDest();

        List<Transition> incoming = StreamSupport.stream(callEnd.getIn().spliterator(), false).collect(Collectors.toList());

        List<State> predecessors = incoming.stream().map(Transition::getSource).collect(Collectors.toList());

        assert predecessors.size() == 1;

        incoming.forEach(Transition::removeEdge);

        Expression returnedVariable = new Variable(1001, variablePrefix + "return", assignment.getRhs().getType());

        new Assignment(predecessors.get(0), callEnd, assignment.getLhs(), returnedVariable);

        caller.refreshStates();

        drawGraph(caller, "caller_inlined");
    }

    private void inlineProcedure(Transition transition, FunctionCall expression, Procedure caller, Procedure callee, String variablePrefix) {
        drawGraph(caller, "caller");
        drawGraph(callee, "callee");

        State callBegin = transition.getSource();
        State callEnd = transition.getDest();

        State calleeEnter = callee.getBegin();
        State calleeExit = callee.getEnd();

        transition.removeEdge();

        callBegin = transformArgumentsToVariables(expression, callee, callBegin, variablePrefix);

        renameCalleeVariables(variablePrefix, calleeEnter);

        new Nop(callBegin, calleeEnter);
        new Nop(calleeExit, callEnd);

        caller.refreshStates();

        drawGraph(caller, "caller_inlined");
    }

    private State transformArgumentsToVariables(FunctionCall expression, Procedure callee, State callBegin, String prefix) {
        List<Expression> arguments = expression.getParamsUnchanged();
        List<String> parameters = callee
                .getFormalParameters()
                .stream()
                .map(integer -> compilationUnit.getVariableName(integer))
                .collect(Collectors.toList());

        assert arguments.size() == parameters.size();

        for (int i = 0; i < arguments.size(); i++) {
            State newCallBegin = new State();

            Expression argument = new Variable(1000, prefix + parameters.get(i), arguments.get(i).getType());

            new Assignment(callBegin, newCallBegin, argument, arguments.get(i));

            callBegin = newCallBegin;
        }

        return callBegin;
    }

    private void renameCalleeVariables(String variablePrefix, State calleeEnter) {
        RenamingVisitor visitor = new RenamingVisitor(variablePrefix);
        calleeEnter.forwardAccept(visitor, true);
        visitor.fullAnalysis();
    }

    private String generatePrefix() {
        return "__in_" + generator.generate(3) + "_";
    }

    private static void drawGraph(Procedure procedure, String name) {
        try {
            new DotLayout("png", name + ".png").callDot(procedure);
        } catch (Exception e) {
            System.err.println("Could not create dot file.");
        }
    }

    public static void main(String[] args) throws Exception {
        CompilationUnit compilationUnit = Compiler.parse(new File("examples/procedure.c"));

        Optimization optimization = new Optimization(compilationUnit);

        optimization.inline();
    }
}
