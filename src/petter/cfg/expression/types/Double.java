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
public class Double extends Type {
    private Double() {
    }
    private static Double singleton = new Double();
    public static Double create(){
        return singleton;
    }

    @Override
    public boolean equals(Object obj) {
        return singleton==obj;
    }

    @Override
    public String toString() {
        return "double"; //
    }

    @Override
    public boolean isBasicType() {
        return true;
    }
    
}
