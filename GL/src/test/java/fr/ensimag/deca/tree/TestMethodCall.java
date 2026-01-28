// package fr.ensimag.deca.tree;

// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.Mockito.*;

// import java.io.ByteArrayOutputStream;
// import java.io.PrintStream;
// import java.util.Arrays;

// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.mockito.ArgumentCaptor;

// import fr.ensimag.deca.DecacCompiler;
// import fr.ensimag.deca.context.ClassDefinition;
// import fr.ensimag.deca.context.ClassType;
// import fr.ensimag.deca.context.ContextualError;
// import fr.ensimag.deca.context.EnvironmentExp;
// import fr.ensimag.deca.context.ExpDefinition;
// import fr.ensimag.deca.context.MethodDefinition;
// import fr.ensimag.deca.context.Signature;
// import fr.ensimag.deca.context.Type;
// import fr.ensimag.deca.tools.IndentPrintStream;
// import fr.ensimag.ima.pseudocode.GPRegister;
// import fr.ensimag.ima.pseudocode.Instruction;
// import fr.ensimag.ima.pseudocode.Register;
// import fr.ensimag.ima.pseudocode.instructions.ADDSP;
// import fr.ensimag.ima.pseudocode.instructions.BEQ;
// import fr.ensimag.ima.pseudocode.instructions.BSR;
// import fr.ensimag.ima.pseudocode.instructions.CMP;
// import fr.ensimag.ima.pseudocode.instructions.LOAD;
// import fr.ensimag.ima.pseudocode.instructions.STORE;
// import fr.ensimag.ima.pseudocode.instructions.SUBSP;

// public class TestMethodCall {

//     private DecacCompiler compiler;
//     private EnvironmentExp localEnv;
//     private ClassDefinition currentClass;

//     @BeforeEach
//     void setup() {
//         compiler = mock(DecacCompiler.class);
//         localEnv = mock(EnvironmentExp.class);
//         currentClass = mock(ClassDefinition.class);

//         // Rend le test codeGenInst stable même si l’implémentation appelle ces méthodes.
//         doNothing().when(compiler).libererRegistre();
//         when(compiler.utiliserRegistre()).thenReturn(mock(GPRegister.class));
//         doNothing().when(compiler).addDereferencementNullLabel();
//     }



//     private EnvironmentExp stubObjectAsClass(AbstractExpr object) throws ContextualError {
//         Type typeClass = mock(Type.class);
//         ClassType classType = mock(ClassType.class);
//         ClassDefinition objClassDef = mock(ClassDefinition.class);
//         EnvironmentExp members = mock(EnvironmentExp.class);

//         when(typeClass.isClass()).thenReturn(true);
//         when(typeClass.asClassType(anyString(), any())).thenReturn(classType);
//         when(classType.getDefinition()).thenReturn(objClassDef);
//         when(objClassDef.getMembers()).thenReturn(members);

//         doReturn(typeClass).when(object).verifyExpr(compiler, localEnv, currentClass);
//         return members;
//     }

//     private ExpDefinition stubIdent(EnvironmentExp members, AbstractIdentifier methodName) throws ContextualError {
//         ExpDefinition def = mock(ExpDefinition.class);
//         when(methodName.verifyIdent(members)).thenReturn(def);
//         return def;
//     }

//     private MethodDefinition stubAsMethod(ExpDefinition def, int sigSize) throws ContextualError {
//         MethodDefinition mdef = mock(MethodDefinition.class);
//         Signature sig = mock(Signature.class);

//         when(def.isMethod()).thenReturn(true);
//         when(def.asMethodDefinition(anyString(), any())).thenReturn(mdef);
//         when(mdef.getSignature()).thenReturn(sig);
//         when(sig.size()).thenReturn(sigSize);

//         return mdef;
//     }

//     private static ListExpr listExprOf(AbstractExpr... exprs) {
//         ListExpr args = mock(ListExpr.class);
//         // doReturn est plus robuste ici que when(...) (et cohérent avec nos autres stubs)
//         doReturn(Arrays.asList(exprs)).when(args).getList();
//         doReturn(exprs.length == 0).when(args).isEmpty();
//         return args;
//     }


//     // verifyExpr :objet pas classe => ContextualError

//     @Test
//     void verifyExpr_throws_whenObjectIsNotClassType() throws ContextualError {
//         AbstractExpr object = mock(AbstractExpr.class);
//         AbstractIdentifier methodName = mock(AbstractIdentifier.class);
//         ListExpr arguments = listExprOf();

//         Type notClassType = mock(Type.class);
//         when(notClassType.isClass()).thenReturn(false);
//         doReturn(notClassType).when(object).verifyExpr(compiler, localEnv, currentClass);

//         MethodCall call = new MethodCall(object, methodName, arguments);

//         ContextualError ex = assertThrows(ContextualError.class,
//                 () -> call.verifyExpr(compiler, localEnv, currentClass));
//         assertEquals("The object is not of class type", ex.getMessage());
//     }

//     // verifyExpr :def pas method => ContextualError

//     @Test
//     void verifyExpr_throws_whenIdentIsNotMethod() throws ContextualError {
//         AbstractExpr object = mock(AbstractExpr.class);
//         AbstractIdentifier methodName = mock(AbstractIdentifier.class);
//         ListExpr arguments = listExprOf();

//         EnvironmentExp members = stubObjectAsClass(object);
//         ExpDefinition def = stubIdent(members, methodName);

//         when(def.isMethod()).thenReturn(false);

//         MethodCall call = new MethodCall(object, methodName, arguments);

//         ContextualError ex = assertThrows(ContextualError.class,
//                 () -> call.verifyExpr(compiler, localEnv, currentClass));
//         assertEquals("The method is not defined", ex.getMessage());
//     }


//     // verifyExpr :mauvais nb d'args => ContextualError

//     @Test
//     void verifyExpr_throws_whenWrongNumberOfArgs() throws ContextualError {
//         AbstractExpr object = mock(AbstractExpr.class);
//         AbstractIdentifier methodName = mock(AbstractIdentifier.class);

//         AbstractExpr a0 = mock(AbstractExpr.class);
//         ListExpr arguments = listExprOf(a0); // fourni 1

//         EnvironmentExp members = stubObjectAsClass(object);
//         ExpDefinition def = stubIdent(members, methodName);
//         stubAsMethod(def, 2); // attendu 2

//         MethodCall call = new MethodCall(object, methodName, arguments);

//         ContextualError ex = assertThrows(ContextualError.class,
//                 () -> call.verifyExpr(compiler, localEnv, currentClass));
//         assertTrue(ex.getMessage().startsWith("Wrong number of arguments: expected 2, got 1"));
//     }


//     // verifyExpr : chemin OK => verifyRValue + setType

//     @Test
//     void verifyExpr_ok_setsReturnType_andChecksArgs() throws ContextualError {
//         AbstractExpr object = mock(AbstractExpr.class);
//         AbstractIdentifier methodName = mock(AbstractIdentifier.class);

//         AbstractExpr a0 = mock(AbstractExpr.class);
//         AbstractExpr a1 = mock(AbstractExpr.class);
//         ListExpr arguments = listExprOf(a0, a1);

//         EnvironmentExp members = stubObjectAsClass(object);
//         ExpDefinition def = stubIdent(members, methodName);
//         MethodDefinition mdef = stubAsMethod(def, 2);

//         Signature sig = mdef.getSignature();
//         Type p0 = mock(Type.class);
//         Type p1 = mock(Type.class);
//         Type ret = mock(Type.class);

//         when(sig.paramNumber(0)).thenReturn(p0);
//         when(sig.paramNumber(1)).thenReturn(p1);
//         when(mdef.getType()).thenReturn(ret);

//         doReturn(a0).when(a0).verifyRValue(compiler, localEnv, currentClass, p0);
//         doReturn(a1).when(a1).verifyRValue(compiler, localEnv, currentClass, p1);

//         MethodCall call = new MethodCall(object, methodName, arguments);

//         Type out = call.verifyExpr(compiler, localEnv, currentClass);

//         assertSame(ret, out);
//         assertSame(ret, call.getType());

//         verify(a0).verifyRValue(compiler, localEnv, currentClass, p0);
//         verify(a1).verifyRValue(compiler, localEnv, currentClass, p1);
//     }


//     // decompile : avec objet + args

//     @Test
//     void decompile_withObjectAndArgs() {
//         AbstractExpr object = mock(AbstractExpr.class);
//         AbstractIdentifier methodName = mock(AbstractIdentifier.class);
//         ListExpr arguments = mock(ListExpr.class);

//         doAnswer(inv -> { ((IndentPrintStream) inv.getArgument(0)).print("obj"); return null; })
//                 .when(object).decompile(any(IndentPrintStream.class));
//         doAnswer(inv -> { ((IndentPrintStream) inv.getArgument(0)).print("m"); return null; })
//                 .when(methodName).decompile(any(IndentPrintStream.class));

//         when(arguments.isEmpty()).thenReturn(false);
//         doAnswer(inv -> { ((IndentPrintStream) inv.getArgument(0)).print("1,2"); return null; })
//                 .when(arguments).decompile(any(IndentPrintStream.class));

//         MethodCall call = new MethodCall(object, methodName, arguments);

//         ByteArrayOutputStream baos = new ByteArrayOutputStream();
//         IndentPrintStream ips = new IndentPrintStream(new PrintStream(baos));
//         call.decompile(ips);

//         assertEquals("obj.m(1,2)", baos.toString());
//     }


//     // decompile : sans objet + sans args
//     @Test
//     void decompile_withoutObject_noArgs() {
//         AbstractIdentifier methodName = mock(AbstractIdentifier.class);
//         ListExpr arguments = mock(ListExpr.class);

//         doAnswer(inv -> { ((IndentPrintStream) inv.getArgument(0)).print("m"); return null; })
//                 .when(methodName).decompile(any(IndentPrintStream.class));

//         when(arguments.isEmpty()).thenReturn(true);

//         MethodCall call = new MethodCall(null, methodName, arguments);

//         ByteArrayOutputStream baos = new ByteArrayOutputStream();
//         IndentPrintStream ips = new IndentPrintStream(new PrintStream(baos));
//         call.decompile(ips);

//         assertEquals("m()", baos.toString());
//         verify(arguments, never()).decompile(any(IndentPrintStream.class));
//     }


//     // prettyPrintChildren / iterChildren
//     @Test
//     void prettyPrintChildren_callsAllChildren_whenObjectNotNull() {
//         AbstractExpr object = mock(AbstractExpr.class);
//         AbstractIdentifier methodName = mock(AbstractIdentifier.class);
//         ListExpr arguments = mock(ListExpr.class);

//         MethodCall call = new MethodCall(object, methodName, arguments);
//         call.prettyPrintChildren(System.out, "P");

//         verify(object).prettyPrint(any(PrintStream.class), eq("P"), eq(false));
//         verify(methodName).prettyPrint(any(PrintStream.class), eq("P"), eq(false));
//         verify(arguments).prettyPrint(any(PrintStream.class), eq("P"), eq(true));
//     }

//     @Test
//     void iterChildren_callsChildren_whenObjectNull() {
//         AbstractIdentifier methodName = mock(AbstractIdentifier.class);
//         ListExpr arguments = mock(ListExpr.class);
//         TreeFunction f = mock(TreeFunction.class);

//         MethodCall call = new MethodCall(null, methodName, arguments);
//         call.iterChildren(f);

//         verify(methodName).iter(f);
//         verify(arguments).iter(f);
//     }

//     // simuleExecutionNbRegistres
//     @Test
//     void simuleExecutionNbRegistres_objectNull_usesSimulerAllocation() {
//         when(compiler.simulerAllocationRegistre()).thenReturn(1);

//         ListExpr arguments = listExprOf(); // vide

//         MethodCall call = new MethodCall(null, mock(AbstractIdentifier.class), arguments);

//         int max = call.simuleExecutionNbRegistres(compiler);
//         assertEquals(1, max);


//         // verify(compiler, atLeastOnce()).commencerSimulationRegistre(true);
//         // verify(compiler, atLeastOnce()).arreterSimulationRegistre(false);
//         verify(compiler).simulerAllocationRegistre();
//     }
// >>>>>>> 371d58db726fc9137a184c1683d38fa5521df9e9

//     @Test
//     void simuleExecutionNbRegistres_takesMaxAmongObjectAndArgs() {
//         AbstractExpr object = mock(AbstractExpr.class);
//         when(object.simuleExecutionNbRegistres(compiler)).thenReturn(2);

//         AbstractExpr a0 = mock(AbstractExpr.class);
//         AbstractExpr a1 = mock(AbstractExpr.class);
//         when(a0.simuleExecutionNbRegistres(compiler)).thenReturn(1);
//         when(a1.simuleExecutionNbRegistres(compiler)).thenReturn(4);

//         ListExpr arguments = listExprOf(a0, a1);

//         MethodCall call = new MethodCall(object, mock(AbstractIdentifier.class), arguments);

//         assertEquals(4, call.simuleExecutionNbRegistres(compiler));
//     }

//     // codeGenInst

//     @Test
//     void codeGenInst_emitsExpectedInstructionKinds() {
//         AbstractExpr object = mock(AbstractExpr.class);
//         GPRegister rObj = mock(GPRegister.class);
//         doReturn(rObj).when(object).codeGenInst(compiler);

//         AbstractExpr arg0 = mock(AbstractExpr.class);
//         GPRegister rArg0 = mock(GPRegister.class);
//         doReturn(rArg0).when(arg0).codeGenInst(compiler);

//         ListExpr arguments = listExprOf(arg0);

//         AbstractIdentifier methodName = mock(AbstractIdentifier.class);
//         MethodDefinition mdef = mock(MethodDefinition.class);
//         when(methodName.getMethodDefinition()).thenReturn(mdef);
//         when(mdef.getIndex()).thenReturn(0);

//         MethodCall call = new MethodCall(object, methodName, arguments);

//         ArgumentCaptor<Instruction> cap = ArgumentCaptor.forClass(Instruction.class);
//         doNothing().when(compiler).addInstruction(cap.capture());

//         GPRegister res = call.codeGenInst(compiler);

//         assertSame(Register.R0, res);

//         assertTrue(cap.getAllValues().stream().anyMatch(i -> i instanceof ADDSP));
//         assertTrue(cap.getAllValues().stream().anyMatch(i -> i instanceof STORE));
//         assertTrue(cap.getAllValues().stream().anyMatch(i -> i instanceof LOAD));
//         assertTrue(cap.getAllValues().stream().anyMatch(i -> i instanceof CMP));
//         assertTrue(cap.getAllValues().stream().anyMatch(i -> i instanceof BEQ));
//         assertTrue(cap.getAllValues().stream().anyMatch(i -> i instanceof BSR));
//         assertTrue(cap.getAllValues().stream().anyMatch(i -> i instanceof SUBSP));

//         verify(compiler, atLeastOnce()).addDereferencementNullLabel();
//     }
// }
