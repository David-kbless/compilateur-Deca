package fr.ensimag.deca.tree;

import java.io.PrintStream;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.BRA;
import fr.ensimag.ima.pseudocode.instructions.LOAD;

public class Return extends AbstractInst {
    private final AbstractExpr expr;

    public Return(AbstractExpr expr) {
        this.expr = expr;
    }
    @Override
    public void decompile(fr.ensimag.deca.tools.IndentPrintStream s) {
        s.print("return ");
        expr.decompile(s);
        s.print(";");
    }

    @Override
    protected void verifyInst(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass,
            Type returnType) throws ContextualError {
    
        if (returnType.isVoid()) {
            throw new ContextualError("Return statement not allowed in void methods", getLocation());
        }

        expr.verifyRValue(compiler, localEnv, currentClass, returnType);
    }

    @Override
    protected GPRegister codeGenInst(DecacCompiler compiler) {
        // on evalue l'expression tout dabord
        DVal reg = expr.codeGenInst(compiler);

        // puis on le met dans le reg RO
        compiler.addInstruction(new LOAD(reg, Register.R0));

        // on libere le registre 
        compiler.libererRegistre();

        // on rajoute le label fin : 
        compiler.addInstruction(new BRA(compiler.getCurrentLabel()));

        return Register.R0;

    }

    @Override
    public int simuleExecutionNbRegistres(DecacCompiler compiler){
        return expr.simuleExecutionNbRegistres(compiler);
    }


    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        expr.prettyPrint(s, prefix, true);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        f.apply(this.expr);
    }

}
