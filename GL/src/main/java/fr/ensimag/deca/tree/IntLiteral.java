package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.ImmediateInteger;

import java.io.PrintStream;
import java.math.BigInteger;


/**
 * Integer literal
 *
 * @author gl54
 * @date 01/01/2026
 */
public class IntLiteral extends AbstractExpr {
    private final BigInteger value;

    public IntLiteral(BigInteger value) {
        this.value = value;
    }

    public int getValue() {
        return value.intValue();
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {

        // Verification of the the 3é signed bits limits
        if (value.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0) {
            throw new ContextualError("Literal entier trop grand : " + value + 
                " (max: " + Integer.MAX_VALUE + ")", getLocation());
        }
        // Managing of the negative case too for more security
        if (value.compareTo(BigInteger.valueOf(Integer.MIN_VALUE)) < 0) {
             throw new ContextualError("Literal entier trop petit : " + value + 
                " (min: " + Integer.MIN_VALUE + ")", getLocation());
        }
        Type t = compiler.environmentType.INT;
        this.setType(t);
        return t;
    }

    @Override
    public String toString() {
        return value.toString();
    }

    @Override
    String prettyPrintNode() {
        return "Int (" + getValue() + ")";
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print(value.toString());
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
    protected DVal codeGenInst(DecacCompiler compiler) {
        
        // on retourne un immediate integer
        return new ImmediateInteger(getValue());
    }
    
    @Override
    public int simuleExecutionNbRegistres(DecacCompiler compiler){
        return 0;
    }

}
