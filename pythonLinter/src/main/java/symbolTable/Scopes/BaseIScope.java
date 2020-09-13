package symbolTable.Scopes;


import symbolTable.Symbols.Symbol;

import java.util.LinkedHashMap;
import java.util.Map;

public abstract class BaseIScope implements IScope {
	IScope enclosingIScope;
	Map<String, Symbol> symbols = new LinkedHashMap<String, Symbol>();

    public BaseIScope(IScope enclosingIScope) {
    	this.enclosingIScope = enclosingIScope;
    }

    public Symbol resolve(String name) {
		Symbol s = symbols.get(name);
        if ( s!=null ) {
        	return s;
		}
		if ( enclosingIScope != null ) {
			return enclosingIScope.resolve(name);
		}
		return null;
	}

	public void define(Symbol sym) {
		symbols.put(sym.name, sym);
		sym.IScope = this;
	}

	public boolean symbolExistLocal(String name){
		Symbol s = symbols.get(name);
		if(s == null){
			return false;
		}
		return true;
	}

    public IScope getEnclosingIScope() {
    	return enclosingIScope;
    }

	public String toString() {
    	return symbols.keySet().toString();
    }
}
