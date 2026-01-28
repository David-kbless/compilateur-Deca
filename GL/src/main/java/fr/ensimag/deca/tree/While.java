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
 *
 * @author gl54
 * @date 01/01/2026
 */
public class While extends AbstractInst {
    private AbstractExpr condition;
    private ListInst body;

    public AbstractExpr getCondition() {
        return condition;
    }

    public ListInst getBody() {
        return body;
    }

    public While(AbstractExpr condition, ListInst body) {
        Validate.notNull(condition);
        Validate.notNull(body);
        this.condition = condition;
        this.body = body;
    }

    // @Override
    // protected GPRegister codeGenInst(DecacCompiler compiler) {

    //     // on creer une etiquette cond unique dans le programme : 
    //     Label nouvelleEtiquette =  new Label("E_Cond." + compiler.getCompteurLabelWhile());

    //     // on saute versla condition 
    //     compiler.addInstruction(new BRA(nouvelleEtiquette));

    //     Label debut = new Label("E_Debut." + compiler.getCompteurLabelWhile());

    //     // on ecrit létiquette : 
    //     compiler.addLabel(debut);
    //     compiler.addLabelWhile(); // on incremente le combpeur

    //     // on ecrit le body du while : 
    //     body.codeGenListInst(compiler);


    //     // on ecrit enfin le label de condition
    //     compiler.addLabel(nouvelleEtiquette);

    //     GPRegister reg = condition.codeGenInst(compiler);

    //     compiler.addInstruction(new CMP(0, reg));
    //     compiler.addInstruction(new BNE(debut));

    //     return reg;
    // }

    // @Override
    // public int simuleExecutionNbRegistres(DecacCompiler compiler){
    //     int condSimu = condition.simuleExecutionNbRegistres(compiler);
    //     int bodySimu = body.simuleExecutionNbRegistres(compiler);

    //     return Math.max(condSimu, bodySimu);
    // }



    @Override
    protected GPRegister codeGenInst(DecacCompiler compiler) {
        // @Ousmane : On récupère un numéro unique pour les étiquettes du WHILE
        int num = compiler.incrementerCompteurLabelWhile(); 
        Label eCond = new Label("E_Cond." + num);
        Label eDebut = new Label("E_Debut." + num);

        // @Ousmane : ⟨Code(while (C) { I } )⟩ ≡ BRA E_Cond.n
        compiler.addInstruction(new BRA(eCond));

        // @Ousmane : E_Debut.n :
        compiler.addLabel(eDebut);

        // @Ousmane : ⟨Code(I)⟩ (Le corps de la boucle)
        body.codeGenListInst(compiler);

        // @Ousmane : E_Cond.n :
        compiler.addLabel(eCond);

        // @Ousmane : ⟨Code(C, vrai, E_Debut.n)⟩
        // Utilise le flot de contrôle : branche directement à E_Debut si vrai
        condition.codeGenVrai(compiler, eDebut);

        return null;
    }

    @Override
    public int simuleExecutionNbRegistres(DecacCompiler compiler) {
        // @Ousmane : Le nombre de registres est le max entre la condition et le corps
        return Math.max(condition.simuleExecutionNbRegistres(compiler), 
                        body.simuleExecutionNbRegistres(compiler));
    }

    @Override
    protected void verifyInst(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass, Type returnType)
            throws ContextualError {
        // Vérification de la condition
        condition.verifyCondition(compiler, localEnv, currentClass);

        // Vérification du corps
        body.verifyListInst(compiler, localEnv, currentClass, returnType);
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("while (");
        getCondition().decompile(s);
        s.println(") {");
        s.indent();
        getBody().decompile(s);
        s.unindent();
        s.print("}");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        condition.iter(f);
        body.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        condition.prettyPrint(s, prefix, false);
        body.prettyPrint(s, prefix, true);
    }

}
