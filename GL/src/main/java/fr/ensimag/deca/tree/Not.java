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

/**
 *
 * @author gl54
 * @date 01/01/2026
 */
public class Not extends AbstractUnaryExpr {

    public Not(AbstractExpr operand) {
        super(operand);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        Type t = getOperand().verifyExpr(compiler, localEnv, currentClass);
        Type resultType = typeUnaryOp(compiler, t);
        this.setType(resultType);
        return resultType;
    }

    @Override
    protected Type typeUnaryOp(DecacCompiler compiler, Type t) throws ContextualError {
        if (t.isBoolean()){
            return t;
        }

        throw new ContextualError("Operator NOT cannot be applied on type " + t, getLocation());
    }


    @Override
    protected String getOperatorName() {
        return "!";
    }

    @Override
protected void codeGenVrai(DecacCompiler compiler, Label E) {
    // @Ousmane : L'élimination à la volée : !C est vrai si C est faux
    getOperand().codeGenFaux(compiler, E);
}

@Override
protected void codeGenFaux(DecacCompiler compiler, Label E) {
    // @Ousmane : !C est faux si C est vrai
    getOperand().codeGenVrai(compiler, E);
}

    @Override
    protected GPRegister codeGenOperationUnaire(DecacCompiler compiler, GPRegister operande) {
        // @Ousmane : Méthode court-circuitée par le flot de contrôle
        return operande;
    }

    @Override
    protected GPRegister codeGenInst(DecacCompiler compiler) {
        GPRegister reg = compiler.utiliserRegistre();
        int labelId = compiler.getAndIncOpBool();
        Label eFaux = new Label("Not_Val_Faux." + labelId);
        Label eFin = new Label("Not_Val_Fin." + labelId);

        // @Ousmane : On utilise le flot de contrôle pour déterminer la valeur
        // Si !C est FAUX, on branche vers le chargement du 0
        this.codeGenFaux(compiler, eFaux);

        // Si on n'a pas branché, c'est que !C est VRAI
        compiler.addInstruction(new LOAD(1, reg));
        compiler.addInstruction(new BRA(eFin));

        compiler.addLabel(eFaux);
        compiler.addInstruction(new LOAD(0, reg));

        compiler.addLabel(eFin);
        return reg;
    }
}
