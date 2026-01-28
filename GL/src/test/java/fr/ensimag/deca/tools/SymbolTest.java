package fr.ensimag.deca.tools;

import fr.ensimag.deca.tools.SymbolTable.Symbol;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 * Example JUnit class. A JUnit class must be named TestXXX or YYYTest. JUnit
 * will run each methods annotated with @Test.
 */
public class SymbolTest {
    @Test
    public void testSymbol() {
        SymbolTable t = new SymbolTable();
        Symbol s1 = t.create("foo");
        // Check that two objects are equal (using Object.equals())
        assertEquals(s1.getName(), "foo");
        Symbol s2 = t.create("foo");
        // Check that two objects are the same (same reference)
        assertSame(s1, s2);
        Symbol s3 = t.create("foobar");
        assertNotSame(s1, s3);
    }

    @Test
    public void multipleTables() {
        SymbolTable t1 = new SymbolTable();
        SymbolTable t2 = new SymbolTable();
        Symbol s1 = t1.create("foo");
        Symbol s2 = t2.create("foo");
        assertEquals(s1.getName(), s2.getName());
        assertNotSame(s1, s2);
    }

    //Test des chaines vides --
    @Test
    public void testEmptyString() {
        SymbolTable t = new SymbolTable();
        Symbol s1 = t.create("");
        assertEquals(s1.getName(), "");
        Symbol s2 = t.create("");
        assertSame(s1, s2);
    }

    // Test de chaines égales mais objets String différents --
    @Test
    public void testDifferentStringObjects() {
        SymbolTable t = new SymbolTable();
        String str1 = new String("bar");
        String str2 = new String("bar");
        Symbol s1 = t.create(str1);
        Symbol s2 = t.create(str2);
        assertSame(s1, s2);
    }

    // Test massif (Robustesse); Detecte les erreurs de gestion interne --
    @Test
    public void manySymbols() {
        SymbolTable t = new SymbolTable();
        final int N = 1000;
        Symbol[] symbols = new Symbol[N];
        // Création de N symboles différents --
        for (int i = 0; i < N; i++) {
            String name = "symb_" + i;
            symbols[i] = t.create(name);
            assertEquals(symbols[i].getName(), name);
        }
        // Vérification que les symboles sont bien uniques --
        for (int i = 0; i < N; i++) {
            String name = "symb_" + i;
            Symbol s = t.create(name);
            assertSame(s, symbols[i]);
        }
    }
}