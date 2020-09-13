package antlrImplementations.visitors;

import gen.Python3BaseVisitor;
import gen.Python3Parser;

public class TypeVisitor extends Python3BaseVisitor<String> {


    @Override
    public String visitAtom_expr(Python3Parser.Atom_exprContext ctx){

        if(ctx.atom().makeshifttype().bool() != null){
            return "bool";
        }
        else if(ctx.atom().makeshifttype().number() != null){
            return "number";
        }
        else if(ctx.atom().makeshifttype().string() != null){
            return "string";
        }

        else if(ctx.atom().makeshifttype().identifier() != null) {
            return ctx.atom().makeshifttype().identifier().getChild(0).getText();
        }
        else if(ctx.atom().makeshifttype().testlist_comp() != null) {
            return "list";
        }
        return "none";
    }
}
