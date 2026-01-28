package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.instructions.BGT;
import fr.ensimag.ima.pseudocode.instructions.BLE;
import fr.ensimag.ima.pseudocode.instructions.SGT;

/**
 *
 * @author gl54
 * @date 01/01/2026
 */
public class Greater extends AbstractOpIneq {

    public Greater(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }


    @Override
    protected String getOperatorName() {
        return ">";
    }

    // @Override
    // protected GPRegister codeGenOpComp(DecacCompiler compiler, GPRegister operande1, GPRegister operande2){

    //     // ON va enfin pouvoir utiliser la fameuse instruction SEQ, qui est trop coool (2 en 1)
    //     compiler.addInstruction(new SGT(operande1));

    //     compiler.libererRegistre();

    //     return operande1;
    // }

    @Override
    protected void branchVrai(DecacCompiler compiler, Label E) {
        compiler.addInstruction(new BGT(E));
    }

    @Override
    protected void branchFaux(DecacCompiler compiler, Label E) {
        // @Ousmane : Inverse de > est <=
        compiler.addInstruction(new BLE(E));
    }

    protected GPRegister codeGenOpComp(DecacCompiler compiler, GPRegister operande1, GPRegister operande2) {
        compiler.addInstruction(new SGT(operande1));
        compiler.libererRegistre();
        return operande1;
    }

}
