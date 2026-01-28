package fr.ensimag.deca.tree;


import fr.ensimag.deca.tools.IndentPrintStream;

/**
 * List of expressions (eg list of parameters).
 *
 * @author gl54
 * @date 01/01/2026
 */
public class ListExpr extends TreeList<AbstractExpr> {


    @Override
    public void decompile(IndentPrintStream s) {
        for (AbstractExpr expr : getList()) {
            expr.decompile(s);
            if (expr != getLast()) {
                s.print(", ");
            }
        }
    }
}
