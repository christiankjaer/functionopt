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
public class Short extends Type {
    private Short() {
    }
    private static Short singleton = new Short();
    public static Short create(){
        return singleton;
    }

    @Override
    public boolean equals(Object obj) {
        return singleton==obj;
    }

    @Override
    public String toString() {
        return "int"; //
    }

    @Override
    public boolean isBasicType() {
        return true;
    }
    
}
