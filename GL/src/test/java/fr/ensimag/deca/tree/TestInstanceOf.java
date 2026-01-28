package fr.ensimag.deca.tree;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.*;
import fr.ensimag.deca.tools.SymbolTable;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Instruction;


public class TestInstanceOf {


    private static void injectEnvironmentType(DecacCompiler compiler, EnvironmentType envTypes) {
        try {
            Field f = DecacCompiler.class.getDeclaredField("environmentType");
            f.setAccessible(true);
            f.set(compiler, envTypes);
        } catch (Exception e) {
            fail("Impossible d'injecter environmentType : " + e.getMessage());
        }
    }


     //verifyExpr — cas ERREUR

    @Test
    void verifyExpr_throws_whenTypesInvalid() throws ContextualError {
        DecacCompiler compiler = mock(DecacCompiler.class);
        EnvironmentExp localEnv = mock(EnvironmentExp.class);
        ClassDefinition currentClass = mock(ClassDefinition.class);

        AbstractExpr expr = mock(AbstractExpr.class);
        AbstractIdentifier typeId = mock(AbstractIdentifier.class);

        Type type1 = mock(Type.class);
        Type type2 = mock(Type.class);

        when(type1.isClass()).thenReturn(false);
        when(type1.isNull()).thenReturn(false);
        when(type2.isClass()).thenReturn(false);

        doReturn(type1).when(expr).verifyExpr(compiler, localEnv, currentClass);
        when(typeId.verifyType(compiler)).thenReturn(type2);

        InstanceOf inst = new InstanceOf(expr, typeId);

        ContextualError ex = assertThrows(ContextualError.class,
                () -> inst.verifyExpr(compiler, localEnv, currentClass));

        assertEquals(
                "instanceof requires a class type on the right and a class or null on the left",
                ex.getMessage()
        );
    }


     //verifyExpr — cas OK

    @Test
    void verifyExpr_ok_whenType1AndType2AreClass() throws ContextualError {
        DecacCompiler compiler = mock(DecacCompiler.class);
        EnvironmentExp localEnv = mock(EnvironmentExp.class);
        ClassDefinition currentClass = mock(ClassDefinition.class);

        AbstractExpr expr = mock(AbstractExpr.class);
        AbstractIdentifier typeId = mock(AbstractIdentifier.class);

        Type type1 = mock(Type.class);
        Type type2 = mock(Type.class);

        when(type1.isClass()).thenReturn(true);
        when(type1.isNull()).thenReturn(false);
        when(type2.isClass()).thenReturn(true);

        doReturn(type1).when(expr).verifyExpr(compiler, localEnv, currentClass);
        when(typeId.verifyType(compiler)).thenReturn(type2);

        // Créer un vrai EnvironmentType pour BOOLEAN
        SymbolTable symTable = new SymbolTable();
        doAnswer(inv -> symTable.create(inv.getArgument(0)))
                .when(compiler).createSymbol(anyString());

        EnvironmentType envTypes = new EnvironmentType(compiler);
        injectEnvironmentType(compiler, envTypes);

        InstanceOf inst = new InstanceOf(expr, typeId);
        Type result = inst.verifyExpr(compiler, localEnv, currentClass);

        assertSame(envTypes.BOOLEAN, result);
        assertSame(envTypes.BOOLEAN, inst.getType());
    }

     //decompile

    @Test
    void decompile_printsInstanceOfSyntax() {
        AbstractExpr expr = mock(AbstractExpr.class);
        AbstractIdentifier typeId = mock(AbstractIdentifier.class);

        InstanceOf inst = new InstanceOf(expr, typeId);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(out);
        IndentPrintStream ips = new IndentPrintStream(ps);

        inst.decompile(ips);

        verify(expr).decompile(ips);
        verify(typeId).decompile(ips);
    }


     //prettyPrintChildren

    @Test
    void prettyPrintChildren_callsChildren() {
        AbstractExpr expr = mock(AbstractExpr.class);
        AbstractIdentifier typeId = mock(AbstractIdentifier.class);

        InstanceOf inst = new InstanceOf(expr, typeId);
        inst.prettyPrintChildren(System.out, "P");

        verify(expr).prettyPrint(any(PrintStream.class), eq("P"), eq(true));
        verify(typeId).prettyPrint(any(PrintStream.class), eq("P"), eq(false));
    }


     //iterChildren

    @Test
    void iterChildren_callsChildren() {
        AbstractExpr expr = mock(AbstractExpr.class);
        AbstractIdentifier typeId = mock(AbstractIdentifier.class);
        TreeFunction f = mock(TreeFunction.class);

        InstanceOf inst = new InstanceOf(expr, typeId);
        inst.iterChildren(f);

        verify(expr).iterChildren(f);
        verify(typeId).iterChildren(f);
    }


     //simuleExecutionNbRegistres

    @Test
    void simuleExecutionNbRegistres_delegatesToExpr() {
        AbstractExpr expr = mock(AbstractExpr.class);
        AbstractIdentifier typeId = mock(AbstractIdentifier.class);
        DecacCompiler compiler = mock(DecacCompiler.class);

        when(expr.simuleExecutionNbRegistres(compiler)).thenReturn(2);

        InstanceOf inst = new InstanceOf(expr, typeId);
        int res = inst.simuleExecutionNbRegistres(compiler);

        assertEquals(2, res);
    }


    //codeGenInst

    @Test
    void codeGenInst_emitsInstructions() {
        DecacCompiler compiler = mock(DecacCompiler.class);

        AbstractExpr expr = mock(AbstractExpr.class);
        AbstractIdentifier typeId = mock(AbstractIdentifier.class);
        GPRegister regExpr = mock(GPRegister.class);
        doReturn(regExpr).when(expr).codeGenInst(compiler);
        GPRegister regTmp = mock(GPRegister.class);
        when(compiler.utiliserRegistre()).thenReturn(regTmp);

        ClassDefinition classDef = mock(ClassDefinition.class);
        when(typeId.getClassDefinition()).thenReturn(classDef);
        when(classDef.getVTableAddr()).thenReturn(mock(fr.ensimag.ima.pseudocode.DAddr.class));

        fr.ensimag.deca.tools.SymbolTable symTable = new fr.ensimag.deca.tools.SymbolTable();
        fr.ensimag.deca.tools.SymbolTable.Symbol sym = symTable.create("Cible");
        when(typeId.getName()).thenReturn(sym);

        ArgumentCaptor<Instruction> cap = ArgumentCaptor.forClass(Instruction.class);
        doNothing().when(compiler).addInstruction(cap.capture());

        InstanceOf inst = new InstanceOf(expr, typeId);

        inst.setLocation(new Location(1, 1, "test"));

        GPRegister res = inst.codeGenInst(compiler);

        assertNotNull(res);
        assertFalse(cap.getAllValues().isEmpty());
    }
}
