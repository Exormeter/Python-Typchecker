package  symbolTable;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import symbolTable.Scopes.GlobalIScope;
import symbolTable.Scopes.IScope;
import symbolTable.Symbols.BuiltInTypeSymbol;

public class SymbolTable {
    GlobalIScope globals;
    ParseTreeProperty<IScope> scopes = new ParseTreeProperty<IScope>();


    public SymbolTable(GlobalIScope global) {
        this.globals = global;
        initBuiltInTypes();
    }


    public String toString() {
        return globals.toString();
    }

    protected void initBuiltInTypes(){
        globals.define(new BuiltInTypeSymbol("number"));
        globals.define(new BuiltInTypeSymbol("string"));
        globals.define(new BuiltInTypeSymbol("bool"));
        globals.define(new BuiltInTypeSymbol("none"));
        globals.define(new BuiltInTypeSymbol("void"));
        globals.define(new BuiltInTypeSymbol("functionReturn"));
        globals.define(new BuiltInTypeSymbol("list"));
        globals.define(new BuiltInTypeSymbol("tuple"));
    }

    public void saveScope(ParserRuleContext ctx, IScope s) {
        scopes.put(ctx, s);
    }

    public GlobalIScope getGlobals() {
        return globals;
    }

    public void setGlobals(GlobalIScope globals) {
        this.globals = globals;
    }

    public ParseTreeProperty<IScope> getScopes() {
        return scopes;
    }

    public void setScopes(ParseTreeProperty<IScope> scopes) {
        this.scopes = scopes;
    }
}
