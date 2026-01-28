package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.GPRegister;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TestExprInst{

    @Mock private AbstractExpr expr;

    @Mock private DecacCompiler compiler;
    @Mock private EnvironmentExp localEnv;
    @Mock private ClassDefinition currentClass;
    @Mock private Type returnType;

    @InjectMocks
    private ExprInst inst;
    @Test
    void verifyInst_delegue_a_verifyExpr() throws ContextualError {
        // GIVEN

        // WHEN
        inst.verifyInst(compiler, localEnv, currentClass, returnType);

        // THEN
        verify(expr).verifyExpr(compiler, localEnv, currentClass);
        verifyNoMoreInteractions(expr);
    }

    @Test
    void verifyInst_propage_lexception_ContextualError() throws ContextualError {
        // GIVEN
        doThrow(new ContextualError("propage", null))
                .when(expr).verifyExpr(compiler, localEnv, currentClass);

        // WHEN + THEN
        assertThrows(ContextualError.class,
                () -> inst.verifyInst(compiler, localEnv, currentClass, returnType));
    }

    @Test
    void codeGenInst_retourne_le_registre_de_expr() {
        // GIVEN
        GPRegister reg = mock(GPRegister.class);
        when(expr.codeGenInst(compiler)).thenReturn(reg);

        // WHEN
        DVal result = inst.codeGenInst(compiler);

        // THEN
        assertSame(reg, (GPRegister)result);
        verify(expr).codeGenInst(compiler);
    }

    @Test
    void simuleExecutionNbRegistres_delegue_a_expr() {
        when(expr.simuleExecutionNbRegistres(compiler)).thenReturn(3);

        // WHEN
        int nb = inst.simuleExecutionNbRegistres(compiler);

        // THEN
        assertEquals(3, nb);
        verify(expr).simuleExecutionNbRegistres(compiler);
    }

    @Test
    void decompile_ecrit_expr_puis_point_virgule() {

        // On capture la sortie
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(out);
        IndentPrintStream ips = new IndentPrintStream(ps);

        // WHEN
        inst.decompile(ips);

        // THEN
        verify(expr).decompile(ips);
        assertTrue(out.toString().trim().endsWith(";"));
    }

    @Test
    void prettyPrintChildren_delegue_a_expr() {
        // GIVEN
        PrintStream ps = mock(PrintStream.class);

        // WHEN
        inst.prettyPrintChildren(ps, "P");

        // THEN
        verify(expr).prettyPrint(ps, "P", true);
    }

    @Test
    void iterChildren_delegue_a_expr_iter() {
        // GIVEN
        TreeFunction f = mock(TreeFunction.class);

        // WHEN
        inst.iterChildren(f);

        // THEN
        verify(expr).iter(f); // car ton code fait expr.iter(f)
    }
}
