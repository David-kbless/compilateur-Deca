package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;

/**
 *
 * @author gl54
 * @date 01/01/2026
 */
public abstract class AbstractOpExactCmp extends AbstractOpCmp {

    public AbstractOpExactCmp(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    protected Type typeOfOperation(DecacCompiler compiler, Type t1, Type t2) throws ContextualError {
        boolean numeric = (t1.isInt() || t1.isFloat()) && (t2.isInt() || t2.isFloat());
        boolean booleans = t1.isBoolean() && t2.isBoolean();
        boolean classes = (t1.isClass() || t1.isNull()) && (t2.isClass() || t2.isNull());

        if (numeric || booleans || classes) return compiler.environmentType.BOOLEAN;

        throw new ContextualError("Invalid types for equality comparison: " + t1 + " and " + t2, getLocation());
    }

    @Override
    public boolean isExactComparisonOp(){
        return true;
    }

}
