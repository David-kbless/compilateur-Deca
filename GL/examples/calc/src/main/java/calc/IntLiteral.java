package calc;

public class IntLiteral extends AbstractExpr {
    private final long value;

    public IntLiteral(long value) {
        this.value = value;
    }

    @Override
    public int value() {
        return (int) value;
    }

    @Override
    public String toString() {
        return Integer.toString((int) value);
    }

    // on evite de loader des valeur supp à 2^31 - 1
    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass) 
            throws ContextualError {
        System.out.println(Integer.MAX_VALUE);
        System.out.println(Integer.MIN_VALUE);
        // Vérification des bornes 32 bits signes
        if (value > Integer.MAX_VALUE) {
            throw new ContextualError("Literal entier trop grand : " + value + 
                " (max: " + Integer.MAX_VALUE + ")", getLocation());
        }
        //  On gere le cas négatif ici aussi pour plus de sécurité
        if (value < Integer.MIN_VALUE) {
             throw new ContextualError("Literal entier trop petit : " + value + 
                " (min: " + Integer.MIN_VALUE + ")", getLocation());
        }
        
        Type type = compiler.environmentType.INT;
        this.setType(type);
        return type;
    }

}
