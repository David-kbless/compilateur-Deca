package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.instructions.OPP;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;

/**
 * @author gl54
 * @date 01/01/2026
 */
public class UnaryMinus extends AbstractUnaryExpr {

    public UnaryMinus(AbstractExpr operand) {
        super(operand);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        Type t = getOperand().verifyExpr(compiler, localEnv, currentClass);
        Type resultType = typeUnaryOp(compiler,t);
        this.setType(resultType);
        return resultType;

    }

    @Override 
    protected Type typeUnaryOp(DecacCompiler compiler, Type t) throws ContextualError{
        if (t.isFloat() || t.isInt()){
            return t;
        }

        throw new ContextualError("Operator MINUS cannot be applied on type " + t, getLocation());
    }


    @Override
    protected String getOperatorName() {
        return "-";
    }

    protected GPRegister codeGenOperationUnaire(DecacCompiler compiler, GPRegister operande){
        compiler.addInstruction(new OPP((DVal)operande, operande));
  
        return operande;
    }
}
