
package symbolTable.Scopes;

public class LocalIScope extends BaseIScope {
    public LocalIScope(IScope parent) {
        super(parent);
    }
    public String getScopeName() {
        return "local";
    }
}
