package fr.ensimag.deca.tree;
import java.io.PrintStream;

import org.apache.commons.lang.Validate;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ClassType;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.NullOperand;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.BEQ;
import fr.ensimag.ima.pseudocode.instructions.BNE;
import fr.ensimag.ima.pseudocode.instructions.BRA;
import fr.ensimag.ima.pseudocode.instructions.CMP;
import fr.ensimag.ima.pseudocode.instructions.FLOAT;
import fr.ensimag.ima.pseudocode.instructions.INT;
import fr.ensimag.ima.pseudocode.instructions.LEA;
import fr.ensimag.ima.pseudocode.instructions.LOAD;

public class Cast extends AbstractExpr {
    private final AbstractIdentifier type;
    private final AbstractExpr expr;

    public Cast(AbstractIdentifier type, AbstractExpr expr) {
        Validate.notNull(type);
        Validate.notNull(expr);
        this.type = type;
        this.expr = expr;
    }
    public AbstractIdentifier getTypeId() {
        return type;
    }
    public AbstractExpr getExpr() {
        return expr;
    }
    public void setLocation(Location location) {
        super.setLocation(location);
    }
    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        Type t = getExpr().verifyExpr(compiler, localEnv, currentClass);
        Type typeId = getTypeId().verifyType(compiler);
        castCompatible(typeId, t);
        this.setType(typeId);
        return typeId;
    }
    @Override
    public void decompile(IndentPrintStream s) {
        s.print('(');
        getTypeId().decompile(s);
        s.print(") ");
        getExpr().decompile(s);
    }
    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        type.prettyPrint(s, prefix, true);
        expr.prettyPrint(s, prefix, false);
    }
    @Override
    protected void iterChildren(TreeFunction f) {
        type.iter(f);
        expr.iter(f);
    }




    // @Override
    // protected GPRegister codeGenInst(DecacCompiler compiler) {
    //     GPRegister reg = expr.codeGenInst(compiler);

    //     Type tOld = expr.getType();
    //     Type tNew = this.getType();
        

    //     if(tOld.isInt() && tNew.isFloat()){
    //         compiler.addInstruction(new FLOAT(reg, reg));
    //     }else if(tOld.isFloat() && tNew.isInt()){
    //         compiler.addInstruction(new INT(reg, reg));
    //     }

    //     // après validation par @Safwane, on verra s'il d'autres type de cast 
    //     // @Rappel : safwane
    //     return reg;
    // }



    @Override
    protected DVal codeGenInst(DecacCompiler compiler) {
        DVal reg = expr.codeGenInst(compiler);
        Type tOld = expr.getType();
        Type tNew = this.getType();

        reg = verifRegister(compiler, reg);

        //  Cast numérique : int < = > float
        if (tOld.isInt() && tNew.isFloat()) {
            compiler.addInstruction(new FLOAT(reg, (GPRegister) reg));
        } else if (tOld.isFloat() && tNew.isInt()) {
            compiler.addInstruction(new INT(reg, (GPRegister)reg));
        } 
        // Cast d'objets 
        else if (tOld.isClass() && tNew.isClass()) {
            ClassType from;
            ClassType to;
            try
            {
                from = tOld.asClassType("", getLocation());
                to = tNew.asClassType("", getLocation());
            }catch(ContextualError e)
            {
                // ne devrait jamais arriver
                throw new UnsupportedOperationException("Cast codeGenInst: impossible de caster en ClassType");
            }

            // Si c'est un Downcast (Parent -> Enfant), on doit vérifier à l'exécution
            if (to.isSubClassOf(from) ) {
                genRuntimeCastCheck(compiler, (GPRegister)reg, to);
            }
            // Si c'est un Upcast (Enfant -> Parent), on ne fait rien !
        }
        
        return reg;
    }

    private void genRuntimeCastCheck(DecacCompiler compiler, GPRegister regObj, ClassType targetType) {

        int labelId = compiler.getLabelCastId();
        Label startLoop = new Label("start_cast_" + labelId);
        Label endCast = new Label("end_cast_" + labelId);


        // on ajoute le flag pour générer le label d'erreur de cast
        compiler.addConversionImpossibleLabel();
        
        // Si l'objet est null, le cast est toujours valide en Deca
        compiler.addInstruction(new CMP(new NullOperand(), regObj));
        compiler.addInstruction(new BEQ(endCast));

        // On prépare la comparaison
        GPRegister currentVTable = compiler.utiliserRegistre();
        GPRegister targetVTable = compiler.utiliserRegistre();
        
        // Charger la VTable de la classe cible
        compiler.addInstruction(new LEA(targetType.getDefinition().getVTableAddr(), targetVTable));
        // Charger la VTable de l'objet
        compiler.addInstruction(new LOAD(new RegisterOffset(0, regObj), currentVTable));

        compiler.addLabel(startLoop);
        compiler.addInstruction(new CMP(targetVTable, currentVTable));
        compiler.addInstruction(new BEQ(endCast)); // Trouvé !

        // Remonter au père (offset 0 de la VTable)
        compiler.addInstruction(new LOAD(new RegisterOffset(0, currentVTable), currentVTable));
        compiler.addInstruction(new CMP(new NullOperand(), currentVTable));
        compiler.addInstruction(new BNE(startLoop));

        // Si on arrive ici, le cast est invalide
        compiler.addInstruction(new BRA(new Label("conversion_impossible_error")));
        
        compiler.addLabel(endCast);
        
        compiler.libererRegistre(); // targetVTable
        compiler.libererRegistre(); // currentVTable
    }
    @Override
    public int simuleExecutionNbRegistres(DecacCompiler compiler){
        return expr.simuleExecutionNbRegistres(compiler);
    }

    private void castCompatible(Type target, Type exprType) throws ContextualError {
        // Void case
        if (target.isVoid()){
             throw new ContextualError("Type mismatch: cannot cast " + exprType + " to " + target, getLocation());
        }

        // Same type OK!
        if (exprType.sameType(target) && !(target.isClass() || exprType.isClassOrNull())) return;

        // int -> float and float -> int OK!
        if (target.isFloat() && exprType.isInt()) return;
        if (target.isInt() && exprType.isFloat()) return;

        // Class case
        if(target.isClass() && exprType.isClassOrNull()){
            if(exprType.isNull()) return;

            ClassType targetClass = target.asClassType("Conversion from type " + target + " to ClassType failed", getLocation());
            ClassType exprClass = exprType.asClassType("Conversion from type " + exprType + " to ClassType failed", getLocation());

            if(exprClass.isSubClassOf(targetClass) || targetClass.isSubClassOf(exprClass)) return;
        }

        throw new ContextualError("Type mismatch: cannot cast " + exprType + " to " + target, getLocation());
    }
    
}
