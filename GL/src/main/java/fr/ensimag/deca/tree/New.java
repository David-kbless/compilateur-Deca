package fr.ensimag.deca.tree;
import java.io.PrintStream;

import org.apache.commons.lang.Validate;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.BOV;
import fr.ensimag.ima.pseudocode.instructions.BSR;
import fr.ensimag.ima.pseudocode.instructions.LEA;
import fr.ensimag.ima.pseudocode.instructions.NEW;
import fr.ensimag.ima.pseudocode.instructions.POP;
import fr.ensimag.ima.pseudocode.instructions.PUSH;
import fr.ensimag.ima.pseudocode.instructions.STORE;
public class New extends AbstractExpr {
    private final AbstractIdentifier type;

    public New(AbstractIdentifier type) {
        Validate.notNull(type);
        this.type = type;
    }
    public AbstractIdentifier getTypeIdentifier() {
        return type;
    }

    @Override
    public void setLocation(Location location){
        super.setLocation(location);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        Type type1 = type.verifyType(compiler);
        if (!type1.isClass()) {
            throw new ContextualError("The type " + type1 + " is not a class type", getLocation());
        }
        setType(type1);
        return type1;
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("new ");
        getTypeIdentifier().decompile(s);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        getTypeIdentifier().prettyPrint(s, prefix, false);
    }
    
    @Override
    protected void iterChildren(TreeFunction f) {
        type.iter(f);
    }

    @Override
    protected GPRegister codeGenInst(DecacCompiler compiler) {
        GPRegister reg = compiler.utiliserRegistre();
        ClassDefinition defClass = type.getClassDefinition();


        // Allocation
        compiler.addInstruction(new NEW(defClass.getNumberOfFields() + 1, reg));
        compiler.addTasOverflowLabel();
        compiler.addInstruction(new BOV(new Label("tas_plein")));

        // VTable
        GPRegister tas = compiler.utiliserRegistre();
        compiler.addInstruction(new LEA(defClass.getVTableAddr(), tas));
        compiler.addInstruction(new STORE(tas, new RegisterOffset(0, reg)));
        compiler.libererRegistre();

        // Appel INIT
        compiler.addInstruction(new PUSH(reg));
        compiler.addInstruction(new BSR(defClass.getLabelInit()));
        

        // On récupère le résultat du NEW (l'objet) tout de suite
        compiler.addInstruction(new POP(reg)); 


        return reg;
    }
    
    // @Ousmane , je dois implementer cette fonctionnalite

    @Override
    public int simuleExecutionNbRegistres(DecacCompiler compiler){
        return compiler.simulerAllocationRegistre() + compiler.simulerAllocationRegistre(); // +1 pour l'objet lui meme
    }
}
