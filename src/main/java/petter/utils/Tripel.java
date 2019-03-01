package petter.utils;
import petter.cfg.Annotatable;
import java.util.HashMap;
import java.util.Map;

public class Tripel<T,U,V> implements Annotatable{
    public T a; 
    public U b;
    public V c;
    public Tripel(T a, U b,V c){
        this.a=a;
        this.b=b;
        this.c=c;
    }
    public static<S,W,X> Tripel<S,W,X> create(S s,W w,X x){
        return new Tripel<>(s,w,x);
    }
    public String toString(){
	return "("+a+", "+b+", "+c+")";
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
