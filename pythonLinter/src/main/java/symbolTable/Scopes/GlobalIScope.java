package symbolTable.Scopes;

public class GlobalIScope extends BaseIScope {
    public GlobalIScope() {
        super(null);
    }
    public String getScopeName() {
        return "global";
    }
    public String toString() {
        return "global"+super.symbols.values();
    }
}
