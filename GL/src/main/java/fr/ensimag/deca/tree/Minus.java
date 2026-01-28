package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.instructions.SUB;

/**
 * @author gl54
 * @date 01/01/2026
 */
public class Minus extends AbstractOpArith {
    public Minus(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }


    @Override
    protected String getOperatorName() {
        return "-";
    }
    
    protected GPRegister codeGenOperationBinaire(DecacCompiler compiler, GPRegister operande1, DVal operande2){
        compiler.addInstruction(new SUB(operande2, operande1));

        return operande1;  
    }
}
