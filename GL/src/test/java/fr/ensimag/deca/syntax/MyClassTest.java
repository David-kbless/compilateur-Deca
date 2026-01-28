package fr.ensimag.deca.syntax;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MyClassTest {

    @Test
    void testEqualsObject() {
        MyClass a = new MyClass(5);
        MyClass b = new MyClass(5);
        MyClass c = new MyClass(6);

        // 1. Comparaison avec lui-même
        assertTrue(a.equals(a));

        // 2. Comparaison avec null
        assertFalse(a.equals(null));

        // 3. Comparaison avec un objet d’un autre type
        assertFalse(a.equals("string"));

        // 4. Comparaison avec un objet du même type
        assertFalse(a.equals(b));   // champs identiques car equals(Object) compare les références
        assertFalse(a.equals(c));  // champs différents
    }
}

