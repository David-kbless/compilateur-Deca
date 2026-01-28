
package fr.ensimag.deca.tools;

import org.junit.jupiter.api.Test;

import fr.ensimag.deca.tools.SymbolTable.Symbol;

import static org.junit.jupiter.api.Assertions.*;

class SymbolTableTest {

    @Test
    void testSymbolTableMethods() {
        SymbolTable st = new SymbolTable();

        // create()
        Symbol s1 = st.create("x");
        Symbol s2 = st.create("y");
        assertNotNull(s1);
        assertNotNull(s2);

        // contains()
        assertTrue(st.contains("x"));
        assertFalse(st.contains("z"));

        // size()
        assertEquals(2, st.size());

        // equals(Object)
        assertEquals(st, st);
        assertNotEquals(st, null);
        assertNotEquals(st, "string");
    }

    @Test
    void testCreateSymbol() {
        SymbolTable table = new SymbolTable();

        // 1. Cas normal
        Symbol s1 = table.create("x");
        assertNotNull(s1);
        assertEquals("x", s1.getName());

        // 2. Cas null → exception
        Exception e = assertThrows(IllegalArgumentException.class, () -> table.create(null));
        assertEquals("Symbol name cannot be null", e.getMessage());
    }

    @Test
    void testEquals() {
        SymbolTable st = new SymbolTable();
        Symbol s1 = st.create("a");
        Symbol s2 = st.create("a");
        
        // 1. Comparaison avec lui-même
        assertTrue(s1.equals(s1));

        // 2. Comparaison avec un autre objet (même contenu mais instance différente)
        assertTrue(s1.equals(s2));
    }

}
