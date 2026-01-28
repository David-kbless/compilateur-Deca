package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.ima.pseudocode.DAddr;

// import fr.ensimag.deca.context.Type;
// import fr.ensimag.deca.DecacCompiler;
// import fr.ensimag.deca.context.ClassDefinition;
// import fr.ensimag.deca.context.ContextualError;
// import fr.ensimag.deca.context.EnvironmentExp;

/**
 * Left-hand side value of an assignment.
 * 
 * @author gl54
 * @date 01/01/2026
 */
public abstract class AbstractLValue extends AbstractExpr {

    // utilisé uniquement par assign
    public abstract DAddr getDAddr(DecacCompiler compiler);
}
