package antlrImplementations.listeners;

import antlrImplementations.ContextNode;
import antlrImplementations.ExpressionNode;
import antlrImplementations.SymbolHelper;
import antlrImplementations.TypeMatrix;
import antlrImplementations.visitors.*;
import gen.Python3BaseListener;
import gen.Python3Parser;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import symbolTable.*;
import symbolTable.Scopes.GlobalIScope;
import symbolTable.Scopes.IScope;
import symbolTable.Symbols.*;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class RefClassFuncListener extends Python3BaseListener {

    private ParseTreeProperty<IScope> scopes;
    private GlobalIScope globalScope;
    private IScope currentIScope;
    private SymbolTable symbolTable;
    private TypeVisitor typeVisitor = new TypeVisitor();
    private AtomExprVisitor atomExprVisitor = new AtomExprVisitor();
    private IdentifierVisitor identifierVisitor = new IdentifierVisitor();
    private ExpressionVisitor expressionVisitor = new ExpressionVisitor();

    public RefClassFuncListener(SymbolTable symbolTable){
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

    private boolean isClassSymbol(Symbol symbol){
        return (symbol instanceof ClassSymbol);
    }

    private boolean isClassSymbol(IType type){
        return (type instanceof ClassSymbol);
    }

    private boolean isMethodSymbol(Symbol symbol){
        return (symbol instanceof MethodSymbol);
    }



    private boolean isTypeSymbol(Symbol symbol){
        return (symbol instanceof IType);
    }

    private boolean isBuiltInSymbol(Symbol symbol){
        return (symbol instanceof BuiltInTypeSymbol);
    }

    private boolean isBuiltInSymbol(IType type){
        return (type instanceof BuiltInTypeSymbol);
    }

    public SymbolTable getSymbolTable() {
        this.symbolTable.setScopes(this.scopes);
        this.symbolTable.setGlobals(this.globalScope);
        return symbolTable;
    }

    @Override
    public void enterFile_input(Python3Parser.File_inputContext ctx){

        currentIScope = globalScope;
    }

    @Override
    public void exitFile_input(Python3Parser.File_inputContext ctx){
        System.out.println("Exit File: " + currentIScope);
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
        List<ClassSymbol> superClasses = new LinkedList<>();
        ClassSymbol currentClass = (ClassSymbol) currentIScope.resolve(ctx.NAME().getText());
        currentIScope = scopes.get(ctx);
        if(ctx.arglist() != null){
            for(int child = 0; child < ctx.arglist().getChildCount(); child+=2){
                String superClassName = typeVisitor.visit(ctx.arglist().getChild(child));
                Symbol classSymbol = currentIScope.resolve(superClassName);
                if(classSymbol instanceof ClassSymbol){
                    superClasses.add((ClassSymbol) classSymbol);
                }
            }
            currentClass.setSuperClasses(superClasses);
        }
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

    @Override
    public void enterExpr_stmt(Python3Parser.Expr_stmtContext ctx) {
        if (!isAssignment(ctx)) {
            return;
        }

        if(expressionVisitor.visit(ctx.getChild(2)).getExpressionNode() == ExpressionNode.ATOMEXPRESSION_NODE) {

            Python3Parser.Atom_exprContext leftIdentifier = atomExprVisitor.visit(ctx.getChild(0));


            //Linker Wert ist kein Identifier, falsche Syntax
            if (leftIdentifier == null || leftIdentifier.atom().makeshifttype().identifier() == null) {
                return;
            }

            String typeString = typeVisitor.visit(ctx.getChild(2));

            //Hier werden die Typen von Variabelen geupdatet, welche eine Methode oder Variable zugewiesen bekommen haben
            switch (typeString) {

                case "bool":
                    return;

                case "number":
                    return;

                case "string":
                    return;

                case "functionReturn":
                    return;

                case "list":
                    handleList(ctx);
                    break;


                default:
                    handleAtomicExpression(ctx);
                    break;

            }

        }

        else{
            handleNoneAtomicExpressions(ctx);
        }
        return;
    }

    private void handleAtomicExpression(Python3Parser.Expr_stmtContext ctx){
        IType rightIdentifierType = new BuiltInTypeSymbol("void");

        Python3Parser.Atom_exprContext leftIdentifier = atomExprVisitor.visit(ctx.getChild(0));
        Python3Parser.Atom_exprContext rightIdentifier = atomExprVisitor.visit(ctx.getChild(2));


        String leftVariableName = leftIdentifier.atom().makeshifttype().identifier().NAME().getText();
        Symbol leftSymbol = currentIScope.resolve(leftVariableName);

        String rightVariableName = typeVisitor.visit(ctx.getChild(2));
        Symbol rightSymbol = currentIScope.resolve(rightVariableName);


        //Rechter Wert nicht definert im Programm oder Scope
        if(rightSymbol == null){
            return;
        }

        //Check ob Variable bereit einen nicht Builtin Type zugewiesen bekommen hat
        if(!(leftSymbol.getType().getName().equals("none"))){
            return;
        }

        rightIdentifierType = SymbolHelper.resolveType(rightSymbol, rightIdentifier).getType();



        //Rechter Wert ist definert, aber er zu einem späteren Zeitpunkt
        if(leftSymbol.getToken().getLine() < rightSymbol.getToken().getLine()){
            return;
        }


        leftSymbol.setType(rightIdentifierType);
        currentIScope.define(leftSymbol);
    }

    private void handleList(Python3Parser.Expr_stmtContext ctx){

        Python3Parser.Atom_exprContext leftIdentifier = atomExprVisitor.visit(ctx.getChild(0));
        Python3Parser.Atom_exprContext rightIdentifier = atomExprVisitor.visit(ctx.getChild(2));


        String leftVariableName = leftIdentifier.atom().makeshifttype().identifier().NAME().getText();
        Symbol leftSymbol = currentIScope.resolve(leftVariableName);

        Python3Parser.Testlist_compContext listContext = rightIdentifier.atom().makeshifttype().testlist_comp();
        String firstSymbolInListName = typeVisitor.visit(rightIdentifier.atom().makeshifttype().testlist_comp().getChild(0));
        Symbol firstSymbolInList = currentIScope.resolve(firstSymbolInListName);
        IType firstElementInListType = new BuiltInTypeSymbol("void");

        //Check ob Liste bereits ein echter Listentyp zugewiesen worden ist
        if((leftSymbol.getType() instanceof GenericType) || (leftSymbol.getType() instanceof ClassSymbol) || leftSymbol.getType() instanceof MethodSymbol){
            return;
        }

        firstElementInListType = SymbolHelper.resolveType(firstSymbolInList, rightIdentifier).getType();



        leftSymbol.setType(new GenericType(leftSymbol.type.getName(), firstElementInListType));
        currentIScope.define(leftSymbol);
    }

    private void handleNoneAtomicExpressions(Python3Parser.Expr_stmtContext ctx){
        IType rightIdentifierType = new BuiltInTypeSymbol("void");

        Python3Parser.Atom_exprContext leftIdentifier = atomExprVisitor.visit(ctx.getChild(0));
        Python3Parser.Atom_exprContext rightIdentifier = atomExprVisitor.visit(ctx.getChild(2));


        String leftVariableName = leftIdentifier.atom().makeshifttype().identifier().NAME().getText();
        Symbol leftSymbol = currentIScope.resolve(leftVariableName);

        rightIdentifierType = SymbolHelper.getTypeRecursive(ctx.getChild(2), currentIScope).getType();


        //Check ob Variable bereit einen nicht Builtin Type zugewiesen bekommen hat
        if(!(leftSymbol.getType().getName().equals("none"))){
            return;
        }



        leftSymbol.setType(rightIdentifierType);
        currentIScope.define(leftSymbol);
    }






}
