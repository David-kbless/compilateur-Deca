package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.instructions.DIV;
import fr.ensimag.ima.pseudocode.instructions.QUO;

/**
 *
 * @author gl54
 * @date 01/01/2026
 */
public class Divide extends AbstractOpDivMod {
    public Divide(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }


    @Override
    protected String getOperatorName() {
        return "/";
    }

    protected  GPRegister codeGenOperationQuotionReste(DecacCompiler compiler, GPRegister operande1, DVal operande2){
        
        // en fonction du type des operandes 
        if (getLeftOperand().getType().isFloat()) {
            // si operande flottante
            compiler.addInstruction(new DIV(operande2, operande1));

        } else {
            // SI Operande entier
            compiler.addInstruction(new QUO(operande2, operande1));
        }

        return operande1;  
    }

}
