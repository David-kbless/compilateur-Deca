package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.tools.IndentPrintStream;

import java.util.List;

import org.apache.log4j.Logger;


/**
 *
 * @author gl54
 * @date 01/01/2026
 */
public class ListDeclClass extends TreeList<AbstractDeclClass> {
    private static final Logger LOG = Logger.getLogger(ListDeclClass.class);
    
    @Override
    public void decompile(IndentPrintStream s) {
        for (AbstractDeclClass c : getList()) {
            c.decompile(s);
            s.println();
        }
    }

    /**
     * Pass 1 of [SyntaxeContextuelle]
     */
    void verifyListClass(DecacCompiler compiler) throws ContextualError {
        LOG.debug("verify listClass: start");
        List<AbstractDeclClass> list = getList();
        for (AbstractDeclClass declClass : list) {
            declClass.verifyClass(compiler);
        }
        LOG.debug("verify listClass: end");
    }

    /**
     * Pass 2 of [SyntaxeContextuelle]
     */
    public void verifyListClassMembers(DecacCompiler compiler) throws ContextualError {
        LOG.debug("verify listClassMembers: start");
        List<AbstractDeclClass> list = getList();
        for (AbstractDeclClass declClass : list) {
            declClass.verifyClassMembers(compiler);
        }
        LOG.debug("verify listClassMembers: end");
    }
    
    /**
     * Pass 3 of [SyntaxeContextuelle]
     */
    public void verifyListClassBody(DecacCompiler compiler) throws ContextualError {
        LOG.debug("verify listClassBody: start");
        List<AbstractDeclClass> list = getList();
        for (AbstractDeclClass declClass : list) {
            declClass.verifyClassBody(compiler);
        }
        LOG.debug("verify listClassBody: end");
    }


}
