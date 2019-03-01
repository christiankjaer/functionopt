package petter.cfg.edges;
import petter.cfg.PropagatingVisitor;
import petter.cfg.Analyzable;
import petter.cfg.State;
import petter.cfg.Visitor;
import java.util.Map;
import java.util.HashMap;
/**
 * represents an edge in a CFG
 * abstract class for all kinds of CFGEdge
 */
public abstract class Transition implements java.io.Serializable, Analyzable{
    protected State source;
    protected State dest;
//    protected SymbolTable symtab;
    /**
     * Arbitrary annotations identified by key.
     */
    private Map<Object, Object> annotations;
    public Object getAnnotation(Object key) {
        if (annotations == null) return null;
        return annotations.get(key);
    }
    public Object putAnnotation(Object key, Object value) {
        if (annotations == null)
            annotations = new HashMap<Object, Object>();
        return annotations.put(key, value);
    }
    public <T> T getAnnotation(Class<T> key) throws ClassCastException {
        if (annotations == null) return null;
        return key.cast(annotations.get(key));
    }
    public <T> T putAnnotation(Class<T> key, T value) throws ClassCastException {
        if (annotations == null)
            annotations = new HashMap<Object, Object>();
        return key.cast(annotations.put(key, value));
    }
    public Map<Object, Object> getAnnotations() {
        return annotations;
    }
    public void putAnnotations(Map<?, ?> a) {
        annotations.putAll(a);
    }

    /**
     * construct an new CFGEdge, characterized by start and end state
     * @param source  start of edge
     * @param dest    end of edge
     */
    public Transition(State source,State dest){
	this.source=source;
        source.setEnd(false);
	this.dest=dest;
        dest.setBegin(false);
	if (source!=null){
	    source.addOutEdge(this);
	    dest.addInEdge(this);
	}
//	symtab=dest.getSymtab();
    }
    // access to members...
    /**
     * remove Edge
     */
    public void removeEdge(){
	source.deleteOutEdge(this);
	dest.deleteInEdge(this);
    }
    /**
     * obtain the source state 
     * @return guess what?
     */
    public State getSource(){
	return source;
    }
    /**
     * obtain the dest state 
     * @return guess what?
     */
    public State getDest(){
	return dest;
    }
    /**
     * string representation of this edge.
     * @return id of source state & id of dest state
     */
    public String toString(){
	return "Edge: "+source.getId()+" -> "+dest.getId();
    }
    /**
     * set dest state
     */
    public void setDest(State state) {
	dest = state;
	dest.addInEdge(this);

    }
    /**
     * hashCode is similar to each edge from same source to same target
     */
    @Override
    public int hashCode(){
        return (source.hashCode() * dest.hashCode()) / 13;
    }

    /**
     *
     */
    @Override
    public boolean equals(Object o){
        if (o instanceof Transition)
        {
            Transition e2 = (Transition)o;
            return (source.equals(e2.source) && dest.equals(e2.dest));
        }
        return false;
    }

    /**
     * set source state
     */
    public void setSource(State state) {
	source = state;
	source.addOutEdge(this);
    }
    // end of access methds

    // interface Analyzable:
    public abstract void forwardAccept(Visitor v);
    public abstract void backwardAccept(Visitor v);
    public abstract <T>void forwardAccept(PropagatingVisitor<T> v, T d);
    public abstract <T>void backwardAccept(PropagatingVisitor<T> v, T d);
    // end of interface Analyzable
}
