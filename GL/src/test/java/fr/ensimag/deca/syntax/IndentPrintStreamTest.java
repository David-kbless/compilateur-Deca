
package fr.ensimag.deca.syntax;

import org.junit.jupiter.api.Test;

import fr.ensimag.deca.tools.IndentPrintStream;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import static org.junit.jupiter.api.Assertions.*;

class IndentPrintStreamTest {

    @Test
    void testIndentPrintStream() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        IndentPrintStream ips = new IndentPrintStream(new PrintStream(baos));

        // tester indent et unindent
        ips.indent();
        ips.printIndent();
        ips.print("Hello");
        ips.println();
        ips.unindent();
        ips.printIndent();
        ips.println("World");
        ips.print('!');

        String output = baos.toString();
        assertTrue(output.contains("Hello"), "Doit contenir 'Hello'");
        assertTrue(output.contains("World"), "Doit contenir 'World'");
        assertTrue(output.contains("!"), "Doit contenir '!'");
    }
}
