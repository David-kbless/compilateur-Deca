package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.instructions.BGE;
import fr.ensimag.ima.pseudocode.instructions.BLT;
import fr.ensimag.ima.pseudocode.instructions.SLT;

/**
 *
 * @author gl54
 * @date 01/01/2026
 */
public class Lower extends AbstractOpIneq {

    public Lower(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }


    @Override
    protected String getOperatorName() {
        return "<";
    }

    // @Override
    // protected GPRegister codeGenOpComp(DecacCompiler compiler, GPRegister operande1, GPRegister operande2){

    //     // ON va enfin pouvoir utiliser la fameuse instruction SEQ, qui est trop coool (2 en 1)
    //     compiler.addInstruction(new SLT(operande1));

    //     compiler.libererRegistre();

    //     return operande1;
    // }


    @Override
    protected void branchVrai(DecacCompiler compiler, Label E) {
        // @Ousmane : Branchement si strictement inférieur
        compiler.addInstruction(new BLT(E));
    }

    @Override
    protected void branchFaux(DecacCompiler compiler, Label E) {
        // @Ousmane : Inverse de < est >=
        compiler.addInstruction(new BGE(E));
    }

    protected GPRegister codeGenOpComp(DecacCompiler compiler, GPRegister operande1, GPRegister operande2) {
        compiler.addInstruction(new SLT(operande1));
        compiler.libererRegistre();
        return operande1;
    }

}
