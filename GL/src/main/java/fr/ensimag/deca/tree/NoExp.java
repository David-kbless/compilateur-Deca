package fr.ensimag.deca.tree;

import java.io.PrintStream;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.ima.pseudocode.GPRegister;

/**
 * Représente une absence d'expression (champ non initialisé, expression vide, etc.).
 * Utile pour que les méthodes iter(), decompile(), checkAllLocations() ne plantent pas.
 */
public class NoExp extends AbstractExpr {
    public NoExp() {
        // constructeur vide
    }

    @Override
    public void decompile(fr.ensimag.deca.tools.IndentPrintStream s) {
        // No expression to decompile
    }
    
    @Override
    protected void iterChildren(TreeFunction f) {
        // pas d'enfants => rien à faire
    }

     @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        // pas d'enfants => rien à faire
    }

    @Override
    protected void checkLocation() {
        // pas de localisation à vérifier
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        // nothing to do        
        throw new UnsupportedOperationException("NoExp.verifyExpr should not be called");
    }

    protected GPRegister codeGenOperationBinaire(DecacCompiler compiler, GPRegister operande1, GPRegister operande2){
        // rien a faire car il ya pas d'expression 
        return null;  
    }

    @Override
    protected GPRegister codeGenInst(DecacCompiler compiler) {
        return null;
    }

    @Override
    public int simuleExecutionNbRegistres(DecacCompiler compiler){
        return 0;
    }
}
