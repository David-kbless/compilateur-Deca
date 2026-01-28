package fr.ensimag.deca.syntax;

import fr.ensimag.deca.CompilerOptions;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.tree.AbstractProgram;
import org.antlr.v4.runtime.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ParserGeneratedCodeTest {

    private AbstractProgram parse(String code) {
        DecaLexer lexer = new DecaLexer(CharStreams.fromString(code));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        DecaParser parser = new DecaParser(tokens);
        parser.setDecacCompiler(new DecacCompiler(new CompilerOptions(), null));
        return parser.parseProgramAndManageErrors(System.err);
    }

    // @Test
    // void coverExpressionsAndSempred() {
    //     String code = """
    //     {
    //         int a;
    //         boolean b;

    //         // sum_expr_sempred ( + - )
    //         a = 1 + 2 + 3 + 4 + 5;

    //         // mult_expr_sempred ( * / )
    //         a = 1 * 2 * 3 / 4 * 5;

    //         // inequality_expr_sempred
    //         b = 1 < 2 < 3 < 4;

    //         // eq_neq_expr_sempred
    //         b = 1 == 2 != 3 == 4;

    //         // and_expr_sempred
    //         b = true && false && true && false;

    //         // or_expr_sempred
    //         b = true || false || true || false;

    //         print(a);
    //     }
    //     """;

    //     AbstractProgram prog = parse(code);
    //     assertNotNull(prog);
    // }

    // @Test
    // void coverSelectExpr() {
    //     String code = """
    //     class C { int x; }
    //     class B { C c; }
    //     class A { B b; }

    //     {
    //         A a;
    //         a = new A();
    //         print(a.b.c.x);
    //     }
    //     """;

    //     AbstractProgram prog = parse(code);
    //     assertNotNull(prog);
    // }

    @Test
    void coverGeneratedParserMethods() {
        DecaParser parser = new DecaParser(
                new CommonTokenStream(
                        new DecaLexer(CharStreams.fromString("{ }"))
                )
        );

        assertNotNull(parser.getTokenNames());
        assertNotNull(parser.getVocabulary());
        assertNotNull(parser.getGrammarFileName());
        assertNotNull(parser.getRuleNames());
        assertNotNull(parser.getSerializedATN());
        assertNotNull(parser.getATN());
    }
}
