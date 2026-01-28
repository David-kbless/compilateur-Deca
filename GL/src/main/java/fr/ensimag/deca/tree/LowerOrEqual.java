package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.instructions.BGT;
import fr.ensimag.ima.pseudocode.instructions.BLE;
import fr.ensimag.ima.pseudocode.instructions.SLE;

/**
 *
 * @author gl54
 * @date 01/01/2026
 */
public class LowerOrEqual extends AbstractOpIneq {
    public LowerOrEqual(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }


    @Override
    protected String getOperatorName() {
        return "<=";
    }

    // @Override
    // protected GPRegister codeGenOpComp(DecacCompiler compiler, GPRegister operande1, GPRegister operande2){

    //     // ON va enfin pouvoir utiliser la fameuse instruction SEQ, qui est trop coool (2 en 1)
    //     compiler.addInstruction(new SLE(operande1));

    //     compiler.libererRegistre();

    //     return operande1;
    // }


    @Override
    protected void branchVrai(DecacCompiler compiler, Label E) {
        compiler.addInstruction(new BLE(E));
    }

    @Override
    protected void branchFaux(DecacCompiler compiler, Label E) {
        // @Ousmane : Inverse de <= est >
        compiler.addInstruction(new BGT(E));
    }

    protected GPRegister codeGenOpComp(DecacCompiler compiler, GPRegister operande1, GPRegister operande2) {
        compiler.addInstruction(new SLE(operande1));
        compiler.libererRegistre();
        return operande1;
    }

}
