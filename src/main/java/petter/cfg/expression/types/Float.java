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
public class Float extends Type {
    private Float() {
    }
    private static Float singleton = new Float();
    public static Float create(){
        return singleton;
    }

    @Override
    public boolean equals(Object obj) {
        return singleton==obj;
    }

    @Override
    public String toString() {
        return "float"; //
    }

    @Override
    public boolean isBasicType() {
        return true;
    }
    
}
