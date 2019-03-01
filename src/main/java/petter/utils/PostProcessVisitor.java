package petter.utils;
import petter.cfg.AbstractVisitor;
import petter.cfg.edges.Transition;
import petter.cfg.edges.Nop;
import petter.cfg.Procedure;
import petter.cfg.CompilationUnit;
import petter.cfg.State;
import java.util.*;

public class PostProcessVisitor extends AbstractVisitor{

    private HashSet<State> visited = new HashSet<State>();

    public PostProcessVisitor(){
	super(false);
    }

    /*
     * prettify CFG of CFGClass c
     * resulting CFG does not contain skip;-edges, unreachable pathes
     */
    public static void prettify(CompilationUnit c){
	PostProcessVisitor p = new PostProcessVisitor();
	for(Procedure m : c){
	    p.enter(m.getEnd());
	}
	p.fullAnalysis();
    }


    /* 
     * remove unreachable states
     *@return false if states was visited for the first time
     */
    @Override public boolean visit(State s){
	if((s.getInIterator() == null) && (! s.isBegin())){
	    Iterator<Transition> it  = s.getOutIterator();
	    while(it.hasNext()){
		Transition e = it.next();
		if(e.getSource().getAnnotation("__location") != null) e.getDest().putAnnotation("__location", e.getSource().getAnnotation("__location"));
		enter(e.getDest());
		enter(e.getSource());
		e.removeEdge(); 
	    }
	}
	if (visited.contains(s)) return false;
        else {
            visited.add(s);
            return true;
        }
    }

    /*
     * remove skip;-edges in CFG
     */
    @Override public boolean visit(Nop e){
	State source = e.getSource();
       	State dest = e.getDest();
	if((source.getOutDegree() != 1 ) ||(dest.getInDegree() != 1)) return true;
	
	if((source.isBegin())){
	    e.removeEdge();
	    source.setLoopSeparator(dest.isLoopSeparator());
	    if(dest.getAnnotation("__location") != null) source.putAnnotation("__location", dest.getAnnotation("__location"));
	    if(dest.getAnnotation("__label") != null)    source.putAnnotation("__label", dest.getAnnotation("__label"));
	    for(Transition d : dest.getOut()){
		d.setSource(source);
		enter(d.getSource());
	    }
	    for(Transition d : dest.getIn()){
		d.setDest(source);
		enter(d.getSource());
	    }
	    return false;
	}

	for(Transition out : dest.getOut()){
	    enter(out.getSource());
	}
	for(Transition in : dest.getIn()){
	    enter(in.getSource());
	}

       //dest state erhalten; dest aller in source eingehenden Kanten umsetzen!
	for( Transition in : source.getIn()){
	    State oldDest = in.getDest();
	    in.setDest(dest);
	    if(oldDest.getAnnotation("__location") != null) in.getDest().putAnnotation("__location", oldDest.getAnnotation("__location"));
	    if(oldDest.getAnnotation("__label") != null)    in.getDest().putAnnotation("__label", oldDest.getAnnotation("__label"));
	    if(oldDest.isLoopSeparator())                   in.getDest().setLoopSeparator(true);
	    enter(in.getSource());
	}
	//src aller von source ausgehenden Kanten umsetzen!
	for(Transition out : source.getOut()){
	    State oldSrc = out.getSource();
	    out.setSource(dest);
	    if(oldSrc.getAnnotation("__location") != null) out.getSource().putAnnotation("__location", oldSrc.getAnnotation("__location"));
	    if(oldSrc.getAnnotation("__label") != null)    out.getSource().putAnnotation("__label", oldSrc.getAnnotation("__label"));
	    if(oldSrc.isLoopSeparator())                   out.getSource().setLoopSeparator(true);
	    enter(out.getSource());
	}
	e.removeEdge();
	return false;
    }	

}



