package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.DAddr;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.ADDSP;
import fr.ensimag.ima.pseudocode.instructions.BOV;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.STORE;
import fr.ensimag.ima.pseudocode.instructions.TSTO;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.NullOperand;

import java.io.PrintStream;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;

/**
 * @author gl54
 * @date 01/01/2026
 */
public class Main extends AbstractMain {
    private static final Logger LOG = Logger.getLogger(Main.class);
    
    private ListDeclVar declVariables;
    private ListInst insts;
    public Main(ListDeclVar declVariables,
            ListInst insts) {
        Validate.notNull(declVariables);
        Validate.notNull(insts);
        this.declVariables = declVariables;
        this.insts = insts;
    }

    @Override
    protected void verifyMain(DecacCompiler compiler) throws ContextualError {
        LOG.debug("verify Main: start");
        // A FAIRE: Appeler méthodes "verify*" de ListDeclVarSet et ListInst.
        // Vous avez le droit de changer le profil fourni pour ces méthodes
        // (mais ce n'est à priori pas nécessaire).
        
        EnvironmentExp localEnv = new EnvironmentExp(null);
        declVariables.verifyListDeclVariable(compiler, localEnv, null); 
        insts.verifyListInst(compiler, localEnv, null, compiler.environmentType.VOID); 
        LOG.debug("verify Main: end");
    }

    @Override
    protected void codeGenMain(DecacCompiler compiler) {

        // on renitialise le gestionnaire de registre avant de commencer le main
        compiler.resetGestionnaireRegistres(false);


        compiler.addComment("Variable Declarations : ");

        // on parcours toutes les declarations des variables pour leur affecterr une daddr:
        for(AbstractDeclVar declVar : declVariables.getList()){
            DAddr nouveauAddr = new RegisterOffset(compiler.getIndexGB(), Register.GB);
            // on incremente le compteur pour GB
            compiler.getAndAddIndexGB();

            // on recupere le nom de l'identificateur : 
            AbstractIdentifier identificateur = declVar.getVarName();
            
            // on enregisstre la daddr : 
            identificateur.getVariableDefinition().setOperand(nouveauAddr);

            // on initialise si possible la variable alors : 
            if(declVar.getInitialization() instanceof Initialization){
                //transtypage pour eviter les erreurs java 
                AbstractExpr expression = ((Initialization) declVar.getInitialization()).getExpression();

                // on evalue l'expression right value: 
                DVal r = expression.codeGenInst(compiler);

                // on affecter effectivement la valeur a l'identificateur à gauche de l'egalite
                r = verifRegister(compiler, r);
                compiler.addInstruction(new STORE((GPRegister)r, nouveauAddr));

                // on recycle le registre pour ne pas trop utiliser la pile...
                compiler.libererRegistre();
            }else{
                
                // on initialise par null si c'est un objet 
                Type type = ((DeclVar) declVar).getVarName().getVariableDefinition().getType();
                if(!(type.isInt() || type.isBoolean() || type.isFloat())){
                    // on charge dans un registre d'abord
                    compiler.addInstruction(new LOAD(new NullOperand(), Register.R0));
                    compiler.addInstruction(new STORE(Register.R0, nouveauAddr));
                }
            }
        }


        // definition du label StackOverflowErr
        Label StackOverflowErr = new Label("stack_overflow_error");

        

        compiler.addComment("Beginning of main instructions:");


        // on renitialise également le gestionnaire de registre : 
        compiler.resetGestionnaireRegistres(false);

        // on reset le gestionnaire de pile avant de commencer le main
        compiler.resetTaillePileMaxUtilisee();
        insts.codeGenListInst(compiler);

        int taillePileUtilisee = compiler.getTaillePileMaxUtilisee();
        // puis on decale le sp s'il ya des variables globale au main : 
        if(compiler.getIndexGB() > 1 || taillePileUtilisee > 0){



            // on force l'ajout des instruction TSTO BOV au debut du fichier .ass
            compiler.addFirstInstruction(new ADDSP(compiler.getIndexGB() - 1));
            compiler.addFirstInstruction(new BOV(StackOverflowErr));
            compiler.addFirstInstruction(new TSTO(compiler.getIndexGB() -1 + compiler.getAutrePush() + taillePileUtilisee));
            // ajout d'un commentaire pour ressembler absolument à l'exemple du poly
            compiler.addFirstComment("start main program");

            
        }
        
    }
    
    @Override
    public void decompile(IndentPrintStream s) {
        s.println("{");
        s.indent();
        declVariables.decompile(s);
        insts.decompile(s);
        s.unindent();
        s.println("}");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        declVariables.iter(f);
        insts.iter(f);
    }
 
    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        declVariables.prettyPrint(s, prefix, false);
        insts.prettyPrint(s, prefix, true);
    }
}
