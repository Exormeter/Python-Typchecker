package antlrImplementations.visitors;

import antlrImplementations.ContextNode;
import antlrImplementations.ExpressionNode;
import gen.Python3BaseVisitor;
import gen.Python3Parser;

public class ExpressionVisitor extends Python3BaseVisitor<ContextNode> {

    @Override
    public ContextNode visitTestlist_star_expr(Python3Parser.Testlist_star_exprContext ctx) {
        if(ctx.getChildCount() > 1){
            return new ContextNode(ExpressionNode.TUPLE_NODE, ctx, ctx.getStart());
        }
        return this.visit(ctx.getChild(0));
    }

    @Override
    public ContextNode visitOr_test(Python3Parser.Or_testContext ctx) {
        if(ctx.getChildCount() > 1){
            return new ContextNode(ExpressionNode.OR_TEST_NODE, ctx, ctx.getStart());
        }
        return this.visit(ctx.getChild(0));
    }

    @Override
    public ContextNode visitAnd_test(Python3Parser.And_testContext ctx) {
        if(ctx.getChildCount() > 1){
            return new ContextNode(ExpressionNode.AND_TEST_NODE, ctx, ctx.getStart());
        }
        return this.visit(ctx.getChild(0));
    }

    @Override
    public ContextNode visitNot_test(Python3Parser.Not_testContext ctx) {
        if(ctx.getChildCount() > 1){
            return new ContextNode(ExpressionNode.NOT_TEST_NODE, ctx, ctx.getStart());
        }
        return this.visit(ctx.getChild(0));
    }

    @Override
    public ContextNode visitComparison(Python3Parser.ComparisonContext ctx) {
        if(ctx.getChildCount() > 1){
            return new ContextNode(ExpressionNode.COMPARISON_NODE, ctx, ctx.getStart());
        }
        return this.visit(ctx.getChild(0));
    }

    @Override
    public ContextNode visitExpr(Python3Parser.ExprContext ctx) {
        if(ctx.getChildCount() > 1){
            return new ContextNode(ExpressionNode.BINARY_EXPRESSION_NODE, ctx, ctx.getStart());
        }
        return this.visit(ctx.getChild(0));
    }

    @Override
    public ContextNode visitXor_expr(Python3Parser.Xor_exprContext ctx) {
        if(ctx.getChildCount() > 1){
            return new ContextNode(ExpressionNode.BINARY_EXPRESSION_NODE, ctx, ctx.getStart());
        }
        return this.visit(ctx.getChild(0));
    }

    @Override
    public ContextNode visitAnd_expr(Python3Parser.And_exprContext ctx) {
        if(ctx.getChildCount() > 1){
            return new ContextNode(ExpressionNode.BINARY_EXPRESSION_NODE, ctx, ctx.getStart());
        }
        return this.visit(ctx.getChild(0));
    }

    @Override
    public ContextNode visitShift_expr(Python3Parser.Shift_exprContext ctx) {
        if(ctx.getChildCount() > 1){
            return new ContextNode(ExpressionNode.BINARY_EXPRESSION_NODE, ctx, ctx.getStart());
        }
        return this.visit(ctx.getChild(0));
    }

    @Override
    public ContextNode visitArith_expr(Python3Parser.Arith_exprContext ctx) {
        if(ctx.getChildCount() > 1){
            return new ContextNode(ExpressionNode.ARITHEXPRESSION_NODE, ctx, ctx.getStart());
        }
        return this.visit(ctx.getChild(0));
    }

    @Override
    public ContextNode visitTerm(Python3Parser.TermContext ctx) {
        if(ctx.getChildCount() > 1){
            return new ContextNode(ExpressionNode.TERM_NODE, ctx, ctx.getStart());
        }
        return this.visit(ctx.getChild(0));
    }

    @Override
    public ContextNode visitFactor(Python3Parser.FactorContext ctx) {
        if(ctx.getChildCount() > 1){
            return new ContextNode(ExpressionNode.FACTOR_NODE, ctx, ctx.getStart());
        }
        return this.visit(ctx.getChild(0));
    }

    @Override
    public ContextNode visitPower(Python3Parser.PowerContext ctx) {
        if(ctx.getChildCount() > 1){
            return new ContextNode(ExpressionNode.POWER_NODE, ctx, ctx.getStart());
        }
        return this.visit(ctx.getChild(0));
    }

    @Override
    public ContextNode visitAtom_expr(Python3Parser.Atom_exprContext ctx) {

        if(ctx.atom() != null && ctx.atom().getChild(0).getText().equals("(")){
            return this.visit(ctx.atom().getChild(1).getChild(0).getChild(0));
        }

        return new ContextNode(ExpressionNode.ATOMEXPRESSION_NODE, ctx, ctx.getStart());
    }


}
