package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Label;

/**
 *
 * @author gl54
 * @date 01/01/2026
 */
public class Or extends AbstractOpBool {

    public Or(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    protected String getOperatorName() {
        return "||";
    }

// @Override
// protected GPRegister codeGenOperationBinaireBool(DecacCompiler compiler, GPRegister operande1){
//     int num = compiler.getAndIncOpBool();
//     Label eFin = new Label("E_Fin_Or." + num); // Utilise un nom explicite pour débugger

//     compiler.addInstruction(new CMP(0, operande1));
    
//     // LOGIQUE OR : Si le premier est VRAI (différent de 0), 
//     // on a fini, on saute à la fin.
//     compiler.addInstruction(new BNE(eFin)); 

//     // Si on est ici, c'est que le premier était FAUX.
//     // On évalue le deuxième.
//     GPRegister operande2 = getRightOperand().codeGenInst(compiler);
//     compiler.addInstruction(new LOAD(operande2, operande1));
//     compiler.libererRegistre();

//     compiler.addLabel(eFin);
//     return operande1;  
// }


    @Override
    protected void codeGenVrai(DecacCompiler compiler, Label E) {
        // @Ousmane : Si C1 est vrai, alors (C1 || C2) est vrai -> branchement direct à E
        getLeftOperand().codeGenVrai(compiler, E);
        getRightOperand().codeGenVrai(compiler, E);
    }

    @Override
    protected void codeGenFaux(DecacCompiler compiler, Label E) {
        // @Ousmane : Si C1 est vrai, on court-circuite vers la fin du OU
        Label eFin = new Label("E_Fin_Or." + compiler.getAndIncOpBool());
        getLeftOperand().codeGenVrai(compiler, eFin);
        getRightOperand().codeGenFaux(compiler, E);
        compiler.addLabel(eFin);
    }

    @Override
    protected GPRegister codeGenOperationBinaireBool(DecacCompiler compiler, GPRegister operande1) {
        return operande1;
    }


}



