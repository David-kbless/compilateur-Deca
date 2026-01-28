package fr.ensimag.deca.context;

import java.util.HashMap;
import java.util.Map;

import fr.ensimag.deca.tools.SymbolTable.Symbol;

/**
 * Dictionary associating identifier's ExpDefinition to their names.
 * 
 * This is actually a linked list of dictionaries: each EnvironmentExp has a
 * pointer to a parentEnvironment, corresponding to superblock (eg superclass).
 * 
 * The dictionary at the head of this list thus corresponds to the "current" 
 * block (eg class).
 * 
 * Searching a definition (through method get) is done in the "current" 
 * dictionary and in the parentEnvironment if it fails. 
 * 
 * Insertion (through method declare) is always done in the "current" dictionary.
 * 
 * @author gl54
 * @date 01/01/2026
 */
public class EnvironmentExp {
    // A FAIRE : implémenter la structure de donnée représentant un
    // environnement (association nom -> définition, avec possibilité
    // d'empilement).

    private Map<Symbol, ExpDefinition> env;
    EnvironmentExp parentEnvironment;
    
    public EnvironmentExp(EnvironmentExp parentEnvironment) {
        this.env = new HashMap<Symbol, ExpDefinition>();
        this.parentEnvironment = parentEnvironment;
    }

    public static class DoubleDefException extends Exception {
        private static final long serialVersionUID = -2733379901827316441L;
    }


    public EnvironmentExp getParentEnvironment() {
        return parentEnvironment;
    }
    
    /**
     * Return the definition of the symbol in the environment, or null if the
     * symbol is undefined.
     */
    public ExpDefinition get(Symbol key) {
        ExpDefinition def = env.get(key);
        if (def != null) {
            return def;
        }
        if (parentEnvironment != null) {
            return parentEnvironment.get(key);
        }
        return null;
    }

    public ExpDefinition getLocal(Symbol key) {
        ExpDefinition def = env.get(key);
        if (def != null) {
            return def;
        }
        return null;
    }

    /**
     * Add the definition def associated to the symbol name in the environment.
     * 
     * Adding a symbol which is already defined in the environment,
     * - throws DoubleDefException if the symbol is in the "current" dictionary 
     * - or, hides the previous declaration otherwise.
     * 
     * @param name
     *            Name of the symbol to define
     * @param def
     *            Definition of the symbol
     * @throws DoubleDefException
     *             if the symbol is already defined at the "current" dictionary
     *
     */
    public void declare(Symbol name, ExpDefinition def) throws DoubleDefException {
        if (env.containsKey(name)) {
            throw new DoubleDefException();
        }
        env.put(name, def);    
    }

    /**
     * Retourne un nouvel environnement correspondant à env1 / env2
     * (env1 au-dessus de env2)
     */
    public static EnvironmentExp stack(EnvironmentExp env1, EnvironmentExp env2) {
        env1.parentEnvironment = env2;
        return env1;
    }

    /**
     * Addition d'environnements : env ⊕ other
     * Copie les définitions de other dans env (courant uniquement)
     */
    public void add(EnvironmentExp other) throws DoubleDefException {
        if (other == null) return;

        for (Map.Entry<Symbol, ExpDefinition> entry : other.env.entrySet()) {
            Symbol name = entry.getKey();
            ExpDefinition def = entry.getValue();

            if (this.env.containsKey(name)) {
                throw new DoubleDefException();
            }
            this.env.put(name, def);
        }
    }


}
