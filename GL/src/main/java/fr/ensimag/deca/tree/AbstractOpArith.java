package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.BOV;
import fr.ensimag.ima.pseudocode.instructions.FLOAT;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.POP;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import org.apache.commons.lang.Validate;

/**
 * Arithmetic binary operations (+, -, /, ...)
 * 
 * @author gl54
 * @date 01/01/2026
 */
public abstract class AbstractOpArith extends AbstractBinaryExpr {

    public AbstractOpArith(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        
        //Récupération des types des opérandes
        Type type1 = this.getLeftOperand().verifyExpr(compiler, localEnv, currentClass);
        Type type2 = this.getRightOperand().verifyExpr(compiler, localEnv, currentClass);
        Validate.notNull(type1, "Left operand type cannot be null");
        Validate.notNull(type2, "Right operand type cannot be null");

        //Vérification de la compatibilité des types des opérandes
        Type resultType = this.typeOfOperation(compiler, type1, type2);
        Validate.notNull(resultType, "Result type cannot be null");

        if (resultType.isFloat()) {
            if (type1.isInt()) {
                setLeftOperand(new ConvFloat(getLeftOperand()));
                getLeftOperand().verifyExpr(compiler, localEnv, currentClass);
            }
            if (type2.isInt()) {
                setRightOperand(new ConvFloat(getRightOperand()));
                getRightOperand().verifyExpr(compiler, localEnv, currentClass);
            }
        }

        //décoration du noeud avec le type résultat
        this.setType(resultType);
        return resultType;
    }

    protected Type typeOfOperation(DecacCompiler compiler, Type t1, Type t2) throws ContextualError {
    // Toutes les opérations sauf modulo
        if ((t1.isInt() && t2.isInt())) return compiler.environmentType.INT;
        if ((t1.isInt() && t2.isFloat()) || (t1.isFloat() && t2.isInt()) || (t1.isFloat() && t2.isFloat()))
            return compiler.environmentType.FLOAT;

        throw new ContextualError("Incompatible types for arithmetic operation: " + t1 + " and " + t2, getLocation());
}

    @Override
    public boolean isArtihmeticOp() {
        return true;
    }

    @Override
    protected DVal codeGenInst(DecacCompiler compiler){

        // @Ousmane a modifier
        // on evalue les deux operandes
        AbstractExpr leftOperand = getLeftOperand();
        DVal tmpOp1 = leftOperand.codeGenInst(compiler);
        GPRegister operande1;

        // selon si l'operande gauche est une constante ou pas :     
        if(leftOperand instanceof IntLiteral || leftOperand instanceof FloatLiteral){
            operande1 = compiler.utiliserRegistre();
            compiler.addInstruction(new LOAD(tmpOp1, operande1));
        }else{
            operande1 = (GPRegister)tmpOp1;
        }
 
        // on evalue l'operande droite
        AbstractExpr rightOperand = getRightOperand();
        DVal tmpOp2 = rightOperand.codeGenInst(compiler);
        DVal operande2 = tmpOp2;
        boolean libereOperande2 = false;

        // gestion de la convertion lorsqu'une op est int et l'autre float
        if(rightOperand.getType().isInt() && leftOperand.getType().isFloat()){
            if(rightOperand instanceof IntLiteral || rightOperand instanceof FloatLiteral){
                operande2 = compiler.utiliserRegistre();
                compiler.addInstruction(new LOAD(tmpOp2, (GPRegister)operande2));
                libereOperande2 = true;
            }
            compiler.addInstruction(new FLOAT(operande2, (GPRegister)operande2));
        }else if(leftOperand.getType().isInt() && rightOperand.getType().isFloat()){
            compiler.addInstruction(new FLOAT(operande1, operande1));
        }


        

        // si on a fait un push dans la pile de operande1
        if(operande1 == operande2){
            compiler.addInstruction(new POP((Register.R0)));
            codeGenOperationBinaire(compiler, Register.R0, operande2);
            // on transvase le resultat dans operande2
            compiler.addInstruction(new LOAD(Register.R0, (GPRegister)operande2));
            operande1 = (GPRegister) operande2;
        }else{
            // sinon on effectue l'operation binaire normalement
            codeGenOperationBinaire(compiler, operande1, operande2);
        }
        
        
        // liberation des registres utilises
        if((libereOperande2 || operande2 instanceof GPRegister) && operande2 != operande1){
            compiler.libererRegistre();
        }


        // vérification overflow pour les float
        if (!compiler.getCompilerOptions().getNoCheck() && getType().isFloat()) {
            compiler.addInstruction(new BOV(new Label("overflow_error")));
            compiler.addOverflowLabel();
        }
        
        

        return operande1;
    }

    

    @Override
    public int simuleExecutionNbRegistres(DecacCompiler compiler){
        // On sauvegarde l'état du simulateur avant d'évaluer la branche gauche.
        int compteurAvant = compiler.getCompteurRegistre();
        int stokesAvant = compiler.getNbRegStokes();

        int maxRegGauche = getLeftOperand().simuleExecutionNbRegistres(compiler);

        // Maintenant, on prépare la simulation pour la branche droite.
        if (compiler.getNbRegStokes() > stokesAvant) {
            // Un "spill" (sauvegarde en pile) a eu lieu dans la branche gauche.
            // Le résultat est donc sur la pile. On peut restaurer tous les registres
            // pour la branche droite, en notant qu'un élément de plus est sur la pile.
            compiler.setCompteurRegistre(compteurAvant);
            compiler.setNbRegStokes(stokesAvant + 1);
        } else {
            // Pas de "spill". Le résultat de la branche gauche occupe un registre.
            // On libère tous les autres registres temporaires utilisés pour son calcul
            // en remettant le compteur à "compteur d'avant + 1".
            compiler.setCompteurRegistre(compteurAvant + 1);
            compiler.setNbRegStokes(stokesAvant);
        }

        int maxRegDroit = getRightOperand().simuleExecutionNbRegistres(compiler);

        // Le pic d'utilisation est le maximum de ce qu'on a vu à gauche et à droite.
        return Math.max(maxRegGauche, maxRegDroit);
    }

    protected abstract GPRegister codeGenOperationBinaire(DecacCompiler compiler, GPRegister operande1, DVal operande2);

}
