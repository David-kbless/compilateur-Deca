package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.instructions.BEQ;
import fr.ensimag.ima.pseudocode.instructions.BNE;
import fr.ensimag.ima.pseudocode.instructions.SEQ;

/**
 *
 * @author gl54
 * @date 01/01/2026
 */
public class Equals extends AbstractOpExactCmp {

    public Equals(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }


    @Override
    protected String getOperatorName() {
        return "==";
    }    

    // protected GPRegister codeGenOpComp(DecacCompiler compiler, GPRegister operande1, GPRegister operande2){

    //     // ON va enfin pouvoir utiliser la fameuse instruction SEQ, qui est trop coool (2 en 1)
    //     compiler.addInstruction(new SEQ(operande1));

    //     compiler.libererRegistre();

    //     return operande1;
    // }


    @Override
    protected void branchVrai(DecacCompiler compiler, Label E) {
        // @Ousmane : Si égalité, on branche à E
        compiler.addInstruction(new BEQ(E));
    }

    @Override
    protected void branchFaux(DecacCompiler compiler, Label E) {
        // @Ousmane : Si différent, on branche à E
        compiler.addInstruction(new BNE(E));
    }

    // Gardé uniquement pour le cas où on a besoin de stocker 0 ou 1 dans un registre
    protected GPRegister codeGenOpComp(DecacCompiler compiler, GPRegister operande1, GPRegister operande2) {
        compiler.addInstruction(new SEQ(operande1));
        compiler.libererRegistre();
        return operande1;
    }
    
}
