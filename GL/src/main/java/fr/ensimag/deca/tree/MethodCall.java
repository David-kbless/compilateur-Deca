package fr.ensimag.deca.tree;

import java.io.PrintStream;
import java.util.List;

import org.apache.commons.lang.Validate;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.ExpDefinition;
import fr.ensimag.deca.context.MethodDefinition;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.context.Signature;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.NullOperand;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.ADDSP;
import fr.ensimag.ima.pseudocode.instructions.BEQ;
import fr.ensimag.ima.pseudocode.instructions.BSR;
import fr.ensimag.ima.pseudocode.instructions.CMP;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.STORE;
import fr.ensimag.ima.pseudocode.instructions.SUBSP;

public class MethodCall extends AbstractExpr {
    private AbstractExpr object;
    private final AbstractIdentifier methodName;
    private final ListExpr arguments;

    public MethodCall(AbstractExpr object, AbstractIdentifier methodName, ListExpr arguments) {
        Validate.notNull(methodName);
        this.object = object;
        this.methodName = methodName;
        this.arguments = arguments;
    }
    public AbstractExpr getObject() {
        return object;
    }
    public AbstractIdentifier getMethodName() {
        return methodName;
    }
    public ListExpr getArguments() {
        return arguments;   
    }

    @Override
    public void setLocation(Location location){
        super.setLocation(location);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        EnvironmentExp envExp2;
        if (object!=null){
            Type typeClass2 = object.verifyExpr(compiler, localEnv, currentClass);
            if (!typeClass2.isClass()) {
                throw new ContextualError("The object is not of class type", getLocation());
            }
            envExp2 = typeClass2.asClassType("The object is not of class type", getLocation()).getDefinition().getMembers();
        }
        else if (currentClass != null) {
            this.object = new This();
            Type typeClass2 = object.verifyExpr(compiler, localEnv, currentClass);
            envExp2 = currentClass.getMembers();
        } 
        else {
            throw new ContextualError("A method call without an object cannot be used outside a class", getLocation());
        }
        ExpDefinition def = methodName.verifyIdent(envExp2);
        if (!def.isMethod()) {
            throw new ContextualError("The method is not defined", getLocation());
        }
        MethodDefinition methodDef = def.asMethodDefinition("The method is not defined", getLocation());
        Signature sig = methodDef.getSignature();

        List<AbstractExpr> args = arguments.getList(); // The rvalues

        // Verification of the number of arguments Vérifie nombre d'arguments
        if (args.size() != sig.size()) {
            throw new ContextualError("Wrong number of arguments: expected " + sig.size() +", got " + args.size(),getLocation());
        }

        // Verification of each argument with verifyRValue
        for (int i = 0; i < args.size(); i++) {
            args.get(i).verifyRValue(compiler, localEnv, currentClass, sig.paramNumber(i));
        }
        
        setType(methodDef.getType());
        return methodDef.getType();
    }



    @Override
    public void decompile(IndentPrintStream s) {
        if (getObject() != null) {
            getObject().decompile(s);
            s.print(".");
        }
        getMethodName().decompile(s);
        s.print("(");
        if (!getArguments().isEmpty()) {
            getArguments().decompile(s);
        }
        
        s.print(")");
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        if (object != null) {
            object.prettyPrint(s, prefix, false);
        }
        methodName.prettyPrint(s, prefix, false);
        arguments.prettyPrint(s, prefix, true);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        if (object != null) {
            object.iter(f);
        }
        methodName.iter(f);
        arguments.iter(f);
    }

    // @Ousmane
    @Override
    protected GPRegister codeGenInst(DecacCompiler compiler) {
        
        // On réserve de la place pour les trois paramètres
        int nbParams = arguments.getList().size() + 1; // +1 pour le paramètre implicite this
        compiler.addInstruction(new ADDSP(nbParams));

        // on empile le param implicite this (l'objet)
        DVal reg = object.codeGenInst(compiler);
        compiler.addInstruction(new STORE( (GPRegister)reg, new RegisterOffset(0, Register.SP)));
        compiler.libererRegistre();
        

        // on empile les autres paramètres
        List<AbstractExpr> args = arguments.getList();
        for (int i = 0; i < args.size(); i++) {
            boolean libereReg = true;
            reg = args.get(i).codeGenInst(compiler);

            // on gere le fait que  reg peut etre un immediat : 
            reg = verifRegister(compiler, reg);

            compiler.addInstruction(new STORE((GPRegister)reg, new RegisterOffset(-(i + 1), Register.SP)));
            if (libereReg) {
                compiler.libererRegistre();
            }
        }

        GPRegister regAppel = compiler.utiliserRegistre();

        // On récupère le paramètre implicite
        compiler.addInstruction(new LOAD(new RegisterOffset(0, Register.SP), regAppel));


        // On teste s'il est égal à null
        compiler.addInstruction(new CMP(new NullOperand(), regAppel));
        compiler.addDereferencementNullLabel();
        compiler.addInstruction(new BEQ(new Label("dereferencement.null")));



        // appel de la méthode
        MethodDefinition methodDef = methodName.getMethodDefinition();

        if (object instanceof Cast){
            // On récupère le label de la méthode dans la classe cible du Cast
            Cast cast = (Cast) object;
            Type targetType = cast.getType();


            // on verifie que le nom de la mehtode et de la classe et de la classe cible du cast ne depasse pas 1024 caracteres
            
            
            // On génère le label : code.NomClasse.NomMethode
        
            Label staticLabel = new Label("code." + targetType.getName().getName() + "." + methodName.getName().getName());
            compiler.addInstruction(new BSR(staticLabel));
        }else{
            //On récupère l'adresse de la table des méthodes
            compiler.addInstruction(new LOAD(new RegisterOffset(0, regAppel), regAppel));

            compiler.addInstruction(new BSR(new RegisterOffset(methodDef.getIndex() + 1, regAppel)));

        }

        // on depile les parametres : 
        compiler.addInstruction(new SUBSP(nbParams));
        


        // liberation du registre
        compiler.libererRegistre();

        // on retourne R0 d'après la spé : @arevoir
        return Register.R0;
    }




    // @Ousmane , je dois implementer cette fonctionnalite

    @Override
    public int simuleExecutionNbRegistres(DecacCompiler compiler){
        int maxReg = 0;

        // On simule l'évaluation de l'objet de manière isolée.
        compiler.commencerSimulationRegistres(true);
        if (object != null) {
            maxReg = object.simuleExecutionNbRegistres(compiler);
        } else {
            // Si l'objet est null, c'est un appel sur 'this' implicite, qui nécessite un registre.
            maxReg = compiler.simulerAllocationRegistre();
        }
        compiler.resetGestionnaireRegistres(false);

        // On cherche le pic d'utilisation des registres parmi l'évaluation de chaque argument.
        // Chaque argument est évalué indépendamment.
        for (AbstractExpr a : arguments.getList()) {
            compiler.commencerSimulationRegistres(true);
            int argRegs = a.simuleExecutionNbRegistres(compiler);
            if (argRegs > maxReg) {
                maxReg = argRegs;
            }
            compiler.resetGestionnaireRegistres(false);
        }

        return maxReg;
    }
}
