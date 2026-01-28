package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.instructions.BRA;

import java.io.PrintStream;
import org.apache.commons.lang.Validate;

/**
 * Full if/else if/else statement.
 *
 * @author gl54
 * @date 01/01/2026
 */
public class IfThenElse extends AbstractInst {

    
    private final AbstractExpr condition; 
    private final ListInst thenBranch;
    private ListInst elseBranch;

    public IfThenElse(AbstractExpr condition, ListInst thenBranch, ListInst elseBranch) {
        Validate.notNull(condition);
        Validate.notNull(thenBranch);
        Validate.notNull(elseBranch);
        this.condition = condition;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
    }

    // Ajout d'une méthode pour modifier la branche else
    public void setElseBranch(ListInst elseBranch) {
        Validate.notNull(elseBranch);
        this.elseBranch = elseBranch;
    }
    
    @Override
    protected void verifyInst(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass, Type returnType)
            throws ContextualError {
        // Vérification de la condition
        condition.verifyCondition(compiler, localEnv, currentClass);

        // Vérification de la branche then
        thenBranch.verifyListInst(compiler, localEnv, currentClass, returnType);
        
        // Vérification de la branche else
        elseBranch.verifyListInst(compiler, localEnv, currentClass, returnType);
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("if (");
        condition.decompile(s);
        s.println(") {");
        s.indent();
        thenBranch.decompile(s);
        s.unindent();
        s.println("} else {");
        s.indent();
        elseBranch.decompile(s);
        s.unindent();
        s.println("}");
    }

    @Override
    protected
    void iterChildren(TreeFunction f) {
        condition.iter(f);
        thenBranch.iter(f);
        elseBranch.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        condition.prettyPrint(s, prefix, false);
        thenBranch.prettyPrint(s, prefix, false);
        elseBranch.prettyPrint(s, prefix, true);
    }
@Override
protected GPRegister codeGenInst(DecacCompiler compiler) {
    // @Ousmane : On récupère un numéro unique pour les étiquettes de ce IF
    int num = compiler.getAndIncIf();
    Label eFin = new Label("E_Fin_if." + num);
    Label eSinon = new Label("E_Sinon." + num);

    // @Ousmane : ⟨Code(C1, faux, E_Sinon.n1)⟩
    // On utilise la stratégie de flot de contrôle du polycopié (Section 7.2)
    // Cela génère directement les branchements (BEQ, BLT, etc.) vers E_Sinon
    condition.codeGenFaux(compiler, eSinon);

    // @Ousmane : ⟨Code(I1)⟩ (Branche Then)
    thenBranch.codeGenListInst(compiler);
    
    // @Ousmane : BRA E_Fin.n
    compiler.addInstruction(new BRA(eFin));

    // @Ousmane : E_Sinon.n1 :
    compiler.addLabel(eSinon);
    
    // @Ousmane : ⟨Code(I)⟩ (Branche Else)
    elseBranch.codeGenListInst(compiler);

    // @Ousmane : E_Fin.n :
    compiler.addLabel(eFin);
    
    return null;
}

    @Override
    public int simuleExecutionNbRegistres(DecacCompiler compiler){
        int condRegs = condition.simuleExecutionNbRegistres(compiler);

        int compteurAvant = compiler.getCompteurRegistre();
        int stokesAvant = compiler.getNbRegStokes();

        // Simulate then branch
        int thenRegs = thenBranch.simuleExecutionNbRegistres(compiler);
        
        // Restore state for else branch
        compiler.setCompteurRegistre(compteurAvant);
        compiler.setNbRegStokes(stokesAvant);

        // Simulate else branch
        int elseRegs = elseBranch.simuleExecutionNbRegistres(compiler);

        return Math.max(condRegs, Math.max(thenRegs, elseRegs));
    }

    
}
