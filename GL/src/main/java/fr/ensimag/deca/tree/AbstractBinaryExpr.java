package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.instructions.FLOAT;
import fr.ensimag.ima.pseudocode.instructions.LOAD;

import java.io.PrintStream;
import org.apache.commons.lang.Validate;

/**
 * Binary expressions.
 *
 * @author gl54
 * @date 01/01/2026
 */
public abstract class AbstractBinaryExpr extends AbstractExpr {

    @Override
    public int simuleExecutionNbRegistres(DecacCompiler compiler){
        // On sauvegarde l'état du simulateur avant d'évaluer la branche droite.
        int compteurAvant = compiler.getCompteurRegistre();
        int stokesAvant = compiler.getNbRegStokes();

        int maxRegDroit = getRightOperand().simuleExecutionNbRegistres(compiler);

        // Maintenant, on prépare la simulation pour la branche gauche.
        if (compiler.getNbRegStokes() > stokesAvant) {
            // Un "spill" (sauvegarde en pile) a eu lieu dans la branche droite.
            // Le résultat est donc sur la pile. On peut restaurer tous les registres
            // pour la branche gauche, en notant qu'un élément de plus est sur la pile.
            compiler.setCompteurRegistre(compteurAvant);
            compiler.setNbRegStokes(stokesAvant + 1);
        } else {
            // Pas de "spill". Le résultat de la branche droite occupe un registre.
            // On libère tous les autres registres temporaires utilisés pour son calcul
            // en remettant le compteur à "compteur d'avant + 1".
            compiler.setCompteurRegistre(compteurAvant + 1);
            compiler.setNbRegStokes(stokesAvant);
        }

        int maxRegGauche = getLeftOperand().simuleExecutionNbRegistres(compiler);

        // Le pic d'utilisation est le maximum de ce qu'on a vu à gauche et à droite.
        return Math.max(maxRegGauche, maxRegDroit);
    }

    protected GPRegister getOperande1(DecacCompiler compiler, AbstractExpr exprOperande){
         // on evalue les deux operandes
        AbstractExpr leftOperand = getLeftOperand();
        DVal tmpOp1 = leftOperand.codeGenInst(compiler);
        GPRegister operande1;

        if(tmpOp1 instanceof GPRegister == false){
            operande1 = compiler.utiliserRegistre();
            compiler.addInstruction(new LOAD(tmpOp1, operande1));
        }else{
            operande1 = (GPRegister)tmpOp1;
        }

        // // selon si l'operande gauche est une constante ou pas :     
        // if(leftOperand instanceof IntLiteral || leftOperand instanceof FloatLiteral){
        //     System.out.println("load operande1 non registre");
            
        // }else{
        //     System.out.println("load operande1 registre");
            
        // }
        return operande1;
    }


    protected DVal getOperande2(DecacCompiler compiler,  GPRegister operande1, AbstractExpr exprOperande2){
        // on evalue l'operande droite
        AbstractExpr rightOperand = getRightOperand();
        DVal tmpOp2 = rightOperand.codeGenInst(compiler);
        DVal operande2 = tmpOp2;

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
        return operande2;
    }


    protected boolean libereOperande2 = false;


    public AbstractExpr getLeftOperand() {
        return leftOperand;
    }

    public AbstractExpr getRightOperand() {
        return rightOperand;
    }

    protected void setLeftOperand(AbstractExpr leftOperand) {
        Validate.notNull(leftOperand);
        this.leftOperand = leftOperand;
    }

    protected void setRightOperand(AbstractExpr rightOperand) {
        Validate.notNull(rightOperand);
        this.rightOperand = rightOperand;
    }

    private AbstractExpr leftOperand;
    private AbstractExpr rightOperand;

    public AbstractBinaryExpr(AbstractExpr leftOperand,
            AbstractExpr rightOperand) {
        Validate.notNull(leftOperand, "left operand cannot be null");
        Validate.notNull(rightOperand, "right operand cannot be null");
        Validate.isTrue(leftOperand != rightOperand, "Sharing subtrees is forbidden");
        this.leftOperand = leftOperand;
        this.rightOperand = rightOperand;
    }


    @Override
    public void decompile(IndentPrintStream s) {
        s.print("(");
        getLeftOperand().decompile(s);
        s.print(" " + getOperatorName() + " ");
        getRightOperand().decompile(s);
        s.print(")");
    }

    abstract protected String getOperatorName();

    @Override
    protected void iterChildren(TreeFunction f) {
        leftOperand.iter(f);
        rightOperand.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        leftOperand.prettyPrint(s, prefix, false);
        rightOperand.prettyPrint(s, prefix, true);
    }

    protected abstract Type typeOfOperation(DecacCompiler compiler, Type t1, Type t2) throws ContextualError;

    public Type arithmeticOp(DecacCompiler compiler, String op, Type t1, Type t2) throws ContextualError{
        if ((t1.isInt() && t2.isInt())) {
            this.setType(compiler.environmentType.INT); //décoration du noeud avec le type résultat
            return compiler.environmentType.INT;
        } 
        
        else if ((t1.isFloat() && t2.isFloat()) || (t1.isInt() && t2.isFloat())
                || (t1.isFloat() && t2.isInt())) {
            this.setType(compiler.environmentType.FLOAT);//décoration du noeud avec le type résultat
            return compiler.environmentType.FLOAT;
        } 
        
        else {
            throw new ContextualError("Incompatible types for arithmetic operation: " 
                    + t1 + " and " + t2, this.getLocation());
        }
    }

    public boolean isArtihmeticOp() {
        return false;
    }

    public boolean isComparisonOp() {
        return false;
    }

    public boolean isExactComparisonOp(){
        return false;
    }

    public boolean isBooleanOp(){
        return false;
    }

    

}
