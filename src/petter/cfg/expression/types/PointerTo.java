/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package petter.cfg.expression.types;

/**
 *
 * @author petter
 */
public class PointerTo extends Type {
    private Type inner;
    public PointerTo(Type t){
        inner=t;
    }
    public Type getInner(){ 
        return inner; 
    }

    @Override
    public boolean isCallable() {
        return inner instanceof Function;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PointerTo)) return false;
        return inner.equals(((PointerTo)obj).inner);
    }

    @Override
    public boolean isBasicType() {
        return false;
    }

    @Override
    public boolean hasPointer() {
        return true;
    }

    @Override
    public String toString() {
        return inner+"*";
    }
    
    
    
}
