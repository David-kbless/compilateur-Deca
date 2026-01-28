package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.instructions.REM;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import org.apache.commons.lang.Validate;
/**
 *
 * @author gl54
 * @date 01/01/2026
 */
public class Modulo extends AbstractOpDivMod {

    public Modulo(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {

        // Retrieving operands types
        Type type1 = this.getLeftOperand().verifyExpr(compiler, localEnv, currentClass);
        Type type2 = this.getRightOperand().verifyExpr(compiler, localEnv, currentClass);
        Validate.notNull(type1, "Left operand type cannot be null");
        Validate.notNull(type2, "Right operand type cannot be null");


        // Verification of the compatibility of operands' types
        if(!(type1.isInt() && type2.isInt())) {
            throw new ContextualError("Incompatible types for modulo operation: " 
                    + type1 + " and " + type2, this.getLocation());
        }
        this.setType(compiler.environmentType.INT); // Decorating the node with the result type 
        return compiler.environmentType.INT;
        
    }

    @Override
    protected Type typeOfOperation(DecacCompiler compiler, Type t1, Type t2) throws ContextualError {
        if (t1.isInt() && t2.isInt()) return compiler.environmentType.INT;
        throw new ContextualError("Modulo requires integer operands", getLocation());
    }

    @Override
    protected String getOperatorName() {
        return "%";
    }

    protected  GPRegister codeGenOperationQuotionReste(DecacCompiler compiler, GPRegister operande1, DVal operande2){
        compiler.addInstruction(new REM(operande2, operande1));

        return operande1;  
    }

}
