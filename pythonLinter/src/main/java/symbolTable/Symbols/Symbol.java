
package symbolTable.Symbols;
import org.antlr.v4.runtime.Token;
import symbolTable.Scopes.IScope;

public class Symbol {
    public String name;
    public IType type;
    public symbolTable.Scopes.IScope IScope;
    public Token token;

    public String toString() {
        if ( type!=null ) return '<'+getName()+":"+type+'>';
        return getName();
    }

    public Symbol(String name) {
        this.name = name;
    }

    public Symbol(String name, IType type) {
        this(name);
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public IType getType() {
        return type;
    }

    public void setType(IType type) {
        this.type = type;
    }

    public IScope getIScope() {
        return IScope;
    }

    public void setIScope(IScope IScope) {
        this.IScope = IScope;
    }

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }
}
