package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;

public abstract class AbstractDeclField extends Tree {
    
    protected abstract void verifyFieldBody(DecacCompiler compiler, ClassDefinition currentClass, EnvironmentExp envExp) throws ContextualError;
    protected abstract Visibility verifyDeclField(DecacCompiler compiler, ClassDefinition superClass, ClassDefinition currentClass, EnvironmentExp envExpR) throws ContextualError;

    protected abstract void codeGenDeclField(DecacCompiler compiler);
    public abstract int simuleExecutionNbRegistres(DecacCompiler compiler);
    
}
