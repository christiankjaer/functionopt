package petter.cfg;
import java.util.Map;
/**
 * the base interface for all annotatable elements in a CFG.
 * @author Michael Petter
 */
public interface Annotatable {
    
    /**
     * Get the annotation for the specified key.
     *
     * @param key the key used to identify the object
     * @return the value associated with this key or <code>null</code>
     * @see #putAnnotation(Object, Object)
     * @see #getAnnotation(Class)
     */
    public Object getAnnotation(Object key);

    /**
     * Get the annotation of specified type.
     * This method is used to retrieve an annotation by its type.
     * It requires that the value was stored using the class as key.
     *
     * @param key the type of the value; used to identify the object
     * @return the value associated with this key or <code>null</code>
     * @see #putAnnotation(Class, Object)
     * @see #getAnnotation(Object)
     * @throws ClassCastException if the value is not of type <code>T</code>
     */
    public <T> T getAnnotation(Class<T> key) throws ClassCastException;
  
    /**
     * Store an annotation with the specified key.
     * Suggested type for key values is <code>String</code>.
     *
     * @param key the key used to identify the object
     * @param value the value associated with this key or <code>null</code>
     * @return the old value associated with this key or <code>null</code>
     * @see #getAnnotation(Object)
     * @see #putAnnotation(Class, Object)
     */
    public Object putAnnotation(Object key, Object value);

    /**
     * Store an annotation of specified type.
     * In this case the class of the value is used to identify the value.
     * Use this only when you are sure that no two values of this type
     * would ever be associated with this object, to avoid conflicts.
     *
     * @param key the type of the value; used to identify the object
     * @param value the value associated with this key or <code>null</code>
     * @return the old value associated with this key or <code>null</code>
     * @see #getAnnotation(Class)
     * @see #putAnnotation(Object, Object)
     * @throws ClassCastException if the old value was not of type <code>T</code>
     */
    public <T> T putAnnotation(Class<T> key, T value) throws ClassCastException;
    /**
     * Retrieve all generic annotations.
     * This method retrieves all annotations added by one of the
     * <code>putAnnotation</code> methods. The named annotations like
     * range or comment are not included.
     * Do not depend on the returned map to be modifiable.
     *
     * @return the map of all generic annotations
     * @see #putAnnotations(Map)
     * @see #putAnnotation(Object, Object)
     * @see #putAnnotation(Class, Object)
     */
    public Map<Object, Object> getAnnotations(); 

    /**
     * Store all annotations from a given mapping.
     * Every key value pair contained in the map is stored with this object.
     *
     * @param a the map containing new keys and values
     * @see #getAnnotations()
     * @see #putAnnotation(Object, Object)
     * @see Map#putAll(Map)
     */
    public void putAnnotations(Map<?, ?> a);

}
