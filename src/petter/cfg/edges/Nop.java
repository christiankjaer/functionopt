package petter.cfg.edges;
import petter.cfg.State;
import petter.cfg.Visitor;
import petter.cfg.PropagatingVisitor;
/**
 * represents a NullEdge in the CFG, which has no effects
 */
public class Nop extends Transition {
/**
     * construct an new CFGAssertionEdge
     * @param source  start of edge
     * @param dest    end of edge
     */
    public Nop(State source, State dest) {
	super(source, dest);
    }
    /**
     * string representation of the NullEdge
     * @return skip;
     */
    public String toString(){
	return "skip;";
    }
    // interface Analyzable:
    public void forwardAccept(Visitor v){
	if (v.visit(this)) v.enter(dest);
    }
    public void backwardAccept(Visitor v){
	if (v.visit(this)) v.enter(source);
    }
    public <T>void forwardAccept(PropagatingVisitor<T> v,T d){
	if ((d=v.visit(this,d))!=null) v.enter(dest,d);
    }
    public <T>void backwardAccept(PropagatingVisitor<T> v, T d){
	if ((d=v.visit(this,d))!=null) v.enter(source,d);
    }
    // interface Analyzable end
}
