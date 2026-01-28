package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.EnvironmentExp.DoubleDefException;
import fr.ensimag.deca.context.ExpDefinition;
import fr.ensimag.deca.context.FieldDefinition;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.STORE;

import org.apache.commons.lang.Validate;

public class DeclField extends AbstractDeclField {
    private Visibility v;
    private AbstractIdentifier type;
    private final AbstractIdentifier fieldName;
    private final AbstractInitialization initialization;


    public AbstractInitialization getInitialization() {
        return initialization;
    }
    

    public DeclField(AbstractIdentifier fieldName, AbstractInitialization initialization) {
        Validate.notNull(fieldName);
        Validate.notNull(initialization);

        this.fieldName = fieldName;
        this.initialization = initialization;
    }
    
    public void setVisibility(Visibility v) {
        this.v = v;
    }

    public void setType(AbstractIdentifier type) {
        this.type = type;
    }

    public AbstractIdentifier getType(){
        return type;
    }

    public AbstractIdentifier getFieldName() {
        return fieldName;
    }

    @Override
    protected Visibility verifyDeclField(DecacCompiler compiler, ClassDefinition superClass, ClassDefinition currentClass, EnvironmentExp envExpR) throws ContextualError {
        Type realType = type.verifyType(compiler);
        if (realType.isVoid()){
            throw new ContextualError("A variable cannot have type void", type.getLocation()); //implémentation de la condition
        }
        EnvironmentExp classEnv = currentClass.getMembers();
        EnvironmentExp superClassEnv = superClass.getMembers();
        Symbol fieldNameSymbol = fieldName.getName();
        ExpDefinition fieldNameDef = classEnv.getLocal(fieldNameSymbol);
        ExpDefinition superFieldDef = superClassEnv.getLocal(fieldNameSymbol);
        int index = currentClass.getNumberOfFields() ;  
        FieldDefinition realFieldDef;
        if (fieldNameDef != null) {
                throw new ContextualError("The name " + fieldNameSymbol.getName() + " is already used ", fieldName.getLocation());
            // realFieldDef = fieldNameDef.asFieldDefinition(" Could not convert " + fieldNameDef + " to a FieldDefinition", getLocation());
        }
        if (superFieldDef != null) {
            if (!superFieldDef.isField()) {
                throw new ContextualError("The name " + fieldNameSymbol.getName() + " is already used in super class and is not a field ", fieldName.getLocation());
            }
        }
        
        realFieldDef = new FieldDefinition(realType, getLocation(), v, currentClass, index);

        try {
            envExpR.declare(fieldNameSymbol, realFieldDef);
        } catch (DoubleDefException e) {
            //si elle avait deja été declarée -> contextualerror
            throw new ContextualError("Variable " + fieldName.getName() + " is already defined in this scope",fieldName.getLocation());
        }
        currentClass.incNumberOfFields();
        fieldName.setDefinition(realFieldDef);
        
        return v;

    }

    @Override
    protected void verifyFieldBody(DecacCompiler compiler, ClassDefinition currentClass, EnvironmentExp envExp)
            throws ContextualError {

                // @safwane !!!!
                // encore un oublie de ta part et tu sera sanction : 
                // signé @Ousmane !!
        FieldDefinition fieldDef = fieldName.getFieldDefinition();
    
        Type type = fieldDef.getType();
        initialization.verifyInitialization(compiler, type, envExp, currentClass);
    }
        
    
    @Override
    public void decompile(IndentPrintStream s) {
        if (v != null) {
            v.decompile(s);
            s.print(" ");
        }
        if (type != null) {
            type.decompile(s);
            s.print(" ");
        }
        fieldName.decompile(s);
        s.print(" ");
        if (!(initialization instanceof NoInitialization)) {
            s.print(" = ");
            initialization.decompile(s);
        }
        s.print(";");
    }

    @Override
    protected void codeGenDeclField(DecacCompiler compiler) {

                DVal expr = getInitialization().codeGen(compiler);

                // s'il ya bien une initialisation, alors elle ecrase ce qui est deja dans le champs de ce objet
                if(expr != null){
                    compiler.addInstruction(new STORE((GPRegister) expr, new RegisterOffset(getFieldName().getFieldDefinition().getIndex() + 1, Register.getR(2))));

                    // on libere le registre utiliser pour. expr dans ce cas
                    compiler.libererRegistre();
                }
    }

    public int simuleExecutionNbRegistres(DecacCompiler compiler){
        int nbRegistres = 0;
        nbRegistres = getInitialization().simuleExecutionNbRegistresField(compiler);
        
        if(nbRegistres > 0){
            compiler.simulerLiberationRegistre();
        }

        return nbRegistres;
    }

    @Override
    protected void prettyPrintChildren(java.io.PrintStream s, String prefix) {
        fieldName.prettyPrint(s, prefix, false);
        // Print the initialization if it exists
        if (initialization != null) {
            initialization.prettyPrint(s, prefix, true);
        }
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        fieldName.iter(f);
        initialization.iter(f);
    }
}
