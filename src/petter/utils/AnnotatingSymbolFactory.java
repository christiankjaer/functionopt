package petter.utils;
import petter.cfg.Annotatable;
import java_cup.runtime.ComplexSymbolFactory;
import java_cup.runtime.Symbol;
import java.io.File;

public class AnnotatingSymbolFactory extends ComplexSymbolFactory{
    public static class ExtendedLocation extends Location{
        private int offset;
        public ExtendedLocation(String unit, int line, int column, int offset){
            super(unit,line,column);
            this.offset=offset;
        }
        public ExtendedLocation(int line, int column, int offset){
            super(line,column);
            this.offset=offset;
        }
        public String toString(){
            return getLine()+"/"+getColumn()+"("+offset+")";
        }
        public int getOffset(){
            return offset;
        }
    }

    public AnnotatingSymbolFactory(File f){
    }
    public AnnotatingSymbolFactory(){
    }
    public Symbol newSymbol(String name, int id, Symbol left, Symbol right, Object value){
        ComplexSymbol sym = (ComplexSymbol)super.newSymbol(name,id,left,right,value);
        if (!(value instanceof Annotatable)) return sym;
        Annotatable a = (Annotatable)value;
        a.putAnnotation("__location_left",sym.getLeft());
        a.putAnnotation("__location_right",sym.getRight());
        return sym;
    }
}
