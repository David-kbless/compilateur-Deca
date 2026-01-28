package fr.ensimag.deca.tree;

import java.io.PrintStream;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.DVal;

public class ExprInst extends AbstractInst {
    private final AbstractExpr expr;

    public ExprInst(AbstractExpr expr) {
        this.expr = expr;
    }

    @Override
    protected void verifyInst(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass,
            Type returnType) throws ContextualError {
        expr.verifyExpr(compiler, localEnv, currentClass);
    }

    @Override
    protected DVal codeGenInst(DecacCompiler compiler) {
        return expr.codeGenInst(compiler);
        
    }

    @Override
    public int simuleExecutionNbRegistres(DecacCompiler compiler){
        return expr.simuleExecutionNbRegistres(compiler);
    }

    @Override
    public void decompile(IndentPrintStream s) {
        expr.decompile(s);
        s.println(";");
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        expr.prettyPrint(s, prefix, true);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        expr.iter(f);
    }
}