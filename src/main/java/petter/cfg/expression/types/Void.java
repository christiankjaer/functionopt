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
public class Void extends Type {
    private Void() {}
    private static Void singleton = new Void();
    public static Void create(){
        return singleton;
    }

    @Override
    public boolean equals(Object obj) {
        return obj==singleton;
    }
    
    

    @Override
    public String toString() {
        return "void"; //To change body of generated methods, choose Tools | Templates.
    }

    
    
    @Override
    public boolean isBasicType() {
        return true;
    }
   
}
