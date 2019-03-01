/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package petter.cfg.expression.types;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author petter
 */
public class Function extends Type {

    private final Type returnType;
    private final List<Type> parameterTypes;

    public Function(Type returnType, List<Type> parameterTypes){
        this.returnType=returnType;
        this.parameterTypes=parameterTypes;
    }

    @Override
    public boolean isCallable() {
        return true;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Function)) return false;
        return returnType.equals(((Function)obj).returnType)&&parameterTypes.equals(((Function)obj).parameterTypes);
    }
    public Type getReturnType(){
        return returnType;
    }
    @Override
    public boolean isBasicType() {
        return false;
    }
    /**
     * gives access to parameters
     * @return types of all formal parameters
     */
    public Iterable<Type> parameter() {
        return parameterTypes;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Type t : parameterTypes) {
            sb.append(t);
            sb.append(",");
        }
        return returnType+"("+ sb.toString() +")";
    }
    
    
    
}
