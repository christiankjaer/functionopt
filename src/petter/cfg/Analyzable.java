package petter.cfg;
import java.util.Map;
/**
 * the base interface for all elements of a CFG.
 * Implement here, how to navigate in both directions forward and backward in Your CFG-Elements to obtain a flexible way of implementing forward and backward analysis by a Visitor like the ones provided in this framework. By using the predefined abstract visitors, You can obtain a fixpoint-analysis rapidly
 * @author Michael Petter
 * @see de.tum.in.wwwseidl.programanalysis.cfg.CFGVisitor
 * @see de.tum.in.wwwseidl.programanalysis.cfg.CFGPropagatingVisitor
 * @see AbstractCFGVisitor
 * @see AbstractCFGPropagatingVisitor
 */
public interface Analyzable extends Annotatable{
    /**
     * Plain forward analysis with a {@link de.tum.in.wwwseidl.programanalysis.cfg.CFGVisitor}
     * @param v the analysing Visitor
     */
    public void forwardAccept(Visitor v);
    /**
     * Plain backward analysis with a {@link de.tum.in.wwwseidl.programanalysis.cfg.CFGVisitor}
     * @param v the analysing Visitor
     */
    public void backwardAccept(Visitor v);

    /**
     * Advanced forward analysis.
     * With a {@link de.tum.in.wwwseidl.programanalysis.cfg.CFGPropagatingVisitor} and means to propagate additional information along with each path, the visitor is taking
     * @param v the analysing Visitor
     * @param d additional data to be propagated in the analysis
     */
    public <T>void forwardAccept(PropagatingVisitor<T> v,T d);
    /**
     * Advanced backward analysis. 
     * With a {@link de.tum.in.wwwseidl.programanalysis.cfg.CFGPropagatingVisitor} and means to propagate additional information along with each path, the visitor is taking
     * @param v the analysing Visitor
     * @param d additional data to be propagated in the analysis
     */
    public <T>void backwardAccept(PropagatingVisitor<T> v,T d);
}
