package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Signature;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.context.Type;

public class ListDeclParam extends TreeList<AbstractDeclParam> {

    // Pass 1 of [SyntaxeContextuelle] A revoir ...
    public Signature verifyListDeclParam(DecacCompiler compiler)
            throws ContextualError {
        Signature sig = new Signature();
        int index = 3;
        for (AbstractDeclParam param : getList()) {
            Type t = param.verifyDeclParamMembers(compiler, index++);
            sig.add(t);
        }
        return sig;
    }

    public EnvironmentExp verifyListDeclParamBody(DecacCompiler compiler)
            throws ContextualError {
        EnvironmentExp envExpR = new EnvironmentExp(null);
        for (AbstractDeclParam param : getList()) {
            EnvironmentExp envExp = param.verifyParamBody(envExpR);
            try { envExpR.add(envExp);
            } catch (EnvironmentExp.DoubleDefException e) {
                throw new ContextualError("Parameter " +
                        ((DeclParam) param).getName().getName().getName() +
                        " already defined", param.getLocation());
            }
        }
        return envExpR;
    }

    public void decompile(IndentPrintStream s) {
        boolean first = true;
        for (AbstractDeclParam param : getList()) {
            if (!first) {
                s.print(", ");
            }
            param.decompile(s);
            first = false;
        }
    }

}