package fr.ensimag.deca.tree;

import java.io.PrintStream;

import org.apache.commons.lang.Validate;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.DAddr;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.NullOperand;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.BEQ;
import fr.ensimag.ima.pseudocode.instructions.BNE;
import fr.ensimag.ima.pseudocode.instructions.BRA;
import fr.ensimag.ima.pseudocode.instructions.CMP;
import fr.ensimag.ima.pseudocode.instructions.LEA;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
public class InstanceOf extends AbstractExpr {
    private final AbstractExpr expr;
    private final AbstractIdentifier type;

    public InstanceOf(AbstractExpr expr, AbstractIdentifier type) {
        Validate.notNull(expr);
        Validate.notNull(type);
        this.expr = expr;
        this.type = type;
    }

    public AbstractExpr getExpr() {
        return expr;
    }
    public AbstractIdentifier getTypeIdentifier() {
        return type;
    }

    public void setLocation(Location location) {
        super.setLocation(location);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
        ClassDefinition currentClass) throws ContextualError {

        Type type1 = expr.verifyExpr(compiler, localEnv, currentClass);
        Type type2 = type.verifyType(compiler);

        if ((type1.isClass() || type1.isNull()) && type2.isClass()) {
            setType(compiler.environmentType.BOOLEAN);
            return compiler.environmentType.BOOLEAN;
        }

        throw new ContextualError("instanceof requires a class type on the right and a class or null on the left", getLocation());
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("(");
        expr.decompile(s);
        s.print(" instanceof ");
        type.decompile(s);
        s.print(")");
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        expr.prettyPrint(s, prefix, true);
        type.prettyPrint(s, prefix, false);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        expr.iterChildren(f);
        type.iterChildren(f);
    }

    static int instanceOfCounter = 0;
    
    @Override
    protected GPRegister codeGenInst(DecacCompiler compiler) {
        // 1. On réserve les registres
        GPRegister regObj = (GPRegister)expr.codeGenInst(compiler); // Registre contenant l'objet
        GPRegister regResult = compiler.utiliserRegistre(); // Registre pour le booléen (0 ou 1)

        String fieldName = getFieldName();
        if(fieldName.length() > 1019){
            fieldName = fieldName.substring(0, 1015);
            fieldName += String.valueOf(instanceOfCounter++);
        }

        // Labels pour la boucle de recherche
        Label startLoop = new Label("start_instanceof_" + fieldName);
        Label isInstance = new Label("is_instance_" + fieldName);
        Label notInstance = new Label("not_instance_" + fieldName);
        Label endInstance = new Label("end_instanceof_" + fieldName);

        // 2. Si l'objet est null, ce n'est l'instance de rien (sauf cas spécial, mais ici false)
        compiler.addInstruction(new CMP(new NullOperand(), regObj));
        compiler.addInstruction(new BEQ(notInstance));

        // 3. On récupère l'adresse de la VTable de la classe cible
        // L'adresse de la VTable de la classe cible est à son adresse en GB
        DAddr addrTargetVTable = type.getClassDefinition().getVTableAddr();
        GPRegister regTargetVTable = compiler.utiliserRegistre();
        compiler.addInstruction(new LEA(addrTargetVTable, regTargetVTable));

        // 4. On récupère l'adresse de la VTable de l'objet actuel
        // On réutilise regObj pour stocker la VTable courante (économie de registres)
        compiler.addInstruction(new LOAD(new RegisterOffset(0, regObj), regObj));

        // 5. Boucle de remontée de l'héritage
        compiler.addLabel(startLoop);
        
        // Comparaison : est-ce la bonne VTable ?
        compiler.addInstruction(new CMP(regTargetVTable, regObj));
        compiler.addInstruction(new BEQ(isInstance));

        // Sinon, on remonte au père : la VTable du père est à l'offset 0 de la VTable actuelle
        compiler.addInstruction(new LOAD(new RegisterOffset(0, regObj), regObj));
        
        // Si le père est null, on a fini de remonter et on n'a pas trouvé
        compiler.addInstruction(new CMP(new NullOperand(), regObj));
        compiler.addInstruction(new BNE(startLoop));

        // 6. Gestion des résultats
        compiler.addLabel(notInstance);
        compiler.addInstruction(new LOAD(0, regResult)); // False
        compiler.addInstruction(new BRA(endInstance));

        compiler.addLabel(isInstance);
        compiler.addInstruction(new LOAD(1, regResult)); // True

        compiler.addLabel(endInstance);

        // Nettoyage des registres temporaires
        compiler.libererRegistre(); // libère regTargetVTable
        compiler.libererRegistre(); // libère regObj (utilisé pour expr)

        return regResult;
    }

    // Petite méthode utilitaire pour des labels uniques
    private String getFieldName() {
        return type.getName().getName() + "_" + getLocation().getLine() + "_" + getLocation().getPositionInLine();
    }

    // @Ousmane, a refaire quand j'aurai fini le body des methodes

    @Override
    public int simuleExecutionNbRegistres(DecacCompiler compiler){
        return expr.simuleExecutionNbRegistres(compiler);
    }
}
