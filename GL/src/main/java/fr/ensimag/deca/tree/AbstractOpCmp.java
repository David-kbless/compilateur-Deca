package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import org.apache.commons.lang.Validate;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.BRA;
import fr.ensimag.ima.pseudocode.instructions.CMP;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.Register;
/**
 *
 * @author gl54
 * @date 01/01/2026
 */
public abstract class AbstractOpCmp extends AbstractBinaryExpr {

    public AbstractOpCmp(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {

        Type t1 = this.getLeftOperand().verifyExpr(compiler, localEnv, currentClass);
        Type t2 = this.getRightOperand().verifyExpr(compiler, localEnv, currentClass);
        Validate.notNull(t1);
        Validate.notNull(t2);

        Type resultType = typeOfOperation(compiler, t1, t2);
        this.setType(resultType);
        return resultType;
    }

    @Override
    protected Type typeOfOperation(DecacCompiler compiler, Type t1, Type t2) throws ContextualError {
        if ((t1.isInt() || t1.isFloat()) && (t2.isInt() || t2.isFloat())) {
            return compiler.environmentType.BOOLEAN;
        }
        throw new ContextualError("Comparison operators require int or float operands", getLocation());
    }

    @Override
    public boolean isComparisonOp() {
        return true;
    }



    //---------------
    protected abstract GPRegister codeGenOpComp(DecacCompiler compiler, GPRegister operande1, GPRegister operande2);

    @Override
    protected void codeGenVrai(DecacCompiler compiler, Label E) {
        // @Ousmane : On compare et on branche si la condition VRAIE est remplie
        codeGenCompare(compiler);
        branchVrai(compiler, E);
    }

    @Override
    protected void codeGenFaux(DecacCompiler compiler, Label E) {
        // @Ousmane : On compare et on branche si la condition FAUSSE est remplie
        codeGenCompare(compiler);
        branchFaux(compiler, E);
    }

    
    protected void codeGenCompare(DecacCompiler compiler) {
        GPRegister operande1 = getOperande1(compiler, getLeftOperand());
        DVal operande2 = getOperande2(compiler, operande1, getRightOperand());
        // @Ousmane : Génération de l'instruction CMP
        // Si operande1 a été poussé sur la pile par manque de registres
        if (operande1 == operande2) {
            compiler.addInstruction(new LOAD(new RegisterOffset(0, Register.SP), Register.R0));
            compiler.addInstruction(new CMP(operande2, Register.R0));
        } else {
            compiler.addInstruction(new CMP(operande2, operande1));
        }
        
        // On libère les registres utilisés pour les opérandes après le CMP
        compiler.libererRegistre(); 
        compiler.libererRegistre();
    }


    @Override
    protected GPRegister codeGenInst(DecacCompiler compiler) {
        // @Ousmane : Si on a besoin d'une valeur 0/1 (ex: boolean b = x > y)
        GPRegister reg = compiler.utiliserRegistre();
        Label eFaux = new Label("E_Faux_Cmp." + compiler.getAndIncOpBool());
        Label eFin = new Label("E_Fin_Cmp." + compiler.getAndIncOpBool());

        this.codeGenFaux(compiler, eFaux);
        
        compiler.addInstruction(new LOAD(1, reg));
        compiler.addInstruction(new BRA(eFin));

        compiler.addLabel(eFaux);
        compiler.addInstruction(new LOAD(0, reg));

        compiler.addLabel(eFin);
        return reg;
    }

    // @Ousmane : Les sous-classes implémenteront le branchement spécifique (BNE, BEQ, BLT...)
    protected abstract void branchVrai(DecacCompiler compiler, Label E);
    protected abstract void branchFaux(DecacCompiler compiler, Label E);


}



