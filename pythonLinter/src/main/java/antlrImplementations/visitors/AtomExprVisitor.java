package antlrImplementations.visitors;

import gen.Python3BaseVisitor;
import gen.Python3Parser;

public class AtomExprVisitor extends Python3BaseVisitor <Python3Parser.Atom_exprContext> {

    @Override
    public Python3Parser.Atom_exprContext visitAtom_expr(Python3Parser.Atom_exprContext ctx){
        return ctx;
    }
}
