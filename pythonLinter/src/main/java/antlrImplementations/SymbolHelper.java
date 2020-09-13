package antlrImplementations;

import antlrImplementations.visitors.ExpressionVisitor;
import antlrImplementations.visitors.IdentifierVisitor;
import antlrImplementations.visitors.TypeVisitor;
import gen.Python3Parser;
import org.antlr.v4.runtime.tree.ParseTree;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import symbolTable.Scopes.IScope;
import symbolTable.Symbols.*;
import org.antlr.v4.runtime.Token;

import java.util.Arrays;
import java.util.LinkedList;

public class SymbolHelper {

    private static ExpressionVisitor expressionVisitor = new ExpressionVisitor();
    private static TypeVisitor typeVisitor = new TypeVisitor();

    public static TypeDiagnostic resolveType(Symbol rightSymbol, Python3Parser.Atom_exprContext rightIdentifierContext){
        IdentifierVisitor identifierVisitor = new IdentifierVisitor();

        //Wenn rechtes Symbol von Type IType ist, ist es entweder ein Funktions- oder Konstruktoraufruf
        if(rightSymbol instanceof IType){
            return new TypeDiagnostic((IType) rightSymbol);
        }

        //Falls nicht, dann ist es eine Varaiable, von welcher wir den Typ erfragen müssen um das Linke Symbol updaten zu können
        else if (isClassSymbol(rightSymbol.getType())){

            switch(identifierVisitor.visit(rightIdentifierContext)){

                case INSTANCEVAR_ACCESS:
                    String instanceVariable = rightIdentifierContext.atom().makeshifttype().identifier().getChild(1).getChild(1).getText();
                    Symbol instanceSymbol = ((ClassSymbol) rightSymbol.getType()).resolveMember(instanceVariable);
                    if(instanceSymbol == null){
                        return new TypeDiagnostic( new BuiltInTypeSymbol("void"));
                    }
                    else if(rightIdentifierContext.getChildCount() > 1){
                        return resolveDotProducts((ClassSymbol) instanceSymbol.getType(), rightIdentifierContext);
                    }
                    else{
                        return new TypeDiagnostic(instanceSymbol.getType());
                    }


                //Wird nicht benutzt, da Return Werte nicht ausgewertet werden können
                case METHDOE_CALL:
                    return new TypeDiagnostic(new BuiltInTypeSymbol("functionReturn"));



                case CLASS_ISELF:
                    return new TypeDiagnostic(rightSymbol.getType());
            }
        }

        else if(isBuiltInSymbol(rightSymbol.getType())){
            return new TypeDiagnostic(rightSymbol.getType());
        }

        else if(isGenericSymbol(rightSymbol.getType())){
            return new TypeDiagnostic(rightSymbol.getType());
        }
        return new TypeDiagnostic(new BuiltInTypeSymbol("void"));
    }

    public static TypeDiagnostic resolveDotProducts(ClassSymbol symbol, Python3Parser.Atom_exprContext atom_exprContext){
        ClassSymbol classSymbol = symbol;
        IType returnType = new BuiltInTypeSymbol("void");
        for(int trailerIndex = 1; trailerIndex < atom_exprContext.getChildCount(); trailerIndex++){

            //Springe über Methodenklammern
            if(atom_exprContext.getChild(trailerIndex).getChild(0).getText().equals("(")){
                continue;
            }

            //Resolved den Methodennamen nach dem Punkt
            String classMemberName = atom_exprContext.getChild(trailerIndex).getChild(1).getText();
            Symbol tempSymbol = classSymbol.resolveMember(classMemberName);
            if(tempSymbol == null){
                Diagnostic tempDiagnostic = new Diagnostic(
                        new Range(new Position(atom_exprContext.getStart().getLine()-1, atom_exprContext.getStart().getCharPositionInLine()),
                                new Position(atom_exprContext.getStart().getLine()-1, 5)),
                        "Error, " + classMemberName + " was not found as instance variable or method in Class",
                        DiagnosticSeverity.Error,
                        "My first Linter"
                );
                System.out.println("Error, " + classMemberName + " was not found as instance variable or method in Class");
                return new TypeDiagnostic(new BuiltInTypeSymbol("void"), tempDiagnostic);

            }
            if(isClassSymbol(tempSymbol.getType())){
                classSymbol = (ClassSymbol) tempSymbol;
            }
            else{
                returnType = classSymbol.resolveMember(classMemberName).getType();
                break;
            }
        }
        return new TypeDiagnostic(returnType);

    }

    public static TypeDiagnostic getTypeRecursive(ParseTree tree, IScope currentIScope){
        TypeDiagnostic currentType = new TypeDiagnostic();
        ContextNode contextNode = expressionVisitor.visit(tree);
        String[] filterOperatoren;
        LinkedList<String> filter;

        if(contextNode.getExpressionNode() == ExpressionNode.ATOMEXPRESSION_NODE){
            String type = typeVisitor.visit(contextNode.getParseTree());
            Symbol typeSymbol = currentIScope.resolve(type);
            return SymbolHelper.resolveType(typeSymbol, (Python3Parser.Atom_exprContext)contextNode.getParseTree());
        }

        LinkedList<ContextNode> contextNodes;
        switch(contextNode.getExpressionNode()){

            case TUPLE_NODE:
                filterOperatoren = new String[] {","};
                filter = new LinkedList<>(Arrays.asList(filterOperatoren));
                contextNodes = filterChildren(filter, contextNode.getParseTree());
                currentType.update(getTypeRecursive(contextNodes.getFirst().getParseTree(), currentIScope));
                currentType.setType(new GenericType("tuple", currentType.getType()));
                break;




            case NOT_TEST_NODE:
                currentType.setType((IType) currentIScope.resolve("bool"));
                //return (IType) currentIScope.resolve("bool");
                break;

            case AND_TEST_NODE:
                //return (IType) currentIScope.resolve("bool");
                currentType.setType((IType) currentIScope.resolve("bool"));
                break;

            case OR_TEST_NODE:
                //return (IType) currentIScope.resolve("bool");
                currentType.setType((IType) currentIScope.resolve("bool"));
                break;




            case COMPARISON_NODE:
                filterOperatoren = new String[] {"<",">","==",">=","<=","<>","!=","in","not", "in","is","is", "not"};
                filter = new LinkedList<>(Arrays.asList(filterOperatoren));
                contextNodes = filterChildren(filter, contextNode.getParseTree());
                currentType.update(getTypeRecursive(contextNodes.getFirst().getParseTree(), currentIScope));
                for (int contextIndex = 1; contextIndex < contextNodes.size(); contextIndex++) {

                    String comparingTypeName = getTypeRecursive(contextNodes.get(contextIndex).getParseTree(), currentIScope).getType().getName();

                    //Es werden zwei Unterschiedliche Typen verglichen
                    String currentTypeName = currentType.getType().getName();
                    if(!currentTypeName.equals(comparingTypeName)){
                        System.out.println("Warning, comparing " + currentTypeName + " to " + comparingTypeName);
                        Diagnostic diagnostic =new Diagnostic(
                                new Range(new Position(contextNodes.get(contextIndex).getToken().getLine()-1, contextNodes.get(contextIndex).getToken().getCharPositionInLine()),
                                        new Position(contextNodes.get(contextIndex).getToken().getLine()-1, 5)),
                                "Warning, comparing " + currentTypeName + " to " + comparingTypeName,
                                DiagnosticSeverity.Warning,
                                "My first Linter"
                        );
                        currentType.addDiagnostic(diagnostic);
                    }
                    currentType.update(getTypeRecursive(contextNodes.get(contextIndex).getParseTree(), currentIScope));
                }

                currentType.setType((IType) currentIScope.resolve("bool"));
                break;




            case ARITHEXPRESSION_NODE:
                filterOperatoren = new String[] {"+", "-"};
                filter = new LinkedList<>(Arrays.asList(filterOperatoren));
                contextNodes = filterChildren(filter, contextNode.getParseTree());
                currentType.update(getTypeRecursive(contextNodes.getFirst().getParseTree(), currentIScope));
                for (int contextIndex = 1; contextIndex < contextNodes.size(); contextIndex++) {

                    IType comparingType = getTypeRecursive(contextNodes.get(contextIndex).getParseTree(), currentIScope).getType();
                    Token comparingToken  = contextNodes.get(contextIndex).getToken();
                    currentType.update(TypeMatrix.typeCompatablityArithmitc(currentType.getType(), comparingType, currentIScope, comparingToken));
                }

                //return currentType;
                break;



            //Combiniert alle Binary Operatoren, da diese alle auf den gleichen Typen arbeiten
            case BINARY_EXPRESSION_NODE:
                filterOperatoren = new String[] {"^", "&", "|", "<<", ">>"};
                filter = new LinkedList<>(Arrays.asList(filterOperatoren));
                contextNodes = filterChildren(filter, contextNode.getParseTree());
                for (int contextIndex = 1; contextIndex < contextNodes.size(); contextIndex++) {

                    IType comparingType = getTypeRecursive(contextNodes.get(contextIndex).getParseTree(), currentIScope).getType();
                    Token comparingToken  = contextNodes.get(contextIndex).getToken();
                    currentType.update(TypeMatrix.typeCompatablityBinaryExpr(currentType.getType(), comparingType, currentIScope, comparingToken));
                }

                //return currentType;
                break;



            case POWER_NODE:
                filterOperatoren = new String[] {"**"};
                filter = new LinkedList<>(Arrays.asList(filterOperatoren));
                contextNodes = filterChildren(filter, contextNode.getParseTree());
                currentType = getTypeRecursive(contextNodes.getFirst().getParseTree(), currentIScope);
                for (int contextIndex = 1; contextIndex < contextNodes.size(); contextIndex++) {

                    IType comparingType = getTypeRecursive(contextNodes.get(contextIndex).getParseTree(), currentIScope).getType();
                    Token comparingToken  = contextNodes.get(contextIndex).getToken();
                    currentType.update(TypeMatrix.typeCompatablityBinaryExpr(currentType.getType(), comparingType, currentIScope, comparingToken));
                }

                //return currentType;
                break;



            case FACTOR_NODE:
                currentType.update(getTypeRecursive(contextNode.getParseTree().getChild(1), currentIScope));
                break;

            case TERM_NODE:
                filterOperatoren = new String[] {"*", "/", "%", "@", "//"};
                filter = new LinkedList<>(Arrays.asList(filterOperatoren));
                contextNodes = filterChildren(filter, contextNode.getParseTree());
                currentType = getTypeRecursive(contextNodes.getFirst().getParseTree(), currentIScope);
                for (int contextIndex = 1; contextIndex < contextNodes.size(); contextIndex++) {

                    IType comparingType = getTypeRecursive(contextNodes.get(contextIndex).getParseTree(), currentIScope).getType();
                    Token comparingToken  = contextNodes.get(contextIndex).getToken();
                    currentType.update(TypeMatrix.typeCompatablityArithmitc(currentType.getType(), comparingType, currentIScope, comparingToken));
                }
                //return currentType;
                break;

        }
        return currentType;
    }

    public static LinkedList<ContextNode> filterChildren(LinkedList<String> filter, ParseTree tree){
        LinkedList<ContextNode> contextNodes = new LinkedList<>();
        for(int child = 0; child < tree.getChildCount(); child++){
            if(filter.contains(tree.getChild(child).getText())){
                continue;
            }
            else{
                contextNodes.add(expressionVisitor.visit(tree.getChild(child)));
            }
        }
        return contextNodes;
    }

    public static boolean isClassSymbol(Symbol symbol){
        return (symbol instanceof ClassSymbol);
    }

    public static boolean isClassSymbol(IType type){
        return (type instanceof ClassSymbol);
    }

    public static boolean isMethodSymbol(Symbol symbol){
        return (symbol instanceof MethodSymbol);
    }

    public static boolean isTypeSymbol(Symbol symbol){
        return (symbol instanceof IType);
    }

    public static boolean isBuiltInSymbol(Symbol symbol){
        return (symbol instanceof BuiltInTypeSymbol);
    }

    public static boolean isBuiltInSymbol(IType type){
        return (type instanceof BuiltInTypeSymbol);
    }

    public static boolean isGenericSymbol(IType type) {return (type instanceof GenericType);}
}
