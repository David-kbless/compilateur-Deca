// package fr.ensimag.deca.syntax;

// import fr.ensimag.deca.DecacCompiler;
// import fr.ensimag.deca.CompilerOptions;
// import fr.ensimag.deca.tree.AbstractProgram;
// import org.antlr.v4.runtime.CommonTokenStream;
// import org.junit.jupiter.api.Test;

// import java.io.File;

// import static org.junit.jupiter.api.Assertions.*;

// /**
//  * Tests du parser Deca sur les programmes valides et invalides.
//  */
// public class ParserTest {

//     private static final String VALID_DIR = "src/test/deca/syntax/valid/provided/";
//     private static final String INVALID_DIR = "src/test/deca/syntax/invalid/provided/";

//     /**
//      * Test parsing de tous les fichiers Deca valides.
//      */
//     @Test
//     public void testValidPrograms() throws Exception {
//         File validDir = new File(VALID_DIR);
//         File[] validFiles = validDir.listFiles((dir, name) -> name.endsWith(".deca"));
//         assertNotNull(validFiles, "Le dossier des fichiers valides doit exister et contenir des fichiers .deca");

//         for (File file : validFiles) {
//             DecaLexer lexer = AbstractDecaLexer.createLexerFromArgs(new String[]{file.getAbsolutePath()});
//             CommonTokenStream tokens = new CommonTokenStream(lexer);
//             DecaParser parser = new DecaParser(tokens);
//             DecacCompiler compiler = new DecacCompiler(new CompilerOptions(), file);
//             parser.setDecacCompiler(compiler);

//             AbstractProgram prog = parser.parseProgramAndManageErrors(System.err);

//             // Assertions : le parse doit réussir pour un programme valide
//             assertNotNull(prog, "Le programme valide " + file.getName() + " doit être parsé");

//             // Exécute prettyPrint pour couvrir ce code dans Jacoco
//             // prog.prettyPrint(System.out);
//         }
//     }

//     /**
//      * Test parsing de tous les fichiers Deca invalides.
//      */
//     @Test
//     public void testInvalidPrograms() throws Exception {
//         File invalidDir = new File(INVALID_DIR);
//         File[] invalidFiles = invalidDir.listFiles((dir, name) -> name.endsWith(".deca"));
//         assertNotNull(invalidFiles, "Le dossier des fichiers invalides doit exister et contenir des fichiers .deca");

//         for (File file : invalidFiles) {
//             DecaLexer lexer = AbstractDecaLexer.createLexerFromArgs(new String[]{file.getAbsolutePath()});
//             CommonTokenStream tokens = new CommonTokenStream(lexer);
//             DecaParser parser = new DecaParser(tokens);
//             DecacCompiler compiler = new DecacCompiler(new CompilerOptions(), file);
//             parser.setDecacCompiler(compiler);

//             AbstractProgram prog = parser.parseProgramAndManageErrors(System.err);

//             // Assertions : le parse doit échouer pour un programme invalide
//             assertNull(prog, "Le programme invalide " + file.getName() + " ne doit pas être parsé");
//         }
//     }
// }
