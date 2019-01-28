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
public class Char extends Type {
    private Char() {
    }
    private static Char singleton = new Char();
    public static Char create(){
        return singleton;
    }

    @Override
    public boolean equals(Object obj) {
        return singleton==obj;
    }

    @Override
    public String toString() {
        return "char"; //
    }

    @Override
    public boolean isBasicType() {
        return true;
    }
    
}
