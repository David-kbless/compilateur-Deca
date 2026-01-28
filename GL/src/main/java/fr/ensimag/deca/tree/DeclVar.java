package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.context.VariableDefinition;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.EnvironmentExp.DoubleDefException;
import fr.ensimag.deca.context.ExpDefinition;
import fr.ensimag.deca.tools.IndentPrintStream;
import java.io.PrintStream;
import org.apache.commons.lang.Validate;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.STORE;
/**
 * @author gl54
 * @date 01/01/2026
 */
public class DeclVar extends AbstractDeclVar {

    
    final private AbstractIdentifier type;
    final private AbstractIdentifier varName;
    final private AbstractInitialization initialization;

    public DeclVar(AbstractIdentifier type, AbstractIdentifier varName, AbstractInitialization initialization) {
        Validate.notNull(type);
        Validate.notNull(varName);
        Validate.notNull(initialization);
        this.type = type;
        this.varName = varName;
        this.initialization = initialization;
    }

    public AbstractIdentifier getType() {
        return type;
    }

    public AbstractIdentifier getVarName() {
        return varName;
    }
    public AbstractInitialization getInitialization() {
        return initialization;
    }

    @Override
    protected void verifyDeclVar(DecacCompiler compiler,
            EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        
        // On commence par traiter le non terminal Type qui nous renvoie le type qu'on déclare, ce type normalement a déjà été ajouté 
        // à l'environnment dans l'une des passes précédentes
        Type realType = type.verifyType(compiler);
        if (realType.isVoid()){
            throw new ContextualError("A variable cannot have type void", type.getLocation()); //implémentation de la condition
        }

        //On décore la variable déclarée
        VariableDefinition varDef = new VariableDefinition(realType, varName.getLocation());
        varName.setDefinition(varDef);
        Symbol name = varName.getName();
        Validate.notNull(name);

        /*Sinon on traite l'initialisation de la variable, normalement le type dynamique a été défini au niveau de l'étape A*/
        // Type t = varDef.getType();
        if (currentClass == null) {
            initialization.verifyInitialization(compiler, realType, localEnv, currentClass);
        }
        else {
            initialization.verifyInitialization(compiler, realType, EnvironmentExp.stack(localEnv, currentClass.getMembers()), currentClass);
        }
        
        try {
            localEnv.declare(name, varDef);
        } catch (DoubleDefException e) {
            //si elle avait deja été declarée -> contextualerror
            throw new ContextualError("Variable " + varName.getName() + " is already defined in this scope",varName.getLocation());
        }
        
        
    }
    

    
    @Override
    public void decompile(IndentPrintStream s) {
        getType().decompile(s);
        s.print(" ");
        getVarName().decompile(s);
        if (!(getInitialization() instanceof NoInitialization)) {
            s.print(" = ");
            getInitialization().decompile(s);
        }
    }

    @Override
    protected
    void iterChildren(TreeFunction f) {
        type.iter(f);
        varName.iter(f);
        initialization.iter(f);
    }
    
    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        type.prettyPrint(s, prefix, false);
        varName.prettyPrint(s, prefix, false);
        initialization.prettyPrint(s, prefix, true);
    }


    public void codeGenDecl(DecacCompiler compiler) {
    // Si on a une initialisation (ex: = 1)
    if (!(initialization instanceof NoInitialization)) {
        // On évalue l'expression et on met le résultat dans R0
        // initialization est une AbstractInitialization
        DVal reg = initialization.codeGen(compiler);
        reg = verifRegister(compiler, reg);
        
        // On récupère l'offset (ex: 1(LB)) qu'on a fixé précédemment
        ExpDefinition def = varName.getExpDefinition();
        
        // On stocke R0 à l'emplacement de la variable
        compiler.addInstruction(new STORE((GPRegister) reg, def.getOperand()));
    }
}
}
