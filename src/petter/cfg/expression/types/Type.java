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
public abstract class Type {
    public boolean hasPointer(){
        return false;
    }
    public abstract boolean isBasicType();
    public boolean isCallable(){
        return false;
    }
}
