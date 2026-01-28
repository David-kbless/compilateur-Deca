package fr.ensimag.deca.tools;

import java.util.HashMap;
import java.util.Map;

/**
 * Manage unique symbols.
 * 
 * A Symbol contains the same information as a String, but the SymbolTable
 * ensures the uniqueness of a Symbol for a given String value. Therefore,
 * Symbol comparison can be done by comparing references, and the hashCode()
 * method of Symbols can be used to define efficient HashMap (no string
 * comparison or hashing required).
 * 
 * @author gl54
 * @date 01/01/2026
 */
public class SymbolTable {
    private Map<String, Symbol> map = new HashMap<String, Symbol>();

    /**
     * Create or reuse a symbol.
     * 
     * If a symbol already exists with the same name in this table, then return
     * this Symbol. Otherwise, create a new Symbol and add it to the table.
     */
    public Symbol create(String name) {
        // Un identifiant null n'est pas autorisé en Deca --
        if (name == null) {
            throw new IllegalArgumentException("Symbol name cannot be null");
        }

        Symbol s = map.get(name);
        if (s == null) {
            s = new Symbol(name);
            map.put(name, s);
        }
        return s;

        //throw new UnsupportedOperationException("Symbol creation");
    }

    // Return the number of symbols in the table --
    public int size() {
        return map.size();
    }

    // Check if a symbol with the given name exists in the table --
    public boolean contains(String name) {
        return map.containsKey(name);
    }

    /**
     * Symbols are unique per SymbolTable.
     * Two symbols with the same name are guaranteed to be the same object.
     */
    public static class Symbol {
        // Constructor is private, so that Symbol instances can only be created
        // through SymbolTable.create factory (which thus ensures uniqueness
        // of symbols).
        private Symbol(String name) {
            super();
            this.name = name;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return name;
        }

        // Equality is based on reference equality, ensured by SymbolTable --
        @Override
        public int hashCode() {
            return System.identityHashCode(this);
        }

        @Override
        public boolean equals(Object obj) {
            return this == obj;
        }

        private String name;
    }
}
