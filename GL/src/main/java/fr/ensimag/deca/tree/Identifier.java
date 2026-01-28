package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.context.TypeDefinition;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.Definition;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.EnvironmentType;
import fr.ensimag.deca.context.FieldDefinition;
import fr.ensimag.deca.context.MethodDefinition;
import fr.ensimag.deca.context.ExpDefinition;
import fr.ensimag.deca.context.VariableDefinition;
import fr.ensimag.deca.tools.DecacInternalError;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.ima.pseudocode.DAddr;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.BEQ;
import fr.ensimag.ima.pseudocode.instructions.BNE;
import fr.ensimag.ima.pseudocode.instructions.CMP;
import fr.ensimag.ima.pseudocode.instructions.LOAD;

import java.io.PrintStream;
import org.apache.commons.lang.Validate;
            
/**
 * Deca Identifier
 *
 * @author gl54
 * @date 01/01/2026
 */
public class Identifier extends AbstractIdentifier {

    public DAddr getDAddr(DecacCompiler compiler) {
        return this.getVariableDefinition().getOperand();
    }
    
    @Override
    protected void checkDecoration() {
        if (getDefinition() == null) {
            throw new DecacInternalError("Identifier " + this.getName() + " has no attached Definition");
        }
    }

    @Override
    public Definition getDefinition() {
        return definition;
    }

    /**
     * Like {@link #getDefinition()}, but works only if the definition is a
     * ClassDefinition.
     * 
     * This method essentially performs a cast, but throws an explicit exception
     * when the cast fails.
     * 
     * @throws DecacInternalError
     *             if the definition is not a class definition.
     */
    @Override
    public ClassDefinition getClassDefinition() {
        try {
            return (ClassDefinition) definition;
        } catch (ClassCastException e) {
            throw new DecacInternalError(
                    "Identifier "
                            + getName()
                            + " is not a class identifier, you can't call getClassDefinition on it");
        }
    }

    /**
     * Like {@link #getDefinition()}, but works only if the definition is a
     * MethodDefinition.
     * 
     * This method essentially performs a cast, but throws an explicit exception
     * when the cast fails.
     * 
     * @throws DecacInternalError
     *             if the definition is not a method definition.
     */
    @Override
    public MethodDefinition getMethodDefinition() {
        try {
            return (MethodDefinition) definition;
        } catch (ClassCastException e) {
            throw new DecacInternalError(
                    "Identifier "
                            + getName()
                            + " is not a method identifier, you can't call getMethodDefinition on it");
        }
    }

    /**
     * Like {@link #getDefinition()}, but works only if the definition is a
     * FieldDefinition.
     * 
     * This method essentially performs a cast, but throws an explicit exception
     * when the cast fails.
     * 
     * @throws DecacInternalError
     *             if the definition is not a field definition.
     */
    @Override
    public FieldDefinition getFieldDefinition() {
        try {
            return (FieldDefinition) definition;
        } catch (ClassCastException e) {
            throw new DecacInternalError(
                    "Identifier "
                            + getName()
                            + " is not a field identifier, you can't call getFieldDefinition on it");
        }
    }

    /**
     * Like {@link #getDefinition()}, but works only if the definition is a
     * VariableDefinition.
     * 
     * This method essentially performs a cast, but throws an explicit exception
     * when the cast fails.
     * 
     * @throws DecacInternalError
     *             if the definition is not a field definition.
     */
    @Override
    public VariableDefinition getVariableDefinition() {
        try {
            return (VariableDefinition) definition;
        } catch (ClassCastException e) {
            throw new DecacInternalError(
                    "Identifier "
                            + getName()
                            + " is not a variable identifier, you can't call getVariableDefinition on it");
        }
    }

    /**
     * Like {@link #getDefinition()}, but works only if the definition is a ExpDefinition.
     * 
     * This method essentially performs a cast, but throws an explicit exception
     * when the cast fails.
     * 
     * @throws DecacInternalError
     *             if the definition is not a field definition.
     */
    @Override
    public ExpDefinition getExpDefinition() {
        try {
            return (ExpDefinition) definition;
        } catch (ClassCastException e) {
            throw new DecacInternalError(
                    "Identifier "
                            + getName()
                            + " is not a Exp identifier, you can't call getExpDefinition on it");
        }
    }

    @Override
    public void setDefinition(Definition definition) {
        this.definition = definition;
    }

    @Override
    public Symbol getName() {
        return name;
    }

    private Symbol name;

    public Identifier(Symbol name) {
        Validate.notNull(name);
        this.name = name;
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {

        ExpDefinition def = localEnv.get(getName());
        if (def == null) {
            throw new ContextualError("Identifier " + getName() + " is unknown", getLocation());
        }

        // SEULES ces définitions sont assignables, règles (3.67) (3.68) et (3.69)
        if (!(def.isExpression() || def.isField() || def.isParam())) {
            throw new ContextualError("Identifier " + getName() + " is not assignable", getLocation());
        }

        setDefinition(def);
        setType(def.getType());
        return def.getType();
    }

    @Override
    public ExpDefinition verifyIdent(EnvironmentExp localEnv) throws ContextualError{
        ExpDefinition definition = localEnv.get(getName());
        if(definition == null){
            throw new ContextualError("identifier " + name + " is unkown", this.getLocation());
        }
        
        Type type = definition.getType();
        this.setDefinition(definition);
        this.setType(type);

        return definition;
    }

    // @Override
    // public Type verifyLValue(DecacCompiler compiler,
    //                      EnvironmentExp localEnv,
    //                      ClassDefinition currentClass)
    //     throws ContextualError {

    //     ExpDefinition def = localEnv.get(getName());
    //     if (def == null) {
    //         throw new ContextualError("Identifier " + getName() + " is unknown", getLocation());
    //     }

    //     // SEULES ces définitions sont assignables
    //     if (!(def.isExpression() || def.isField())) {
    //         throw new ContextualError("Identifier " + getName() + " is not assignable", getLocation());
    //     }

    //     setDefinition(def);
    //     setType(def.getType());
    //     return def.getType();
    // }


    /**
     * Implements non-terminal "type" of [SyntaxeContextuelle] in the 3 passes
     * @param compiler contains "env_types" attribute
     */
    @Override
    public Type verifyType(DecacCompiler compiler) throws ContextualError {
        //LOG.debug("verify Type: start");
        
        EnvironmentType envTypes = compiler.environmentType;
        TypeDefinition definition = envTypes.defOfType(name);
        
        if(definition == null){
            //LOG.debug("verify Type: un type n'a pas été ajouté à envTypes");
            throw new ContextualError("Type " + name + " is unkown", getLocation());
        }
        
        Type type = definition.getType();
        this.setDefinition(definition);

        //LOG.debug("verify Type: end");

        return type;
    }
    
    
    private Definition definition;


    @Override
    protected void iterChildren(TreeFunction f) {
        // leaf node => nothing to do
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        // leaf node => nothing to do
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print(name.toString());
    }

    @Override
    String prettyPrintNode() {
        return "Identifier (" + getName() + ")";
    }

    @Override
    protected void prettyPrintType(PrintStream s, String prefix) {
        Definition d = getDefinition();
        if (d != null) {
            s.print(prefix);
            s.print("definition: ");
            s.print(d);
            s.println();
        }
    }

    // @Override
    // public GPRegister codeGenInst(DecacCompiler compiler) {
    //     GPRegister reg = compiler.utiliserRegistre();
        
    //     // on charge d'abord le daddr 
    //     DAddr daddr = this.getVariableDefinition().getOperand();

    //     // puis on on charge dans le registre reg :
    //     compiler.addInstruction(new LOAD( daddr, reg));
    //     return reg;
    // }

    @Override
    public GPRegister codeGenInst(DecacCompiler compiler) {
        GPRegister reg = compiler.utiliserRegistre();
        ExpDefinition def = this.getExpDefinition();

        if (def.isField()) {
            // s'il s'agit d'un champs 
            FieldDefinition fieldDef = (FieldDefinition) def;
            int offset = fieldDef.getIndex() + 1;

            // on charge this dans un registre
            GPRegister regThis = compiler.utiliserRegistre();
            compiler.addInstruction(new LOAD(new RegisterOffset(-2, Register.LB), regThis));
            
            // On charge la valeur du champ à partir de l'adresse de l'objet
            compiler.addInstruction(new LOAD(new RegisterOffset(offset, regThis), reg));
            // on libère le registre utilisé pour this
            compiler.libererRegistre();
        }else {
            // si c'est une variable globale ou local : 
            DAddr daddr = def.getOperand();
            compiler.addInstruction(new LOAD(daddr, reg));
        }
        
        return reg;
    }


    @Override
protected void codeGenVrai(DecacCompiler compiler, fr.ensimag.ima.pseudocode.Label E) {
    // @Ousmane : On évalue l'identificateur par valeur d'abord
    DVal reg = this.codeGenInst(compiler);

    reg = verifRegister(compiler, reg);
    // @Ousmane : Comparaison avec faux (#0) [cite: 87, 89]
    compiler.addInstruction(new CMP(0, (GPRegister)reg));
    // @Ousmane : Branchement si vrai (différent de 0) [cite: 89]
    compiler.addInstruction(new BNE(E));
    compiler.libererRegistre();
}

@Override
protected void codeGenFaux(DecacCompiler compiler, fr.ensimag.ima.pseudocode.Label E) {
    DVal reg = this.codeGenInst(compiler);

    reg = verifRegister(compiler, reg);
    compiler.addInstruction(new CMP(0, (GPRegister)reg));
    // @Ousmane : Branchement si faux (égal à 0) 
    compiler.addInstruction(new BEQ(E));
    compiler.libererRegistre();
}

    @Override
    public int simuleExecutionNbRegistres(DecacCompiler compiler){
        if (getExpDefinition() != null && this.getExpDefinition().isField()) {
            compiler.simulerAllocationRegistre(); // for this
            return compiler.simulerAllocationRegistre(); // for the field value
        } else {
            return compiler.simulerAllocationRegistre(); // for the variable value
        }
    }

}
