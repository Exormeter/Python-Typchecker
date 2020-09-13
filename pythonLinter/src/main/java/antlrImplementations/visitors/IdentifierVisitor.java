package antlrImplementations.visitors;

import antlrImplementations.IdentifierConext;
import gen.Python3BaseVisitor;
import gen.Python3Parser;



public class IdentifierVisitor extends Python3BaseVisitor<IdentifierConext> {



    @Override
    public IdentifierConext visitAtom_expr(Python3Parser.Atom_exprContext ctx) {
       if(ctx.trailer() == null){
           return IdentifierConext.METHDOE_CALL;
       }
       return visit(ctx.getChild(0));
    }

    @Override
    public IdentifierConext visitIdentifier(Python3Parser.IdentifierContext ctx) {
        if(ctx.trailer() != null){
            return IdentifierConext.INSTANCEVAR_ACCESS;
        }
        return IdentifierConext.CLASS_ISELF;
    }
}
