package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.instructions.BRA;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;


import org.apache.commons.lang.Validate;

/**
 *
 * @author gl54
 * @date 01/01/2026
 */
public abstract class AbstractOpBool extends AbstractBinaryExpr {

    public AbstractOpBool(AbstractExpr leftOperand, AbstractExpr rightOperand) {
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
        Validate.notNull(resultType);

        this.setType(resultType);
        return resultType;
    }

    @Override
    protected Type typeOfOperation(DecacCompiler compiler, Type t1, Type t2) throws ContextualError {
        if (t1.isBoolean() && t2.isBoolean()) return compiler.environmentType.BOOLEAN;
        throw new ContextualError("Boolean operations require boolean operands", getLocation());
    }

    @Override
    public boolean isBooleanOp(){
        return true;
    }

    // @Override
    // protected GPRegister codeGenInst(DecacCompiler compiler){
        
    //     // on evalue d'abord et seulement l'operande 1
    //     GPRegister operande1 = getLeftOperand().codeGenInst(compiler);
        
    //     return codeGenOperationBinaireBool(compiler, operande1);
    // }

    @Override
    protected GPRegister codeGenInst(DecacCompiler compiler) {
        // @Ousmane : Si on a besoin de la valeur (0 ou 1) dans un registre
        GPRegister reg = compiler.utiliserRegistre();
        Label eFaux = new Label("E_Faux_Bool." + compiler.getAndIncOpBool());
        Label eFin = new Label("E_Fin_Bool." + compiler.getAndIncOpBool());
        this.codeGenFaux(compiler, eFaux);
        
        // Si on n'a pas branché vers Faux, alors c'est Vrai
        compiler.addInstruction(new LOAD(1, reg));
        compiler.addInstruction(new BRA(eFin));

        compiler.addLabel(eFaux);
        compiler.addInstruction(new LOAD(0, reg));

        compiler.addLabel(eFin);
        return reg;
    }
    
    protected abstract GPRegister codeGenOperationBinaireBool(DecacCompiler compiler, GPRegister operande1);
}
