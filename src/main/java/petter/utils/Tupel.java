package petter.utils;
import petter.cfg.Annotatable;
import java.util.HashMap;
import java.util.Map;

public class Tupel<T,U> implements Annotatable{
    public T a; 
    public U b;
    public Tupel(T a, U b){
        this.a=a;
        this.b=b;
    }
    public  static<S,V> Tupel<S,V> create(S s, V v){
        return new Tupel<>(s,v);
    }
   public String toString(){
	return "("+a+", "+b+")";
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
