package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.instructions.FLOAT;


import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.ContextualError;

/**
 * Conversion of an int into a float. Used for implicit conversions.
 * 
 * @author gl54
 * @date 01/01/2026
 */
public class ConvFloat extends AbstractUnaryExpr {
    public ConvFloat(AbstractExpr operand) {
        super(operand);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        this.setType(compiler.environmentType.FLOAT);
        return this.getType();
    }

    protected Type typeUnaryOp(DecacCompiler compiler, Type t) throws ContextualError {
        if (t.isInt()) {
            return compiler.environmentType.FLOAT;
        }
        throw new ContextualError("Operator CONV_FLOAT cannot be applied on type " + t, getLocation());
    }


    @Override
    protected String getOperatorName() {
        return "/* conv float */";
    }

    @Override
    
    protected  GPRegister codeGenOperationUnaire(DecacCompiler compiler, GPRegister operande){
        
        compiler.addInstruction(new FLOAT(operande, operande));

        return operande;
    }
    
}
