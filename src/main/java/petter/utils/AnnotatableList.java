package petter.utils;
import petter.cfg.Annotatable;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

public class AnnotatableList<T> implements Annotatable, Iterable<T>{
    
    private List<T> list;

    public AnnotatableList(){
	list = new ArrayList<T>();
    }

    public List<T> getList(){
	return list;
    }

    public T get(int index){
	return list.get(index);
    }

    public void add2Begin(T t){
	list.add(0, t);
    }

    public void add(T t){
	list.add(t);
    }
    public Iterator<T> iterator(){
	return list.iterator();
    }

    public int size(){
	return list.size();
    }

   public String toString(){
       return list.toString();
    }

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



}
