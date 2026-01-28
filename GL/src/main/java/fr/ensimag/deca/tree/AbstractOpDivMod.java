package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.ImmediateFloat;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.BEQ;
import fr.ensimag.ima.pseudocode.instructions.CMP;
import fr.ensimag.ima.pseudocode.instructions.LOAD;

public abstract class AbstractOpDivMod extends AbstractOpArith{
    
    public AbstractOpDivMod(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }


    protected GPRegister codeGenOperationBinaire(DecacCompiler compiler, GPRegister operande1, DVal operande2){
        
        // s'il l'utilisateur active les erreurs d'overflow et de division par zero
        if (!compiler.getCompilerOptions().getNoCheck()) {

            // comme operande 2 peut etre de type RegistreOffset, alors
            // j'utilise R0 pour eviter des erreur de cast dur a debug
            GPRegister temp = compiler.utiliserRegistre();
            compiler.addInstruction(new LOAD(operande2, temp));

            if (getRightOperand().getType().isInt()) {
                compiler.addInstruction(new CMP(0, temp));
            } else {
                compiler.addInstruction(new CMP(new ImmediateFloat(0.0f), temp));
            }
            compiler.addInstruction(new BEQ(new Label("div_zero_error")));
            compiler.addDivisionByZeroLabel();
            compiler.libererRegistre();
        } 

        return codeGenOperationQuotionReste(compiler, operande1, operande2);
    }

    protected abstract GPRegister codeGenOperationQuotionReste(DecacCompiler compiler, GPRegister operande1, DVal operande2);

}
