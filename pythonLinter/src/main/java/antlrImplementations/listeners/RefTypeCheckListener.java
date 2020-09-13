package antlrImplementations.listeners;

import antlrImplementations.ExpressionNode;
import antlrImplementations.SymbolHelper;
import antlrImplementations.TypeDiagnostic;
import antlrImplementations.visitors.*;
import gen.Python3BaseListener;
import gen.Python3Parser;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import symbolTable.*;
import symbolTable.Scopes.GlobalIScope;
import symbolTable.Scopes.IScope;
import symbolTable.Symbols.*;

import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;


public class RefTypeCheckListener extends Python3BaseListener {

    private ParseTreeProperty<IScope> scopes;
    private GlobalIScope globalScope;
    private IScope currentIScope;
    private SymbolTable symbolTable;
    LinkedList<Diagnostic> diagnostics = new LinkedList<>();
    private TypeVisitor typeVisitor = new TypeVisitor();
    private AtomExprVisitor atomExprVisitor = new AtomExprVisitor();
    private IdentifierVisitor identifierVisitor = new IdentifierVisitor();
    private ExpressionVisitor expressionVisitor = new ExpressionVisitor();


    public RefTypeCheckListener(SymbolTable symbolTable){
        this.symbolTable = symbolTable;
        this.scopes = symbolTable.getScopes();
        this.globalScope = symbolTable.getGlobals();
    }

    private boolean isFunction(Python3Parser.SuiteContext ctx){
        return ctx.getParent().getChild(0).getText().equals("def");
    }

    private boolean isClass(Python3Parser.SuiteContext ctx){
        return ctx.getParent().getChild(0).getText().equals("class");
    }

    private boolean isAssignment(Python3Parser.Expr_stmtContext ctx){
        if(ctx.getChild(1) == null){
            return false;
        }
        return ctx.getChild(1).getText().equals("=");
    }

    private boolean isInstanceVariableCall(Python3Parser.Atom_exprContext ctx){
        if(ctx.atom() == null){
            return false;
        }
        if(ctx.atom().makeshifttype() == null){
            return false;
        }
        if(ctx.atom().makeshifttype().identifier() == null){
            return false;
        }
        if(ctx.atom().makeshifttype().identifier().trailer() == null){
            return false;
        }
        return true;
    }

    private boolean isMethodeCall(Python3Parser.Atom_exprContext ctx){
        if(ctx.trailer() == null){
            return false;
        }
        return true;
    }



    public SymbolTable getSymbolTable() {
        this.symbolTable.setScopes(this.scopes);
        this.symbolTable.setGlobals(this.globalScope);
        return symbolTable;
    }

    public List<Diagnostic> getDiagnostics() {
        return diagnostics;
    }


    @Override
    public void enterFile_input(Python3Parser.File_inputContext ctx){
        currentIScope = globalScope;
    }

    @Override
    public void exitFile_input(Python3Parser.File_inputContext ctx){
        System.out.println(diagnostics);
    }

    @Override
    public void enterFuncdef(Python3Parser.FuncdefContext ctx){
        currentIScope = scopes.get(ctx);
    }

    @Override
    public void exitFuncdef(Python3Parser.FuncdefContext ctx){
        currentIScope = currentIScope.getEnclosingIScope();
    }

    @Override
    public void enterClassdef(Python3Parser.ClassdefContext ctx){
        currentIScope = scopes.get(ctx);
    }

    @Override
    public void exitClassdef(Python3Parser.ClassdefContext ctx){
        currentIScope = currentIScope.getEnclosingIScope();
    }

    @Override
    public void enterSuite(Python3Parser.SuiteContext ctx){
        if(!isFunction(ctx) && !isClass(ctx)){
            currentIScope = scopes.get(ctx);
        }
    }

    @Override
    public void exitSuite(Python3Parser.SuiteContext ctx){
        if(!isFunction(ctx) && !isClass(ctx)){
            currentIScope = currentIScope.getEnclosingIScope();
        }
    }

    public void enterExpr_stmt(Python3Parser.Expr_stmtContext ctx){
        if(!isAssignment(ctx)){
            return;
        }
        Python3Parser.Atom_exprContext leftIdentifierContext = atomExprVisitor.visit(ctx.getChild(0));
        Python3Parser.Atom_exprContext rightIdentifierContext = atomExprVisitor.visit(ctx.getChild(2));

        //Linker Wert ist kein Identifier, falsche Syntax
        if(leftIdentifierContext.atom().makeshifttype().identifier() == null){
            System.out.println("Identifier expected");
            diagnostics.add(new Diagnostic(
                    new Range(new Position(leftIdentifierContext.getStart().getLine()-1, leftIdentifierContext.getStart().getCharPositionInLine()),
                            new Position(leftIdentifierContext.getStart().getLine()-1, 5)),
                    "Identifier expected",
                    DiagnosticSeverity.Error,
                    "My first Linter"
            ));
            return;
        }

        if(expressionVisitor.visit(ctx.getChild(2)).getExpressionNode() == ExpressionNode.ATOMEXPRESSION_NODE) {
            String typeString = typeVisitor.visit(ctx.getChild(2));


            switch (typeString) {

                case "bool":
                    checkBuiltIn(leftIdentifierContext, rightIdentifierContext);
                    break;

                case "string":
                    checkBuiltIn(leftIdentifierContext, rightIdentifierContext);
                    break;

                case "number":
                    checkBuiltIn(leftIdentifierContext, rightIdentifierContext);
                    break;

                case "list":
                    checkList(leftIdentifierContext, rightIdentifierContext);
                    break;

                case "void":
                    checkBuiltIn(leftIdentifierContext, rightIdentifierContext);
                    break;

                default:
                    checkIdentifier(leftIdentifierContext, rightIdentifierContext);
                    break;
            }
        }
        else{
            checkNonAtomic(ctx);
        }


    }


    private void checkBuiltIn(Python3Parser.Atom_exprContext leftIdentifierContext, Python3Parser.Atom_exprContext rightBuiltInContext){
        String leftVariableName = leftIdentifierContext.atom().makeshifttype().identifier().NAME().getText();
        Symbol leftSymbol = currentIScope.resolve(leftVariableName);
        String rightVariableName = typeVisitor.visit(rightBuiltInContext);
        Symbol rightSymbol = currentIScope.resolve(rightVariableName);

        if(!leftSymbol.getType().equals((IType) rightSymbol)){
            System.out.println("Warning, " + leftSymbol.getName() + " was from Type " +leftSymbol.getType().getName() + " before Assigment, is now " + (IType)rightSymbol);
            diagnostics.add(new Diagnostic(
                    new Range(new Position(leftIdentifierContext.getStart().getLine()-1, leftIdentifierContext.getStart().getCharPositionInLine()),
                            new Position(leftIdentifierContext.getStart().getLine()-1, 5)),
                    leftSymbol.getName() + " was from Type " +leftSymbol.getType().getName() +  "before assigment, is now " + (IType)rightSymbol,
                    DiagnosticSeverity.Warning,
                    "My first Linter"
            ));
        }
        leftSymbol.setType((IType) rightSymbol);
        currentIScope.define(leftSymbol);

    }


    private void checkIdentifier(Python3Parser.Atom_exprContext leftIdentifierContext, Python3Parser.Atom_exprContext rightIdentifierContext){
        IType rightIdentifierType = new BuiltInTypeSymbol("void");

        String leftVariableName = leftIdentifierContext.atom().makeshifttype().identifier().NAME().getText();
        Symbol leftSymbol = currentIScope.resolve(leftVariableName);
        String rightVariableName = typeVisitor.visit(rightIdentifierContext);
        Symbol rightSymbol = currentIScope.resolve(rightVariableName);


        //Rechter Wert nicht definert im Programm oder IScope
        if(rightSymbol == null){
            System.out.println("Error, " + rightVariableName + " not found");
            diagnostics.add(new Diagnostic(
                    new Range(new Position(rightIdentifierContext.getStart().getLine()-1, rightIdentifierContext.getStart().getCharPositionInLine()),
                            new Position(rightIdentifierContext.getStart().getLine()-1, 5)),
                    "Error, " + rightVariableName + " can not be resolved",
                    DiagnosticSeverity.Error,
                    "My first Linter"
            ));
            return;
        }



        //Rechter Wert ist definert, aber er zu einem späteren Zeitpunkt
        if(leftIdentifierContext.atom().makeshifttype().identifier().getStart().getLine() < rightSymbol.getToken().getLine()){
            System.out.println("Error, " + rightVariableName + " Class or Function not declared before use");
            diagnostics.add(new Diagnostic(
                    new Range(new Position(rightIdentifierContext.getStart().getLine()-1, rightIdentifierContext.getStart().getCharPositionInLine()),
                            new Position(rightIdentifierContext.getStart().getLine()-1, 5)),
                    "Error, " + rightVariableName + "Class or Function not declared before use",
                    DiagnosticSeverity.Error,
                    "My first Linter"
            ));
            return;
        }

        TypeDiagnostic typeDiagnostic = SymbolHelper.resolveType(rightSymbol, rightIdentifierContext);
        rightIdentifierType = typeDiagnostic.getType();
        this.diagnostics.addAll(typeDiagnostic.getDiagnostics());


        //Check ob Variable bereit einen nicht Builtin Type zugewiesen bekommen hat
        if(!leftSymbol.getType().equals((IType) rightIdentifierType)){
            System.out.println("Warning, " + leftSymbol.getName() + " was from Type " +leftSymbol.getType().getName() + " before Assigment");
            diagnostics.add(new Diagnostic(
                    new Range(new Position(leftIdentifierContext.getStart().getLine()-1, leftIdentifierContext.getStart().getCharPositionInLine()),
                            new Position(leftIdentifierContext.getStart().getLine()-1, 5)),
                    leftSymbol.getName() + " was from Type " +leftSymbol.getType().getName() +  "before assigment, is now " + rightIdentifierType,
                    DiagnosticSeverity.Warning,
                    "My first Linter"
            ));
        }

        //Update des linken Variablentypen
        leftSymbol.setType(rightIdentifierType);
        currentIScope.define(leftSymbol);
    }

    private void checkList(Python3Parser.Atom_exprContext leftIdentifierContext, Python3Parser.Atom_exprContext rightIdentifierContext){
        String leftVariableName = leftIdentifierContext.atom().makeshifttype().identifier().NAME().getText();
        Symbol leftSymbol = currentIScope.resolve(leftVariableName);
        IType leftSymbolType = leftSymbol.getType();
        Python3Parser.Testlist_compContext listContext = rightIdentifierContext.atom().makeshifttype().testlist_comp();
        Symbol firstSymbolInList = currentIScope.resolve(typeVisitor.visit(listContext.getChild(0)));

        TypeDiagnostic typeDiagnostic = checkListMemberType(listContext, firstSymbolInList,0);
        IType firstElementInListType = typeDiagnostic.getType();
        this.diagnostics.addAll(typeDiagnostic.getDiagnostics());


        //Check ob Varibale vom Typ List ist, falls nicht wird der Type geupdatet mit generischem Type
        if(!(leftSymbol.getType() instanceof GenericType)){
            System.out.println("Warning, Identifier is of Type "+ leftSymbolType + "before Assigment, is now List");
            diagnostics.add(new Diagnostic(
                    new Range(new Position(leftIdentifierContext.getStart().getLine()-1, leftIdentifierContext.getStart().getCharPositionInLine()),
                            new Position(leftIdentifierContext.getStart().getLine()-1, 5)),
                    "Warning, Identifier is of Type \"+ leftSymbolType + \"before Assigment, is now List",
                    DiagnosticSeverity.Warning,
                    "My first Linter"
            ));
            leftSymbol.setType(new GenericType("list", firstElementInListType));

        }
        leftSymbolType = (GenericType) leftSymbol.getType();

        //Check ob Type der List bei Zuweisung einer neuen Liste übereinstimmt
        if(!((GenericType) leftSymbolType).getGenericType().equals(firstElementInListType)){
            System.out.println("Warning, List was of Type "+ ((GenericType) leftSymbolType).getGenericType() + " before Assigment, is now " + firstElementInListType);
            diagnostics.add(new Diagnostic(
                    new Range(new Position(leftIdentifierContext.getStart().getLine()-1, leftIdentifierContext.getStart().getCharPositionInLine()),
                            new Position(leftIdentifierContext.getStart().getLine()-1, 5)),
                    "Warning, Identifier is of Type \"+ leftSymbolType + \"before Assigment, is now List",
                    DiagnosticSeverity.Warning,
                    "My first Linter"
            ));
            leftSymbol.setType(new GenericType("list", firstElementInListType));
        }
        leftSymbolType = (GenericType) leftSymbol.getType();



        for(int childIndex = 0; childIndex < listContext.getChildCount(); childIndex += 2){
            Python3Parser.Atom_exprContext listMemberTypeContext = atomExprVisitor.visit(listContext.getChild(childIndex));
            String listMemberTypeString = typeVisitor.visit(listMemberTypeContext);
            Symbol listMemberSymbol = currentIScope.resolve(listMemberTypeString);

            typeDiagnostic = checkListMemberType(listContext, listMemberSymbol, childIndex);
            IType listMemberType = typeDiagnostic.getType();
            this.diagnostics.addAll(typeDiagnostic.getDiagnostics());


            //Check falls Mitglied zur Zeit der Zuweisung definiert sind
            if(listMemberSymbol == null){
                System.out.println("Error, " + listMemberTypeString + " not found");
                diagnostics.add(new Diagnostic(
                        new Range(new Position(rightIdentifierContext.getStart().getLine()-1, rightIdentifierContext.getStart().getCharPositionInLine()),
                                new Position(rightIdentifierContext.getStart().getLine()-1, 5)),
                        "Error, " + listMemberTypeString + " can not be resolved",
                        DiagnosticSeverity.Error,
                        "My first Linter"
                ));
                continue; //Sorry BC
            }


            //Check ob alle Mitglieder der Liste vom gleichen Typ sind
            if(!listMemberType.equals(((GenericType) leftSymbolType).getGenericType())){
                System.out.println("Warning, List is of Type "+ leftSymbolType + ", but " + listMemberType.getName() + " was added");
                diagnostics.add(new Diagnostic(
                        new Range(new Position(leftIdentifierContext.getStart().getLine()-1, leftIdentifierContext.getStart().getCharPositionInLine()),
                                new Position(leftIdentifierContext.getStart().getLine()-1, 5)),
                        "Warning, List is of Type "+ leftSymbolType + ", but " + listMemberType.getName() + " was added",
                        DiagnosticSeverity.Warning,
                        "My first Linter"
                ));
            }
        }

    }

    @Override
    public void enterAtom_expr(Python3Parser.Atom_exprContext ctx){
        if(!(ctx.getChild(ctx.getChildCount()-1) instanceof  Python3Parser.TrailerContext)){
            return;
        }

        String identifierName = typeVisitor.visit(ctx);
        Symbol identifierSymbol = currentIScope.resolve(identifierName);

        if(identifierName == null){
            return;
        }

        if(!(identifierSymbol.getType() instanceof ClassSymbol)){
            System.out.println("Error, you are trying to call a method on a variable that is not from type Class");
            diagnostics.add(new Diagnostic(
                    new Range(new Position(ctx.getStart().getLine()-1, ctx.getStart().getCharPositionInLine()),
                            new Position(ctx.getStart().getLine()-1, 5)),
                    "Error, you are trying to call a method on a variable that is not form type Class",
                    DiagnosticSeverity.Warning,
                    "My first Linter"
            ));
            return;
        }

        if(((ClassSymbol) identifierSymbol.getType()).resolveMember(ctx.atom().makeshifttype().identifier().trailer().getText().substring(1)) == null){
            System.out.println("Error, you are trying to call a method on a variable that was not found in the Class");
            diagnostics.add(new Diagnostic(
                    new Range(new Position(ctx.getStart().getLine()-1, ctx.getStart().getCharPositionInLine()),
                            new Position(ctx.getStart().getLine()-1, 5)),
                    "Error, you are trying to call a method on a variable that is not form type Class",
                    DiagnosticSeverity.Warning,
                    "My first Linter"
            ));
            return;
        };
    }


    private TypeDiagnostic checkListMemberType(Python3Parser.Testlist_compContext listContext, Symbol symbol, int member){
        //Wenn erstes Symbol in Liste vom Typ BuiltIn
        Python3Parser.Atom_exprContext atomContext= atomExprVisitor.visit(listContext.getChild(member));
        return SymbolHelper.resolveType(symbol, atomContext);
    }

    private void checkNonAtomic(Python3Parser.Expr_stmtContext ctx){
            IType rightIdentifierType = new BuiltInTypeSymbol("void");

            Python3Parser.Atom_exprContext leftIdentifier = atomExprVisitor.visit(ctx.getChild(0));
            Python3Parser.Atom_exprContext rightIdentifier = atomExprVisitor.visit(ctx.getChild(2));


            String leftVariableName = leftIdentifier.atom().makeshifttype().identifier().NAME().getText();
            Symbol leftSymbol = currentIScope.resolve(leftVariableName);

            TypeDiagnostic typeDiagnostic= SymbolHelper.getTypeRecursive(ctx.getChild(2),currentIScope);
            rightIdentifierType = typeDiagnostic.getType();
            this.diagnostics.addAll(typeDiagnostic.getDiagnostics());


            //Check ob Variable bereit einen nicht Builtin Type zugewiesen bekommen hat
            if(!(leftSymbol.getType().getName().equals("none"))){
                return;
            }

            leftSymbol.setType(rightIdentifierType);
            currentIScope.define(leftSymbol);
        }
    }

