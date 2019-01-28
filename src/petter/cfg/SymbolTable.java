package petter.cfg;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Collection;
import java.util.Map;
/**
 * private IDGenerator for the handled internal variables
 * @author Michael Petter
 */
class IDGenerator implements Serializable{
    private static int id;
    static {
	id = 0;
    }
    static int create(){
	assert id < Integer.MAX_VALUE;
	return id++;
    }
    static void reset(){
        id=0;
    }
}
/**
 * is used to store declaration information for identifiers occuring in expressions.
 * SymbolTable Objects are stored in {@link CFGState}s and {@link de.tum.in.wwwseidl.programanalysis.cfg.edges.CFGAssignmentEdge}s or {@link de.tum.in.wwwseidl.programanalysis.cfg.edges.CFGAssertionEdge}s.
 * @see CFGState
 * @see de.tum.in.wwwseidl.programanalysis.cfg.edges.CFGAssignmentEdge
 * @see de.tum.in.wwwseidl.programanalysis.cfg.edges.CFGAssertionEdge
 * @author Michael Petter
 * @author Andrea Flexeder
 */
public class SymbolTable implements Serializable{
    private Hashtable<String,Integer> name2internal;
    private Hashtable<Integer,String> internal2name;
    /**
     * builds a new empty Symboltable for a new Symboltablegeneration
     * resets also the internal ID Generator
     */
    public SymbolTable(){
        this(new Hashtable<String, Integer>(), new Hashtable<Integer, String>());
        IDGenerator.reset();
    }
    /**
     * new SymbolTable with exactly these Hashtables as parameters.
     * just for internal use; doesn't reset the Symboltables ID Generator
     * @param name as new Table mapping from external to internal presentation
     * @param internal as new Table mapping from internal to external presentation
     */
    private SymbolTable(Hashtable<String, Integer> name, Hashtable<Integer, String> internal){
        this.name2internal=name;
        this.internal2name=internal;
    }
    /**
     * sort of copy-constructor :-).
     * clones the given symboltable for use with scopes; the internal names are kept; no ID resetting is performed
     * @param old table to clone
     */
    public SymbolTable(SymbolTable old){
//	this((Hashtable<String, Integer>)old.name2internal.clone(), (Hashtable<Integer, String>)old.internal2name.clone());
	this((Hashtable<String, Integer>)old.name2internal.clone(), old.internal2name);
    }
    /**
     * enter a new mapping from external to internal presentation.
     * @param name external name
     * @return internal number for this identifier
     */
    public int newMapping(String name){
        int internal = IDGenerator.create();
        name2internal.put(name,internal);
        internal2name.put(internal,name);
        return internal;
    }
    /**
     * get internal presentation for a given external identifier
     * @param name the external name to query
     * @return the internal representation; -1 if not available
     */
    public int getInternal(String name){
	assert name2internal.get(name)!=null : "it seems, that the queried name "+name+" wasn't in the symtab";
	if (name2internal.get(name)==null) return -1;
	return name2internal.get(name);
    }
    /**
     * get external presentation of an internal identifier
     * @param internal internal representation to query
     * @return external presentation
     */
    public String getName(int internal){
	assert internal >= 0 :"only positive values are valid as internal representation";
	assert internal2name.get(internal)!=null : "it seems, that the queried object "+internal+" wasn't in the symtab";
        return internal2name.get(internal);
    }
  /**
     * get Collection of all global variables
     * @return internal presentation
     */
    public Collection getValidVariables(){
	return name2internal.values();
    }
    /**
     * can be omitted by using our "copy"-Constructor #SymbolTable
     * @return a clone copy of this SymbolTable
     */
    protected Object clone() throws CloneNotSupportedException {
        return new SymbolTable(this); 
    }
    
    /**
     * get Hashtable of internal representation of variables
     * @return internal presentation
     */
    public Map<Integer, String> getInternal2name(){
        return internal2name;
    }
    /**
     * get Hashtable of external representation of variables
     * @return external presentation
     */
    private Hashtable<String, Integer> getName2internal() {
        return name2internal;
    }
}

