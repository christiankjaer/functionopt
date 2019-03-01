package petter.cfg;

import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import petter.cfg.edges.Transition;
import petter.cfg.edges.Nop;
import petter.cfg.Procedure;
/**
 * represents a program state in a CFG.
 * @author Michael Petter
 */
public class State implements java.io.Serializable, Analyzable{
    public static long statecounter = 0;
    private boolean isBegin = false;
    private boolean isEnd = false;
    private boolean isLoop = false;
    private long statenumber;
    private Procedure method;
    private List<Transition> inEdges = new ArrayList<Transition>();
    private List<Transition> outEdges = new ArrayList<Transition>();
//    private SymbolTable symtab;

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
     *  get method for state
     * remember to call setMethod before
     * @return method to which state belongs
     */
    public Procedure getMethod(){
	assert method !=null : "method not initialisied - call setMethod before";
	return method;
    }
    public void setBegin(boolean b){
        isBegin=b;
    }
    public void setEnd(boolean b){
        isEnd = b;
    }
    public boolean isBegin(){
        return isBegin;
    }
    public boolean isEnd(){
        return isEnd;
    }

    public int getInDegree(){
	return inEdges.size();
    }
    public int getOutDegree(){
	return outEdges.size();
    }

    /**
     *  set method for state
     * @param methode method to which the state belongs
     */
    public void setProcedure(Procedure methode){
	this.method=methode;
    }

    // modelling of the CFGs structure with these methods:
    // also grant convenient access to members...
    /**
     * add an edge leading to another State
     * @param newEdge is the new outgoing edge
     */
    public void addOutEdge(Transition newEdge){
	outEdges.add(newEdge);
    }
    /**
     * add an edge coming from another State
     * @param newEdge is the new ingoing edge
     */
    public void addInEdge(Transition newEdge){
	inEdges.add(newEdge);
    }
    /**
     * add an edge leading to another State
     * @param oldEdge is the old outgoing edge
     */
    public void deleteOutEdge(Transition oldEdge){
	outEdges.remove(oldEdge);
    }
    /**
     * add an edge coming from another State
     * @param oldEdge is the old ingoing edge
     */
    public void deleteInEdge(Transition oldEdge){
	inEdges.remove(oldEdge);
    }
    
    public Iterable<Transition> getIn() {
	return inEdges;
    }
    public Iterable<Transition> getReverseIn() {
	Transition [] e = new Transition[1];
	e = inEdges.toArray(e);
	List<Transition> list = new ArrayList<Transition>(0);
	for(int i= e.length-1; i>= 0; i--)
	    list.add(e[i]);
	return list;
    }
   public Iterable<Transition> getOut() {
	return outEdges;
    }
   public Iterable<Transition> getReverseOut() {
       Transition [] e = new Transition[1];
       e = outEdges.toArray(e);
       List<Transition> list = new ArrayList<Transition>(0);
       for(int i=e.length-1; i>=0;  i--)
	   if(e[i] != null) list.add(e[i]);
       return list;
   }


    /**
     * a means to access all incoming edges
     * @return a <code>Iterator<CFGEdge></code> of incoming edges
     */
    public Iterator<Transition> getInIterator() {
	return inEdges.iterator();
    }
    /**
     * a means to access all outgoing edges
     * @return a <code>Iterator<CFGEdge></code> of outgoing edges
     */
    public Iterator<Transition> getOutIterator() {
	return outEdges.iterator();
    }
//    /**
//     * obtain the SymbolTable, valid at this program state
//     * @return guess what?
//     */
//    public SymbolTable getSymtab() {
//        return symtab;
//    }
//    private void setSymtab(SymbolTable symtab) {
//        this.symtab = symtab;
//    }
    /**
     * when it turns out, that this state is a loop separator, set the appropriate flag
     * @param isLoop <code>true</code>, when this state is a loop separator
     */
    public void setLoopSeparator(boolean isLoop){
	this.isLoop = isLoop;
    }
    /**
     * this state could be a loop separator
     * @return <code>true</code>, when this state is a loop separator
     */
    public boolean isLoopSeparator(){
	return isLoop;
    }
    // interface Analyzable:
    public void forwardAccept(Visitor v){
	if (!v.visit(this)) return;
	Iterator<Transition> it = outEdges.iterator();
	while (it.hasNext()) {
	    v.enter(it.next());
	}
    }
    public void backwardAccept(Visitor v){
	if (!v.visit(this)) return;
	Iterator<Transition> it = inEdges.iterator();
	while (it.hasNext()) {
	    v.enter(it.next());
	}
    }
    public <T>void forwardAccept(PropagatingVisitor<T> v,T d){
	if ((d = v.visit(this,d)) == null) return;
	Iterator<Transition> it = outEdges.iterator();
	while (it.hasNext()) {
	    v.enter(it.next(),d);
	}
    }
    public <T>void backwardAccept(PropagatingVisitor<T> v,T d){
	if ((d = v.visit(this,d)) == null) return;
	Iterator<Transition> it = inEdges.iterator();
	while (it.hasNext()) {
	    v.enter(it.next(),d);
	}
    }
    // end of interface Analyzable

    /**
     * construction of a new CFG State. 
     * You have to provide a Symtab corresponding to the valid identifiers at this programpoint
     * @param symtab a {@link SymbolTable} with the actually valid identifiers
     */
    public State(
//        SymbolTable symtab
        ){
//	this.symtab=symtab;
	// Initialize a Groebner Base with the statenumber as identifier
	statenumber= ++statecounter;
    }
    /**
     * numerical ID of a CFG node / program state
     * @return guess what?
     */
    public long getId(){
	return statenumber;
    }

    /**
     * compares two CFGStates 
     * @return <code>true</code>, when the two CFGStates have the same ID
     */
    @Override
    public boolean equals(Object otherobj) {
	if (!(otherobj instanceof State)) return false;
	if (((State)otherobj).getId()==this.getId() ) return true;
	return false;
    }
    /**
     * string representation of this program state.
     * @return just State: and the ID
     */
    @Override
    public String toString(){
	return "State: "+getId();
	//String retval =  "State: s"+getId()+"("+outEdges.size()+" children):\n";
	//Iterator it = outEdges.iterator();
	//while (it.hasNext()) retval+=it.next().toString()+"\n";
	//return retval;
    }
    
    // is not used any more now
    // compression; i hope, this one does no damage!
    private void compress(HashSet set){
	if ((inEdges.size()!=1)||(outEdges.size()!=1)) {
	    // Do nothing
	} 
	else {
	    if (inEdges.get(0) instanceof Nop ) {
		State predecessor = inEdges.get(0).getSource();
		State follower = outEdges.get(0).getDest();
		outEdges.get(0).setSource(predecessor);
		predecessor.outEdges.remove(inEdges.get(0));
		predecessor.outEdges.add(outEdges.get(0));
		set.remove(this);
		follower.compress(set);
		return;
	    } 
	    else if (outEdges.get(0) instanceof Nop ) {
		State predecessor = inEdges.get(0).getSource();
		State follower = outEdges.get(0).getDest();
		inEdges.get(0).setDest(follower);
		follower.inEdges.remove(outEdges.get(0));
		follower.inEdges.add(inEdges.get(0));
		set.remove(this);
		predecessor.compress(set);
		return;
	    }
	} 
	Object []array = outEdges.toArray();
	for (int i = 0; i< array.length;i++){
	    State follower = ((Transition)array[i]).getDest();
	    if (follower.getId()>getId())	follower.compress(set);
	}
    }
    @Override
    public int hashCode(){
        return (new Long(statenumber)).hashCode();
    }
}
