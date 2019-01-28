package petter.cfg.edges;
import petter.cfg.expression.Expression;
import petter.cfg.expression.Operator;
import petter.cfg.State;
import petter.cfg.Visitor;
import petter.cfg.PropagatingVisitor;
/**
 * represents an AssertionEdge in the CFG
 */
public class GuardedTransition extends Transition {
    private Expression assertion;
    private Operator op;
 /**
     * construct an new CFGAssertionEdge
     * @param src  start of edge
     * @param dst    end of edge
     * @param assertion    expression of assertion
     * @param op   operator of assertion
     */
    public GuardedTransition(State src, State dst, Expression assertion, Operator op){
	super(src,dst);
	this.assertion=assertion;
	this.op=op;
    }
    /**
     * string representation of the Assertion
     * @return guess what?
     */
    public String toString() {
        return "("+assertion+op+"0)";
    }
    /**
     * obtain Assertion 
     * @return an Expression
     */
    public Expression getAssertion() {
        return assertion;
    }
    /**
     * obtain Operator of the Assertion
     * @return an Operator
     */
    public Operator getOperator(){
	return op;
    }

    public void setAssertion(Expression ex){
	assertion = ex;
    }

    // interface Analyzable:
    public void forwardAccept(Visitor v){
	if (v.visit(this)) v.enter(dest);
    }
    public void backwardAccept(Visitor v){
	if (v.visit(this)) v.enter(source);
    }
    public <T>void forwardAccept(PropagatingVisitor<T> v, T d){
	if ((d=v.visit(this,d))!=null) v.enter(dest,d);
    }
    public <T>void backwardAccept(PropagatingVisitor<T> v, T d){
	if ((d=v.visit(this,d))!=null) v.enter(source,d);
    }
    // interface Analyzable end
}
