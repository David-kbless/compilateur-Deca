package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.instructions.BOV;
import fr.ensimag.ima.pseudocode.instructions.LOAD;

import java.io.PrintStream;
import org.apache.commons.lang.Validate;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.context.ContextualError;



/**
 * Unary expression.
 *
 * @author gl54
 * @date 01/01/2026
 */
public abstract class AbstractUnaryExpr extends AbstractExpr {

    public AbstractExpr getOperand() {
        return operand;
    }
    private AbstractExpr operand;
    public AbstractUnaryExpr(AbstractExpr operand) {
        Validate.notNull(operand);
        this.operand = operand;
    }


    protected abstract String getOperatorName();
  
    @Override
    public void decompile(IndentPrintStream s) {
        s.print(getOperatorName());
        operand.decompile(s);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        operand.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        operand.prettyPrint(s, prefix, true);
    }

    public boolean isUnaryOp(){
        return true;
    }

    protected abstract Type typeUnaryOp(DecacCompiler compiler,Type t) throws ContextualError;


    @Override
    protected GPRegister codeGenInst(DecacCompiler compiler){
        DVal tmpOperande = getOperand().codeGenInst(compiler);

        // si l'operande n'est pas dans un registre, on la charge dans un registre
        GPRegister operande;
        if(tmpOperande instanceof GPRegister == false){
            operande = compiler.utiliserRegistre();
            compiler.addInstruction(new LOAD(tmpOperande, operande));
        }else{
            operande = (GPRegister)tmpOperande;
        }
        
        GPRegister reg = codeGenOperationUnaire(compiler, operande);

        if (!compiler.getCompilerOptions().getNoCheck() && getType().isFloat()) {
            compiler.addInstruction(new BOV(new Label("overflow_error")));
            compiler.addOverflowLabel();
        }

        return reg;
    }

    @Override
    public int simuleExecutionNbRegistres(DecacCompiler compiler){
        return getOperand().simuleExecutionNbRegistres(compiler);
    }

    protected abstract GPRegister codeGenOperationUnaire(DecacCompiler compiler, GPRegister operande);

}
