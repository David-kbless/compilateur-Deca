package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.ImmediateInteger;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.instructions.BRA;

import java.io.PrintStream;

/**
 *
 * @author gl54
 * @date 01/01/2026
 */
public class BooleanLiteral extends AbstractExpr {

    private boolean value;

    public BooleanLiteral(boolean value) {
        this.value = value;
    }

    public boolean getValue() {
        return value;
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        Type t = compiler.environmentType.BOOLEAN;
        this.setType(t);
        return t;
    }


    @Override
    public void decompile(IndentPrintStream s) {
        s.print(Boolean.toString(value));
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        // leaf node => nothing to do
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        // leaf node => nothing to do
    }

    @Override
    String prettyPrintNode() {
        return "BooleanLiteral (" + value + ")";
    }

    @Override
    protected DVal codeGenInst(DecacCompiler compiler){
        // on retourne un immediate integer avec 1 pour true et 0 pour false
        int value = this.getValue() ? 1 : 0;
        return new ImmediateInteger(value);

    }


    @Override
protected void codeGenVrai(DecacCompiler compiler, Label E) {
    // @Ousmane : Si c'est 'true', on branche directement vers l'étiquette Vrai
    if (this.getValue()) {
        compiler.addInstruction(new BRA(E));
    }
    // Si la valeur est false, Code(false, vrai, E) ne génère rien (on continue)
}

@Override
protected void codeGenFaux(DecacCompiler compiler, Label E) {
    // @Ousmane : Si c'est 'false', on branche directement vers l'étiquette Faux
    if (!this.getValue()) {
        compiler.addInstruction(new BRA(E));
    }
    // Si la valeur est true, Code(true, faux, E) est vide (ε) selon le poly
}

    @Override
    public int simuleExecutionNbRegistres(DecacCompiler compiler){
        return 0;
    }

}
