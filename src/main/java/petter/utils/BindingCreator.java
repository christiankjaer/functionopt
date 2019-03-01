package petter.utils;

import java.util.LinkedList;
import java.util.List;

import java_cup.runtime.SyntaxTreeDFS;
import java_cup.runtime.XMLElement;
import java_cup.runtime.XMLElement.Terminal;
import petter.cfg.expression.types.Function;
import petter.cfg.expression.types.PointerTo;
import petter.cfg.expression.types.Type;

public class BindingCreator extends SyntaxTreeDFS.AbstractVisitor {
	private Type type;
	private BindingCreator(Type baseType){
		type=baseType;
		registerPreVisit("function",(parent,children) -> { 
			List<Type> l = ((List<Type>)((Terminal)(children.get(1))).value()); 
			type = new Function(type, l); });
		registerPreVisit("array",(parent,children) -> { type= new PointerTo(type); });
		registerPreVisit("pointer",(parent,children) -> { int number = ((Integer)((Terminal)(children.get(0))).value()); for (int i=0;i<number;i++) type= new PointerTo(type); });
	}
	@Override
	public void defaultPost(XMLElement arg0, List<XMLElement> arg1) {
	}

	@Override
	public void defaultPre(XMLElement arg0, List<XMLElement> arg1) {
	}
	public static Type extractType(XMLElement elem, Type baseType){
		BindingCreator bc = new BindingCreator(baseType);
		SyntaxTreeDFS.dfs(elem, bc);
		return bc.type;
	}
}
