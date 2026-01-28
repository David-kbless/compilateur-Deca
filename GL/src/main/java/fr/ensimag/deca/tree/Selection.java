package fr.ensimag.deca.tree;

import java.io.PrintStream;

import org.apache.commons.lang.Validate;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.FieldDefinition;
import fr.ensimag.deca.context.ClassType;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.ExpDefinition;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.DAddr;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.NullOperand;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.BEQ;
import fr.ensimag.ima.pseudocode.instructions.CMP;
import fr.ensimag.ima.pseudocode.instructions.LOAD;

public class Selection extends AbstractLValue {
    private final AbstractExpr object;
    private final AbstractIdentifier fieldName;

    public Selection(AbstractExpr expr, AbstractIdentifier fieldName) {
        Validate.notNull(expr);
        Validate.notNull(fieldName);
        this.object = expr;
        this.fieldName = fieldName;
    }

    public AbstractExpr getObject() {
        return object;
    }

    public AbstractIdentifier getFieldName() {
        return fieldName;
    }

    public void setLocation(Location location) {
        super.setLocation(location);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        Type typeClass2 = object.verifyExpr(compiler, localEnv, currentClass);
        if (!typeClass2.isClass()) {
            throw new ContextualError("The object is not of class type", getLocation());
        }
        ClassDefinition defClass2 = typeClass2.asClassType("The object is not of class type", getLocation()).getDefinition();
        EnvironmentExp envExp2 = defClass2.getMembers();
        ExpDefinition def = fieldName.verifyIdent(envExp2);
        if (!def.isField()) {
            throw new ContextualError("The field is not defined", getLocation());
        }
        FieldDefinition fieldDef = def.asFieldDefinition("The field is not defined", getLocation());
        Visibility vis = fieldDef.getVisibility();
        if (vis == Visibility.PROTECTED) {
            ClassDefinition classField = fieldDef.getContainingClass();
            ClassType typeClass = typeClass2.asClassType("could not convert to ClassType", getLocation());
            ClassType typeClassField = classField.getType().asClassType("could not convert to ClassType", getLocation());
            if (currentClass == null){
                throw new ContextualError("The field " + fieldName.getName() + " is protected and not accessible in main", getLocation());
            }
            ClassType typeClassCurrent = currentClass.getType().asClassType("could not convert to ClassType", getLocation());
            if (!typeClass.isSubClassOf(typeClassCurrent) && typeClassCurrent.isSubClassOf(typeClassField)) {
                throw new ContextualError("The field " + fieldName.getName() + " is not accessible", getLocation());
            }
            
        }
        setType(def.getType());
        return def.getType();
    }

    @Override
    public void decompile(IndentPrintStream s) {
        getObject().decompile(s);
        s.print(".");
        getFieldName().decompile(s);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        object.prettyPrint(s, prefix, false);
        fieldName.prettyPrint(s, prefix, true);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        object.iter(f);
        fieldName.iter(f);
    }

    // @Ousmane
    @Override
    protected DVal codeGenInst(DecacCompiler compiler) {

        DVal reg = object.codeGenInst(compiler);

        // on verfie le derefencemeent nulle :
        compiler.addInstruction(new CMP(new NullOperand(), (GPRegister)reg));
        compiler.addInstruction(new BEQ(new Label("dereferencement.null")));
        compiler.addDereferencementNullLabel();

        // on charge enfin le field dans reg
        compiler.addInstruction(new LOAD(new RegisterOffset(fieldName.getFieldDefinition().getIndex() + 1, (GPRegister)reg), (GPRegister)reg));
        
        return reg;
    }

    

    @Override
    public int simuleExecutionNbRegistres(DecacCompiler compiler){
        return object.simuleExecutionNbRegistres(compiler);
    }

    @Override
    public DAddr getDAddr(DecacCompiler compiler) {

       DVal reg = object.codeGenInst(compiler);

        // on verfie le derefencemeent nulle :
        compiler.addInstruction(new CMP(new NullOperand(), (GPRegister)reg));
        compiler.addInstruction(new BEQ(new Label("dereferencement.null")));
        compiler.addDereferencementNullLabel();
        
        RegisterOffset offset = new RegisterOffset(fieldName.getFieldDefinition().getIndex() + 1, (GPRegister)reg);
        compiler.libererRegistre();
        
        return offset;
    }
}
