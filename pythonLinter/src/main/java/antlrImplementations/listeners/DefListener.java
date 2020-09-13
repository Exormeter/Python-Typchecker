package antlrImplementations.listeners;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.*;
import gen.*;
import symbolTable.*;
import antlrImplementations.visitors.*;
import symbolTable.Scopes.GlobalIScope;
import symbolTable.Scopes.IScope;
import symbolTable.Scopes.LocalIScope;
import symbolTable.Symbols.*;


public class DefListener extends Python3BaseListener {
    SymbolTable symbolTable;
    GlobalIScope globals;
    IScope currentIScope;
    private ExpressionVisitor expressionVisitor = new ExpressionVisitor();
    private AtomExprVisitor atomExprVisitor = new AtomExprVisitor();



    /*
    Check ob Variable nur einen Builtin Typ zugewiesen bekommt. Sollte eine Function, ein anderer Wert oder auf der
    Variable eine Klasse instanziert werden, wird ihr der Type "none" gegeben, da zu diesem Zeitpunkt noch keine
    Referenzierung stattfindet
     */
    private void defineVar(ParseTree identifier, ParseTree valueTree){
        Symbol variable;
        IType type;


        Python3BaseVisitor typeVisitor = new TypeVisitor();
        String typeName = (String) typeVisitor.visit(valueTree);
        Python3Parser.Atom_exprContext leftIdentifier = atomExprVisitor.visit(identifier);
        Python3Parser.Atom_exprContext rightValue = atomExprVisitor.visit(valueTree);


        if(rightValue == null || rightValue.atom().makeshifttype().identifier() != null ){
            type = (IType) currentIScope.resolve("none");
            variable = new Symbol(identifier.getText(), type);
        }
        else{
            type = (IType) currentIScope.resolve(typeName);
            variable = new Symbol(identifier.getText(), type);
        }

        variable.setToken(leftIdentifier.getStart());
        if(!currentIScope.symbolExistLocal(identifier.getText())){
            currentIScope.define(variable);
        }
    }

    private void defineVar(String identifier, ParseTree valueTree){
        Symbol variable;
        IType type;


        Python3BaseVisitor typeVisitor = new TypeVisitor();
        String typeName = (String) typeVisitor.visit(valueTree);
        Python3Parser.Atom_exprContext rightValue = atomExprVisitor.visit(valueTree);


        if(rightValue == null || rightValue.atom().makeshifttype().identifier() != null ){
            type = (IType) currentIScope.resolve("none");
            variable = new Symbol(identifier, type);
        }

        else{
            type = (IType) currentIScope.resolve(typeName);
            variable = new Symbol(identifier, type);
        }

        variable.setToken(rightValue.getStart());
        if(!currentIScope.symbolExistLocal(identifier)){
            currentIScope.define(variable);
        }
    }

    private void defineVar(String identifier, Token token){
        IType type = (IType) currentIScope.resolve("none");
        Symbol symbol = new Symbol(identifier, type);
        symbol.setToken(token);
        if(!currentIScope.symbolExistLocal(identifier)){
            currentIScope.define(symbol);
        }
    }


    private boolean isFunction(Python3Parser.SuiteContext ctx){
        return ctx.getParent().getChild(0).getText().equals("def");
    }

    private boolean isClass(Python3Parser.SuiteContext ctx){
        return ctx.getParent().getChild(0).getText().equals("class");
    }

    public SymbolTable getSymbolTable() {
        return symbolTable;
    }

    public void setSymbolTable(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }

    @Override
    public void enterFile_input(Python3Parser.File_inputContext ctx) {
        globals = new GlobalIScope();
        this.symbolTable = new SymbolTable(globals);
        currentIScope = globals;
    }

    @Override
    public void exitFile_input(Python3Parser.File_inputContext ctx){
        System.out.println("Exit File: " + currentIScope);
    }

    @Override
    public void enterFuncdef(Python3Parser.FuncdefContext ctx){
        String functionName = ctx.NAME().getText();
        IType type = (IType) currentIScope.resolve("functionReturn");
        MethodSymbol methodSymbol = new MethodSymbol(functionName, type, currentIScope);
        methodSymbol.setToken(ctx.getStart());
        currentIScope.define(methodSymbol);
        symbolTable.saveScope(ctx, methodSymbol);
        currentIScope = methodSymbol;
    }

    @Override
    public void exitFuncdef(Python3Parser.FuncdefContext ctx){
        int parameterCount = ctx.parameters().typedargslist().getChildCount();
        Python3Parser.TypedargslistContext argListContext = ctx.parameters().typedargslist();

        for(int parameterIndex = 0; parameterIndex < parameterCount; parameterIndex++){
            if(argListContext.getChild(parameterIndex).getText().equals(",")){
                continue;
            }

            String parameterName = argListContext.getChild(parameterIndex).getText();
            int nextIndex = parameterIndex + 1;

            //Named Parameter
            if((parameterCount > nextIndex) && argListContext.getChild(parameterIndex + 1).getText().equals("=")){
                defineVar(parameterName, argListContext.getChild(parameterIndex + 2));
                parameterIndex += 2;
            }

            //Normal Parameter
            else{
                defineVar(parameterName, (Token) null);
            }
        }

        System.out.println("Exit Function: " + currentIScope);
        currentIScope = currentIScope.getEnclosingIScope();
    }

    @Override
    public void enterExpr_stmt(Python3Parser.Expr_stmtContext ctx){
        if(ctx.getChild(1) == null){
            return;
        }
        if (ctx.getChild(1).getText().equals("=") && ctx.getChildCount() == 3){

            switch(expressionVisitor.visit(ctx.getChild(2)).getExpressionNode()){

                case ATOMEXPRESSION_NODE:
                    defineVar(ctx.getChild(0), ctx.getChild(2));
                    break;

                default:
                    Python3Parser.Atom_exprContext leftIdentifier = atomExprVisitor.visit(ctx.getChild(0));
                    defineVar(ctx.getChild(0).getText(), leftIdentifier.getStart());
            }


        }
    }

    @Override
    public void enterSuite(Python3Parser.SuiteContext ctx){
        if(!isClass(ctx) && !isFunction(ctx)){
            LocalIScope scope = new LocalIScope(currentIScope);
            symbolTable.saveScope(ctx, scope);
            currentIScope = scope;
        }
    }

    @Override
    public void exitSuite(Python3Parser.SuiteContext ctx){
        if(!isClass(ctx) && !isFunction(ctx)){
            System.out.println("Exit Suite: " + currentIScope);
            currentIScope = currentIScope.getEnclosingIScope();
        }

    }

    @Override
    public void enterClassdef(Python3Parser.ClassdefContext ctx){
        String className = ctx.NAME().getText();
        ClassSymbol classSymbol = new ClassSymbol(className, currentIScope, null);
        classSymbol.setToken(ctx.getStart());
        currentIScope.define(classSymbol);
        symbolTable.saveScope(ctx, classSymbol);
        currentIScope = classSymbol;
    }

    public void exitClassdef(Python3Parser.ClassdefContext ctx){
        System.out.println("Exit Classdef:" + currentIScope);
        currentIScope = currentIScope.getEnclosingIScope();
    }
}
