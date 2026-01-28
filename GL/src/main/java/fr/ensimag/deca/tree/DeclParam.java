package fr.ensimag.deca.tree;
import java.io.PrintStream;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.context.ParamDefinition;
import fr.ensimag.deca.context.ExpDefinition;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;

import org.apache.commons.lang.Validate;

public class DeclParam extends AbstractDeclParam {
    private final AbstractIdentifier type;
    private final AbstractIdentifier name;

    public DeclParam(AbstractIdentifier type, AbstractIdentifier name) {
        Validate.notNull(type);
        Validate.notNull(name);
        this.type = type;
        this.name = name;
    }

    public AbstractIdentifier getType() {
        return type;
    }

    public AbstractIdentifier getName() {
        return name;
    }

    @Override
    protected Type verifyDeclParamMembers(DecacCompiler compiler, int index)
            throws ContextualError {
        // Implementation of the verification logic for the parameter declaration
        // This is a placeholder; actual implementation would depend on the compiler's context
        Type realType = type.verifyType(compiler);
        if (realType.isVoid()){
            throw new ContextualError("A parameter cannot have type void", type.getLocation()); //implémentation de la condition
        }

        //On décore la variable déclarée
        ParamDefinition paramDef = new ParamDefinition(realType, name.getLocation());
        name.setDefinition(paramDef);
        Symbol paramName = name.getName();
        Validate.notNull(paramName);
        paramDef.setOperand(new RegisterOffset(-index, Register.LB));
        return realType;
    }

    @Override
    protected EnvironmentExp verifyParamBody(EnvironmentExp env) throws ContextualError {
        EnvironmentExp envExp = new EnvironmentExp(null);
        ExpDefinition def = name.getExpDefinition();
        Symbol nameSymbol = name.getName();
        
        try {
            envExp.declare(nameSymbol, def);
        } catch (EnvironmentExp.DoubleDefException e) {
            throw new ContextualError("The name " + nameSymbol.getName() + " is already used", name.getLocation());
        }

        return envExp;
    }


    @Override
    public void decompile(IndentPrintStream s) {
        type.decompile(s);
        s.print(" ");
        name.decompile(s);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        type.prettyPrint(s, prefix, false);
        name.prettyPrint(s, prefix, true);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        type.iter(f);
        name.iter(f);
    }
}