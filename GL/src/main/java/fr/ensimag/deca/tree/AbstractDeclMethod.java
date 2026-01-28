package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;

public abstract class AbstractDeclMethod extends Tree {
    // protected abstract void verifyMethodMembers(DecacCompiler compiler,
    //                                             ClassDefinition currentClass)
    //         throws ContextualError;

    protected abstract void codeGenMethod(DecacCompiler compiler);

    protected abstract EnvironmentExp verifyDeclMethodMembers(DecacCompiler compiler, ClassDefinition currentClass, int index) throws ContextualError;

    protected abstract void verifyMethodBody(DecacCompiler compiler, EnvironmentExp envExp, ClassDefinition currentClass) throws ContextualError ;
        
}
