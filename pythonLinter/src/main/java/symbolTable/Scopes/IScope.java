

package symbolTable.Scopes;

import symbolTable.Symbols.Symbol;

public interface IScope {


    public String getScopeName();

    public IScope getEnclosingIScope();

    public void define(Symbol sym);

    public Symbol resolve(String name);

    public boolean symbolExistLocal(String name);
}
