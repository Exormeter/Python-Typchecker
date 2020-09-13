package symbolTable.Symbols;

import symbolTable.Scopes.IScope;

import java.util.*;

public class ClassSymbol extends Symbol implements symbolTable.Scopes.IScope, IType {

    IScope enclosingIScope;
    List<ClassSymbol> superClasses;
    public Map<String,Symbol> members=new LinkedHashMap<String,Symbol>();

    public ClassSymbol(String name, IScope enclosingIScope, List<ClassSymbol> superClasses) {
        super(name);
        this.enclosingIScope = enclosingIScope;
        this.superClasses = superClasses;
    }

    public IScope getParentScope() {
        if ( superClasses ==null ) {
            return enclosingIScope;
        }
        return superClasses.get(0);
    }

    public Symbol resolveMember(String name) {
        Symbol s = members.get(name);
        if ( s!=null ) {
            return s;
        }


        if ( superClasses != null ) {
            Symbol symbol;
            for (ClassSymbol superClass: superClasses) {
                symbol = superClass.resolveMember(name);
                if(symbol != null){
                    return symbol;
                }
            }
        }
        return null;
    }

    public Symbol resolve(String name) {
        Symbol s = getMembers().get(name);
        if ( s!=null ) {
            return s;
        }
        if ( getParentScope() != null ) {
            return getParentScope().resolve(name);
        }
        return null;
    }

    public void define(Symbol sym) {
        getMembers().put(sym.name, sym);
        sym.IScope = this;
    }

    public boolean symbolExistLocal(String name){
        Symbol s = members.get(name);
        if(s == null){
            return false;
        }
        return true;
    }

    public IScope getEnclosingIScope() {
        return enclosingIScope;
    }

    public String getScopeName() {
        return name;
    }

    public Map<String, Symbol> getMembers() {
        return members;
    }

    public void setSuperClasses(List<ClassSymbol> superClasses) {
        this.superClasses = superClasses;
    }

    public String toString() {
        return "class "+name+":{"+ (members.keySet().toString())+"} <"+superClasses+">";
    }
}
