package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.DecacInternalError;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.BEQ;
import fr.ensimag.ima.pseudocode.instructions.BNE;
import fr.ensimag.ima.pseudocode.instructions.CMP;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.WFLOAT;
import fr.ensimag.ima.pseudocode.instructions.WFLOATX;
import fr.ensimag.ima.pseudocode.instructions.WINT;
import fr.ensimag.ima.pseudocode.instructions.WSTR;

import java.io.PrintStream;
import org.apache.commons.lang.Validate;

import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Label;

/**
 * Expression, i.e. anything that has a value.
 *
 * @author gl54
 * @date 01/01/2026
 */
public abstract class AbstractExpr extends AbstractInst {
    /**
     * @return true if the expression does not correspond to any concrete token
     * in the source code (and should be decompiled to the empty string).
     */
    boolean isImplicit() {
        return false;
    }

    /**
     * Get the type decoration associated to this expression (i.e. the type computed by contextual verification).
     */
    public Type getType() {
        return type;
    }

    protected void setType(Type type) {
        Validate.notNull(type);
        this.type = type;
    }
    private Type type;

    @Override
    protected void checkDecoration() {
        if (getType() == null) {
            throw new DecacInternalError("Expression " + decompile() + " has no Type decoration");
        }
    }

    /**
     * Verify the expression for contextual error.
     * 
     * implements non-terminals "expr" and "lvalue" 
     *    of [SyntaxeContextuelle] in pass 3
     *
     * @param compiler  (contains the "env_types" attribute)
     * @param localEnv
     *            Environment in which the expression should be checked
     *            (corresponds to the "env_exp" attribute)
     * @param currentClass
     *            Definition of the class containing the expression
     *            (corresponds to the "class" attribute)
     *             is null in the main bloc.
     * @return the Type of the expression
     *            (corresponds to the "type" attribute)
     */
    public abstract Type verifyExpr(DecacCompiler compiler,
            EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError;

    /**
     * Verify the expression in right hand-side of (implicit) assignments 
     * 
     * implements non-terminal "rvalue" of [SyntaxeContextuelle] in pass 3
     *
     * @param compiler  contains the "env_types" attribute
     * @param localEnv corresponds to the "env_exp" attribute
     * @param currentClass corresponds to the "class" attribute
     * @param expectedType corresponds to the "type1" attribute            
     * @return this with an additional ConvFloat if needed...
     */
    public AbstractExpr verifyRValue(DecacCompiler compiler,
            EnvironmentExp localEnv, ClassDefinition currentClass, 
            Type expectedType)
            throws ContextualError {
        this.verifyExpr(compiler, localEnv, currentClass);
        Type type2 = this.verifyExpr(compiler, localEnv, currentClass);
        Assign.assignCompatible(expectedType, type2, getLocation());
        
        if(!expectedType.sameType(type2)) {
            if (type2.isInt() && expectedType.isFloat()) {
                // Convertion int -> float
                ConvFloat conv = new ConvFloat(this);
                conv.setLocation(getLocation());
                conv.verifyExpr(compiler, localEnv, currentClass);
                conv.setType(expectedType);
                return conv;
            }
        } 
        
        return this;
    }
    
    
    @Override
    protected void verifyInst(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass, Type returnType)
            throws ContextualError {
        // Elle implémente la règle (3.20) inst -> expr
        this.verifyExpr(compiler, localEnv, currentClass);
    }

    /**
     * Verify the expression as a condition, i.e. check that the type is
     * boolean.
     *
     * @param localEnv
     *            Environment in which the condition should be checked.
     * @param currentClass
     *            Definition of the class containing the expression, or null in
     *            the main program.
     */
    void verifyCondition(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        Type condType = this.verifyExpr(compiler, localEnv, currentClass);
        if (!condType.isBoolean()) {
            throw new ContextualError("Condition expression must be of boolean type",this.getLocation());
        }
    }

    /**
     * Generate code to print the expression
     *
     *
     * @param compiler
     */
    protected void codeGenPrint(DecacCompiler compiler, boolean printHex) {
        if (this instanceof StringLiteral) {
            compiler.addInstruction(new WSTR(((StringLiteral) this).getValue()));
        } else {
            // Polymorphisme : arg peut être un Identifier (x) ou un IntLiteral (10)
            DVal r = this.codeGenInst(compiler);
            
            // on met la valeur dans R1 pour les instructions de print
            compiler.addInstruction(new LOAD(r, Register.R1));
            
            if (this.getType().isInt()) {
                compiler.addInstruction(new WINT());
            } else {
                if(printHex){
                    compiler.addInstruction(new WFLOATX());
                }else{
                compiler.addInstruction(new WFLOAT());
                }
            }
            compiler.libererRegistre();// On libère le registre retourné par l'expression
        }
    }

    @Override
    protected abstract DVal codeGenInst(DecacCompiler compiler); 

    @Override
    protected void decompileInst(IndentPrintStream s) {
        decompile(s);
        s.print(";");
    }

    @Override
    protected void prettyPrintType(PrintStream s, String prefix) {
        Type t = getType();
        if (t != null) {
            s.print(prefix);
            s.print("type: ");
            s.print(t);
            s.println();
        }
    }


    /**
 * @Ousmane : Comportement par défaut pour le flux de contrôle [cite: 49, 52]
 */
protected void codeGenVrai(DecacCompiler compiler, Label E) {
    DVal reg = this.codeGenInst(compiler);

    if(reg instanceof GPRegister == false){
        compiler.addInstruction(new LOAD(reg, Register.R0));
        reg = Register.R0;
    }
    compiler.addInstruction(new CMP(0, (GPRegister)reg));
    compiler.addInstruction(new BNE(E));
    compiler.libererRegistre();
}

protected void codeGenFaux(DecacCompiler compiler, Label E) {
    DVal reg = this.codeGenInst(compiler);

    if(reg instanceof GPRegister == false){
        compiler.addInstruction(new LOAD(reg, Register.R0));
        reg = Register.R0;
    }
    compiler.addInstruction(new CMP(0, (GPRegister)reg));
    compiler.addInstruction(new BEQ(E));
    compiler.libererRegistre();
}

    protected DVal verifRegister(DecacCompiler compiler, DVal reg) {
        if (reg instanceof GPRegister) {
            return (GPRegister) reg;
        } else {
            GPRegister regGP = compiler.utiliserRegistre();
            compiler.addInstruction(new LOAD(reg, regGP));
            return regGP;
        }
    }
}
