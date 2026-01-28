package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Label;

/**
 *
 * @author gl54
 * @date 01/01/2026
 */
public class And extends AbstractOpBool {

    public And(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    protected String getOperatorName() {
        return "&&";
    }
    

    @Override
    protected void codeGenVrai(DecacCompiler compiler, Label E) {
        // @Ousmane : Si C1 est faux, on sort du ET (court-circuit)
        Label eFin = new Label("E_Fin_And." + compiler.getAndIncOpBool());
        getLeftOperand().codeGenFaux(compiler, eFin);
        getRightOperand().codeGenVrai(compiler, E);
        compiler.addLabel(eFin);
    }

    @Override
    protected void codeGenFaux(DecacCompiler compiler, Label E) {
        // @Ousmane : Si C1 est faux, alors (C1 && C2) est faux -> branchement direct à E [cite: 80, 81]
        getLeftOperand().codeGenFaux(compiler, E);
        getRightOperand().codeGenFaux(compiler, E);
    }

    @Override
    protected GPRegister codeGenOperationBinaireBool(DecacCompiler compiler, GPRegister operande1) {
        // Cette méthode n'est plus utile si tu utilises exclusivement le flot de contrôle
        return operande1;
    }   

    

}
