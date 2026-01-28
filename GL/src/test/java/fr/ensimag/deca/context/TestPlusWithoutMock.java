package fr.ensimag.deca.context;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tree.AbstractExpr;
import fr.ensimag.deca.tree.Plus;
import fr.ensimag.deca.tree.TreeFunction;
import fr.ensimag.ima.pseudocode.GPRegister;

import java.io.PrintStream;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for the Plus node in a manual way. The same test would be much easier to
 * write using a mock-up framework like Mockito.
 *
 * @see TestPlusPlain to see how the Mockito library can help writing this kind
 * of tests.
 *
 * @author Ensimag
 * @date 01/01/2026
 */
public class TestPlusWithoutMock {
    static final Type INT = new IntType(null);
    static final Type FLOAT = new FloatType(null);

    /**
     * Stub usable as a replacement for a real class deriving from AbstractExpr.
     *
     * This would typically be much simpler using Mockito.
     */
    static class DummyIntExpression extends AbstractExpr {
        boolean hasBeenVerified = false;

        @Override
        public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
                ClassDefinition currentClass) throws ContextualError {
            hasBeenVerified = true;
            return INT;
        }

        @Override
        public void decompile(IndentPrintStream s) {
            throw new UnsupportedOperationException("Should not be called.");
        }

        @Override
        protected void prettyPrintChildren(PrintStream s, String prefix) {
            throw new UnsupportedOperationException("Should not be called.");
        }

        @Override
        protected void iterChildren(TreeFunction f) {
            throw new UnsupportedOperationException("Should not be called.");
        }

        /**
         * Check that the object has been properly used after the test.
         */
        public void checkProperUse() {
            assertTrue(hasBeenVerified, "verifyExpr has not been called");
        }

        @Override
        protected GPRegister codeGenInst(DecacCompiler compiler) {
            // TODO Auto-generated method stub
            // throw new UnsupportedOperationException("Unimplemented method 'codeGenInst'");
            return null;
        }

        @Override
        public int simuleExecutionNbRegistres(DecacCompiler compiler) {
            return compiler.simulerAllocationRegistre();
        }
    }

    @Test
    public void testType() throws ContextualError {
        DecacCompiler compiler = new DecacCompiler(null, null);
        DummyIntExpression left = new DummyIntExpression();
        DummyIntExpression right = new DummyIntExpression();
        Plus t = new Plus(left, right);
        // check the result
        assertTrue(t.verifyExpr(compiler, null, null).isInt());
        // check that the dummy expression have been called properly.
        left.checkProperUse();
        right.checkProperUse();
    }

    @Test
    public void testSimuleExecution() {
        DecacCompiler compiler = new DecacCompiler(null, null);
        compiler.commencerSimulationRegistres(true); // Reset simulation state

        DummyIntExpression left = new DummyIntExpression();
        DummyIntExpression right = new DummyIntExpression();
        Plus t = new Plus(left, right);

        // Let's trace the simulation for Plus(left, right)
        // 1. sim(right) is called. It allocates R2. compteur=3. returns 2. maxRegDroit=2.
        // 2. The logic in AbstractBinaryExpr resets the counter: setCompteurRegistre(2 + 1 = 3).
        // 3. sim(left) is called. It allocates R3. compteur=4. returns 3. maxRegGauche=3.
        // 4. The result is max(3, 2) = 3.
        assertEquals(3, t.simuleExecutionNbRegistres(compiler));

        compiler.resetGestionnaireRegistres(false);
    }
}