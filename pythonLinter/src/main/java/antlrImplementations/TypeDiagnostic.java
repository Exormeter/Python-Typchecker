package antlrImplementations;

import org.eclipse.lsp4j.Diagnostic;
import symbolTable.Symbols.BuiltInTypeSymbol;
import symbolTable.Symbols.IType;

import java.lang.reflect.Type;
import java.util.LinkedList;

public class TypeDiagnostic {

    private IType type =  new BuiltInTypeSymbol("void");
    private LinkedList<Diagnostic> diagnostics = new LinkedList<>();

    public TypeDiagnostic(IType type, Diagnostic diagnostic){
        this.type = type;
        diagnostics.add(diagnostic);
    }

    public TypeDiagnostic(IType type){
        this.type = type;
    }

    public TypeDiagnostic(){

    }


    public IType getType() {
        return type;
    }

    public void setType(IType type) {
        this.type = type;
    }

    public LinkedList<Diagnostic> getDiagnostics() {
        return diagnostics;
    }

    public void setDiagnostics(LinkedList<Diagnostic> diagnostics) {
        this.diagnostics = diagnostics;
    }

    public void update(TypeDiagnostic typeDiagnostic){
        this.type = typeDiagnostic.getType();
        diagnostics.addAll(typeDiagnostic.getDiagnostics());
    }

    public void addDiagnostic(Diagnostic diagnostic){
        this.diagnostics.add(diagnostic);
    }
}
