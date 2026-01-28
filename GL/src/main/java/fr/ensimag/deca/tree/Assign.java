package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ClassType;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.FieldDefinition;
import fr.ensimag.ima.pseudocode.DAddr;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.STORE;

/**
 * Assignment, i.e. lvalue = expr.
 *
 * @author gl54
 * @date 01/01/2026
 */
public class Assign extends AbstractBinaryExpr {

    @Override
    public AbstractLValue getLeftOperand() {
        // The cast succeeds by construction, as the leftOperand has been set
        // as an AbstractLValue by the constructor.
        return (AbstractLValue)super.getLeftOperand();
    }

    public Assign(AbstractLValue leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {

        Type leftType = getLeftOperand().verifyExpr(compiler, localEnv, currentClass);

        setRightOperand(getRightOperand().verifyRValue(compiler, localEnv, currentClass, leftType));
        Type rightType = getRightOperand().getType();
        assignCompatible(leftType, rightType, this.getLocation());
        this.setType(leftType);
        return leftType;
    }


    public static void assignCompatible(Type target, Type exprType, Location loc) throws ContextualError {
        // Same type OK!
        if (exprType.sameType(target) && !(target.isClass() || exprType.isClassOrNull())) return;

        // int -> float OK!
        if (target.isFloat() && exprType.isInt()) return;

        // Class case
        if(target.isClass() && exprType.isClassOrNull()){
            if(exprType.isNull()) return;

            ClassType targetClass = target.asClassType("Conversion from type " + target + " to ClassType failed", loc);
            ClassType exprClass = exprType.asClassType("Conversion from type " + exprType + " to ClassType failed", loc);

            if(exprClass.isSubClassOf(targetClass)) return;
        }

        throw new ContextualError("Type mismatch: cannot assign/cast " + exprType + " to " + target, loc);
    }

    @Override
    protected Type typeOfOperation(DecacCompiler compiler, Type t1, Type t2) throws ContextualError{
        return t1;
    }
    
    @Override
    protected String getOperatorName() {
        return "=";
    }

    @Override
    protected DVal codeGenInst(DecacCompiler compiler){

        // on calcul l'expression a stocker
        DVal expressionAStoker = getRightOperand().codeGenInst(compiler);

        DAddr addr = null;
        AbstractLValue leftOperand = getLeftOperand();

        if(leftOperand instanceof Selection){
            addr = leftOperand.getDAddr(compiler);
        }else{
            AbstractIdentifier identifier = (AbstractIdentifier)getLeftOperand();

            if (identifier.getExpDefinition().isField()) {
                FieldDefinition fieldDef = (FieldDefinition) identifier.getExpDefinition();
                int offset = fieldDef.getIndex() + 1;

                // On charge "this" dans un registre
                GPRegister regThis = compiler.utiliserRegistre();
                compiler.addInstruction(new LOAD(new RegisterOffset(-2, Register.LB), regThis));
                
                // On stocke la valeur de droite dans le champ
                addr = new RegisterOffset(offset, regThis);
                // on libère le registre utilisé pour this
                compiler.libererRegistre();
            } else {
                // Cas classique : variable locale ou paramètre
            
                addr = identifier.getExpDefinition().getOperand();
                
            }
        }
        


        // si l'expression a stoker n'est pas dans un registre on la charge d'abord dans un registre
        expressionAStoker = verifRegister(compiler, expressionAStoker);

        // on l'a stocke finalement
        compiler.addInstruction(new STORE((GPRegister)expressionAStoker, addr));

        return expressionAStoker;
    }


    

}
