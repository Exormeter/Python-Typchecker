package antlrImplementations;

import org.antlr.v4.runtime.Token;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import symbolTable.Scopes.IScope;
import symbolTable.Symbols.IType;


public class TypeMatrix {

    public static TypeDiagnostic typeCompatablityArithmitc(IType type, IType otherType, IScope currentScope, Token token) {
        String errorMessage = "Error, operation doesn't support " + type.getName() + " and " + otherType.getName();
        switch (type.getName()) {

            case "number":

                switch (otherType.getName()) {

                    case "number":
                        return new TypeDiagnostic((IType) currentScope.resolve("number"));

                    case "bool":
                        return new TypeDiagnostic((IType) currentScope.resolve("number"));


                    default:
                        Diagnostic diagnostic =new Diagnostic(
                                new Range(new Position(token.getLine()-1, token.getCharPositionInLine()),
                                        new Position(token.getLine()-1, 5)),
                                errorMessage,
                                DiagnosticSeverity.Error,
                                "My first Linter"
                        );
                        return new TypeDiagnostic((IType) currentScope.resolve("void"), diagnostic);


                }


            case "string":
                switch (otherType.getName()) {


                    case "string":
                        return new TypeDiagnostic((IType) currentScope.resolve("string"));


                    case "bool":
                        return new TypeDiagnostic((IType) currentScope.resolve("bool"));


                    default:
                        Diagnostic diagnostic =new Diagnostic(
                                new Range(new Position(token.getLine()-1, token.getCharPositionInLine()),
                                        new Position(token.getLine()-1, 5)),
                                errorMessage,
                                DiagnosticSeverity.Error,
                                "My first Linter"
                        );
                        return new TypeDiagnostic((IType) currentScope.resolve("void"), diagnostic);

                }


            case "tuple":
                switch (otherType.getName()) {

                    case "tuple":
                        return new TypeDiagnostic((IType) currentScope.resolve("tuple"));


                    default:
                        Diagnostic diagnostic =new Diagnostic(
                                new Range(new Position(token.getLine()-1, token.getCharPositionInLine()),
                                        new Position(token.getLine()-1, 5)),
                                errorMessage,
                                DiagnosticSeverity.Error,
                                "My first Linter"
                        );
                        return new TypeDiagnostic((IType) currentScope.resolve("void"), diagnostic);


                }


            case "list":
                switch (otherType.getName()) {

                    case "list":
                        return new TypeDiagnostic((IType) currentScope.resolve("list"));


                    default:
                        Diagnostic diagnostic =new Diagnostic(
                                new Range(new Position(token.getLine()-1, token.getCharPositionInLine()),
                                        new Position(token.getLine()-1, 5)),
                                errorMessage,
                                DiagnosticSeverity.Error,
                                "My first Linter"
                        );
                        return new TypeDiagnostic((IType) currentScope.resolve("void"), diagnostic);


                }

            case "bool":
                switch (otherType.getName()) {

                    case "bool":
                        return new TypeDiagnostic((IType) currentScope.resolve("bool"));

                    case "number":
                        return new TypeDiagnostic((IType) currentScope.resolve("number"));


                    default:
                        Diagnostic diagnostic =new Diagnostic(
                                new Range(new Position(token.getLine()-1, token.getCharPositionInLine()),
                                        new Position(token.getLine()-1, 5)),
                                errorMessage,
                                DiagnosticSeverity.Error,
                                "My first Linter"
                        );
                        return new TypeDiagnostic((IType) currentScope.resolve("void"), diagnostic);


                }


            case "void": {
                Diagnostic diagnostic = new Diagnostic(
                        new Range(new Position(token.getLine() - 1, token.getCharPositionInLine()),
                                new Position(token.getLine() - 1, 5)),
                        errorMessage,
                        DiagnosticSeverity.Error,
                        "My first Linter"
                );
                return new TypeDiagnostic((IType) currentScope.resolve("void"), diagnostic);
            }


            case "functionRetrun": {
                Diagnostic diagnostic = new Diagnostic(
                        new Range(new Position(token.getLine() - 1, token.getCharPositionInLine()),
                                new Position(token.getLine() - 1, 5)),
                        "Warning, return Types can't be checkt, make sure that Types match",
                        DiagnosticSeverity.Warning,
                        "My first Linter"
                );
                return new TypeDiagnostic((IType) currentScope.resolve("void"), diagnostic);
            }

            case "none": {
                Diagnostic diagnostic = new Diagnostic(
                        new Range(new Position(token.getLine() - 1, token.getCharPositionInLine()),
                                new Position(token.getLine() - 1, 5)),
                        errorMessage,
                        DiagnosticSeverity.Error,
                        "My first Linter"
                );
                return new TypeDiagnostic((IType) currentScope.resolve("void"), diagnostic);
            }


            default: {
                Diagnostic diagnostic = new Diagnostic(
                        new Range(new Position(token.getLine() - 1, token.getCharPositionInLine()),
                                new Position(token.getLine() - 1, 5)),
                        errorMessage,
                        DiagnosticSeverity.Error,
                        "My first Linter"
                );
                return new TypeDiagnostic((IType) currentScope.resolve("void"), diagnostic);
            }

        }
    }

    public static TypeDiagnostic typeCompatablityBinaryExpr(IType type, IType otherType, IScope currentScope, Token token){
        if(type.getName().equals("number") && otherType.getName().equals("number")){
            return new TypeDiagnostic((IType) currentScope.resolve("number"));
        }
        else{
            Diagnostic diagnostic = new Diagnostic(
                    new Range(new Position(token.getLine() - 1, token.getCharPositionInLine()),
                            new Position(token.getLine() - 1, 5)),
                    "Error, adding or substracting a " + type.getName() + " and " + otherType.getName() + "is not possible",
                    DiagnosticSeverity.Error,
                    "My first Linter"
            );
            return new TypeDiagnostic((IType) currentScope.resolve("void"), diagnostic);
        }
    }
}
