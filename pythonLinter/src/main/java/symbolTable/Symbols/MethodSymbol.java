
package symbolTable.Symbols;

import symbolTable.Scopes.IScope;

import java.util.LinkedHashMap;
import java.util.Map;



public class MethodSymbol extends Symbol implements symbolTable.Scopes.IScope, IType {
	Map<String, Symbol> orderedArgs = new LinkedHashMap<String, Symbol>();
    IScope enclosingIScope;

    public MethodSymbol(String name, IType retType, IScope enclosingIScope) {
        super(name, retType);
        this.enclosingIScope = enclosingIScope;
    }

    public Symbol resolve(String name) {
		Symbol s = orderedArgs.get(name);
        if ( s!=null ) {
        	return s;
		}
		if ( getEnclosingIScope() != null ) {
			return getEnclosingIScope().resolve(name);
		}
		return null;
	}

	public void define(Symbol sym) {
		orderedArgs.put(sym.name, sym);
		sym.IScope = this;
	}

	public IScope getEnclosingIScope() {
    	return enclosingIScope;
    }

	public String getScopeName() {
    	return name;
    }

    public String toString() {
    	return "method"+super.toString()+":"+orderedArgs.values();
    }

	public boolean symbolExistLocal(String name){
		Symbol s = orderedArgs.get(name);
		if(s == null){
			return false;
		}
		return true;
	}
}
