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
public class Int extends Type {
    private Int() {
    }
    private static Int singleton = new Int();
    public static Int create(){
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
