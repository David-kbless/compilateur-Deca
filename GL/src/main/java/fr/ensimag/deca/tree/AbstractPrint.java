package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
// import fr.ensimag.deca.context.FloatType;
// import fr.ensimag.deca.context.IntType;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;
// import fr.ensimag.ima.pseudocode.Label;
import java.io.PrintStream;
import org.apache.commons.lang.Validate;
import fr.ensimag.ima.pseudocode.GPRegister;

/**
 * Print statement (print, println, ...).
 *
 * @author gl54
 * @date 01/01/2026
 */
public abstract class AbstractPrint extends AbstractInst {

    private boolean printHex;
    private ListExpr arguments = new ListExpr();
    
    abstract String getSuffix();

    public AbstractPrint(boolean printHex, ListExpr arguments) {
        Validate.notNull(arguments);
        this.arguments = arguments;
        this.printHex = printHex;
    }

    public ListExpr getArguments() {
        return arguments;
    }

    @Override
    protected void verifyInst(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass, Type returnType)
            throws ContextualError {
                
            for (AbstractExpr expPrint : getArguments().getList()) {
                Type t = expPrint.verifyExpr(compiler, localEnv, currentClass); // Retrieving the synthesized attribut
                
                
                if(!t.isString() && !t.isInt() && !t.isFloat()){ // Verifications on the types
                    throw new ContextualError("Only strings, ints and floats can be printed", getLocation());
                }
            }
    }

    @Override
    protected GPRegister codeGenInst(DecacCompiler compiler) {
        for (AbstractExpr a : getArguments().getList()) {
            a.codeGenPrint(compiler, getPrintHex());
        }

        return null;
    }

    @Override
    public int simuleExecutionNbRegistres(DecacCompiler compiler){
        int max = 0;

        for (AbstractExpr a : getArguments().getList()) {
            int nb = a.simuleExecutionNbRegistres(compiler);
            if(nb > max){
                max = nb;
            }
        }

        return max;
    }


    private boolean getPrintHex() {
        return printHex;
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("print");;
        s.print(getSuffix());
        if(printHex){
            s.print("x");
        }
        s.print("(");
        arguments.decompile(s);
        s.print(");");
        
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        arguments.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        arguments.prettyPrint(s, prefix, true);
    }

}
