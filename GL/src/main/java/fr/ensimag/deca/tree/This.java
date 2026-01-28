package fr.ensimag.deca.tree;

import java.io.PrintStream;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.DAddr;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.LOAD;


public class This extends AbstractLValue {
    public This() {
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        if (currentClass == null)
            throw new ContextualError("this cannot be used outside of a class", this.getLocation());
        if (currentClass.getType() == compiler.environmentType.OBJECT)
            throw new ContextualError("this cannot be used on object class", this.getLocation());
        this.setType(currentClass.getType());
        return currentClass.getType();
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("this");
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        // No children to print
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        // No children to iterate over
    }
    

    // @Ousmane
    @Override
    protected GPRegister codeGenInst(DecacCompiler compiler) {
        
        GPRegister reg = compiler.utiliserRegistre();
        compiler.addInstruction(new LOAD(new RegisterOffset(-2, Register.LB), reg));
        return reg;
    }

    @Override
    public DAddr getDAddr(DecacCompiler compiler) {
        return new RegisterOffset(-2, Register.LB);
    }

    @Override
    public int simuleExecutionNbRegistres(DecacCompiler compiler) {
        return compiler.simulerAllocationRegistre();
    }
}
