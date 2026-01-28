package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;

public abstract class AbstractDeclParam extends Tree {
    // protected abstract void verifyDeclParam(DecacCompiler compiler,
    //                                          EnvironmentType envParams,
    //                                          ClassDefinition currentClass)
    //         throws ContextualError;

    protected abstract Type verifyDeclParamMembers(DecacCompiler compiler, int index) throws ContextualError;

    protected abstract EnvironmentExp verifyParamBody(EnvironmentExp env) throws ContextualError;
        
        
}
