package antlrImplementations;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;

public class ContextNode {

    private ExpressionNode expressionNode;
    private ParseTree parseTree;
    private Token token;


    public ContextNode(ExpressionNode expressionNode, ParseTree tree, Token token){
        this.expressionNode = expressionNode;
        this.parseTree = tree;
        this.token = token;
    }

    public ExpressionNode getExpressionNode() {
        return expressionNode;
    }

    public void setExpressionNode(ExpressionNode expressionNode) {
        this.expressionNode = expressionNode;
    }

    public ParseTree getParseTree() {
        return parseTree;
    }

    public void setTree(ParseTree tree) {
        this.parseTree = tree;
    }

    public void setParseTree(ParseTree parseTree) {
        this.parseTree = parseTree;
    }

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }
}
