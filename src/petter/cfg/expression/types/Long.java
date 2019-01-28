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
public class Long extends Type {
    private Long() {
    }
    private static Long singleton = new Long();
    public static Long create(){
        return singleton;
    }

    @Override
    public boolean equals(Object obj) {
        return singleton==obj;
    }

    @Override
    public String toString() {
        return "long"; //
    }

    @Override
    public boolean isBasicType() {
        return true;
    }
    
}
