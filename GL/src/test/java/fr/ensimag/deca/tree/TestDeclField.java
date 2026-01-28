package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.*;

import fr.ensimag.deca.context.EnvironmentExp.DoubleDefException;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tools.SymbolTable;
import fr.ensimag.deca.tools.SymbolTable.Symbol;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeclFieldTest {

    @Mock private DecacCompiler compiler;

    @Mock private ClassDefinition superClass;
    @Mock private ClassDefinition currentClass;

    @Mock private EnvironmentExp envExpR;
    @Mock private EnvironmentExp membersCurrent;
    @Mock private EnvironmentExp membersSuper;

    @Mock private AbstractIdentifier fieldName;
    @Mock private AbstractInitialization initialization;

    @Mock private AbstractIdentifier typeId;
    @Mock private Type realType;

    private DeclField newDeclField() {
        DeclField d = new DeclField(fieldName, initialization);
        d.setType(typeId);
        d.setVisibility(Visibility.PUBLIC); // visibilité par défaut pour les tests
        return d;
    }

    private void stubClassEnvs() {
        when(currentClass.getMembers()).thenReturn(membersCurrent);
        when(superClass.getMembers()).thenReturn(membersSuper);
    }

    private Symbol newSymbol(String name) {
        SymbolTable st = new SymbolTable();
        return st.create(name);
    }

    @Test
    void verifyDeclField_throws_whenTypeIsVoid() throws ContextualError {
        // GIVEN
        DeclField declField = newDeclField();
        when(typeId.verifyType(compiler)).thenReturn(realType);
        when(realType.isVoid()).thenReturn(true);

        // WHEN + THEN
        ContextualError ex = assertThrows(ContextualError.class,
                () -> declField.verifyDeclField(compiler, superClass, currentClass, envExpR));
        assertEquals("A variable cannot have type void", ex.getMessage());
    }

    @Test
    void verifyDeclField_throws_whenNameAlreadyUsedInCurrentClass() throws ContextualError {
        // GIVEN
        DeclField declField = newDeclField();

        when(typeId.verifyType(compiler)).thenReturn(realType);
        when(realType.isVoid()).thenReturn(false);

        stubClassEnvs();

        Symbol sym = newSymbol("x");
        when(fieldName.getName()).thenReturn(sym);

        ExpDefinition alreadyThere = mock(ExpDefinition.class);
        when(membersCurrent.getLocal(sym)).thenReturn(alreadyThere);

        // WHEN + THEN
        assertThrows(ContextualError.class,
                () -> declField.verifyDeclField(compiler, superClass, currentClass, envExpR));
    }

    @Test
    void verifyDeclField_throws_whenNameUsedInSuperButNotAField() throws ContextualError {
        // GIVEN
        DeclField declField = newDeclField();

        when(typeId.verifyType(compiler)).thenReturn(realType);
        when(realType.isVoid()).thenReturn(false);

        stubClassEnvs();

        Symbol sym = newSymbol("x");
        when(fieldName.getName()).thenReturn(sym);

        when(membersCurrent.getLocal(sym)).thenReturn(null);

        ExpDefinition superDef = mock(ExpDefinition.class);
        when(superDef.isField()).thenReturn(false);
        when(membersSuper.getLocal(sym)).thenReturn(superDef);

        // WHEN + THEN
        assertThrows(ContextualError.class,
                () -> declField.verifyDeclField(compiler, superClass, currentClass, envExpR));
    }

    @Test
    void verifyDeclField_throws_whenDoubleDefExceptionOnDeclare() throws ContextualError, DoubleDefException {
        // GIVEN
        DeclField declField = newDeclField();

        when(typeId.verifyType(compiler)).thenReturn(realType);
        when(realType.isVoid()).thenReturn(false);

        stubClassEnvs();

        Symbol sym = newSymbol("x");
        when(fieldName.getName()).thenReturn(sym);

        when(membersCurrent.getLocal(sym)).thenReturn(null);
        when(membersSuper.getLocal(sym)).thenReturn(null);

        when(currentClass.getNumberOfFields()).thenReturn(0);

        // envExpR.declare(...) lève une DoubleDefException
        doThrow(mock(DoubleDefException.class))
                .when(envExpR).declare(eq(sym), any(FieldDefinition.class));

        // WHEN + THEN
        assertThrows(ContextualError.class,
                () -> declField.verifyDeclField(compiler, superClass, currentClass, envExpR));
    }

    @Test
    void verifyDeclField_ok_declaresField_setsDefinition_incrementsAndReturnsVisibility() throws ContextualError, DoubleDefException {
        // GIVEN
        DeclField declField = newDeclField();

        when(typeId.verifyType(compiler)).thenReturn(realType);
        when(realType.isVoid()).thenReturn(false);

        stubClassEnvs();

        Symbol sym = newSymbol("x");
        when(fieldName.getName()).thenReturn(sym);

        when(membersCurrent.getLocal(sym)).thenReturn(null);
        when(membersSuper.getLocal(sym)).thenReturn(null);

        when(currentClass.getNumberOfFields()).thenReturn(7);

        // WHEN
        Visibility vis = declField.verifyDeclField(compiler, superClass, currentClass, envExpR);

        // THEN
        assertEquals(Visibility.PUBLIC, vis);

        verify(envExpR).declare(eq(sym), any(FieldDefinition.class));
        verify(currentClass).incNumberOfFields();
        verify(fieldName).setDefinition(any(FieldDefinition.class));
    }

    @Test
    void verifyFieldBody_callsInitializationVerifyInitialization() throws ContextualError {
        // GIVEN
        DeclField declField = newDeclField();

        FieldDefinition fieldDef = mock(FieldDefinition.class);
        Type fieldType = mock(Type.class);

        when(fieldName.getFieldDefinition()).thenReturn(fieldDef);
        when(fieldDef.getType()).thenReturn(fieldType);

        EnvironmentExp envExp = mock(EnvironmentExp.class);

        // WHEN
        declField.verifyFieldBody(compiler, currentClass, envExp);

        // THEN
        verify(initialization).verifyInitialization(compiler, fieldType, envExp, currentClass);

    }

    @Test
    void decompile_calls_children_and_prints_semicolon() {
        // GIVEN
        AbstractIdentifier fieldName = mock(AbstractIdentifier.class);
        AbstractIdentifier typeId = mock(AbstractIdentifier.class);
        AbstractInitialization init = mock(AbstractInitialization.class);

        DeclField declField = new DeclField(fieldName, init);
        declField.setType(typeId);
        declField.setVisibility(Visibility.PUBLIC);

        // Capture de la sortie
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(out);
        IndentPrintStream ips = new IndentPrintStream(ps);

        // WHEN
        declField.decompile(ips);

        // THEN : appels aux enfants
        verify(typeId).decompile(ips);
        verify(fieldName).decompile(ips);
        verify(init).decompile(ips);

        // THEN : sortie contient bien ;
        assertTrue(out.toString().trim().endsWith(";"));
    }

}
