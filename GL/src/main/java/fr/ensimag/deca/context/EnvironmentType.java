package fr.ensimag.deca.context;

import fr.ensimag.deca.DecacCompiler;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.deca.tree.Location;
import fr.ensimag.ima.pseudocode.Label;

// A FAIRE: étendre cette classe pour traiter la partie "avec objet" de Déca
/**
 * Environment containing types. Initially contains predefined identifiers, more
 * classes can be added with declareClass().
 *
 * @author gl54
 * @date 01/01/2026
 */
public class EnvironmentType {
    public EnvironmentType(DecacCompiler compiler) {
        
        envTypes = new HashMap<Symbol, TypeDefinition>();
        
        Symbol intSymb = compiler.createSymbol("int");
        INT = new IntType(intSymb);
        envTypes.put(intSymb, new TypeDefinition(INT, Location.BUILTIN));

        Symbol floatSymb = compiler.createSymbol("float");
        FLOAT = new FloatType(floatSymb);
        envTypes.put(floatSymb, new TypeDefinition(FLOAT, Location.BUILTIN));

        Symbol voidSymb = compiler.createSymbol("void");
        VOID = new VoidType(voidSymb);
        envTypes.put(voidSymb, new TypeDefinition(VOID, Location.BUILTIN));

        Symbol booleanSymb = compiler.createSymbol("boolean");
        BOOLEAN = new BooleanType(booleanSymb);
        envTypes.put(booleanSymb, new TypeDefinition(BOOLEAN, Location.BUILTIN));

        Symbol stringSymb = compiler.createSymbol("string");
        STRING = new StringType(stringSymb);
        // not added to envTypes, it's not visible for the user.

        Symbol nullSymb = compiler.createSymbol("null");
        NULL = new NullType(nullSymb);
        envTypes.put(nullSymb, new TypeDefinition(NULL, Location.BUILTIN));

        Symbol objectSymb = compiler.createSymbol("Object");
        OBJECT = new ClassType(objectSymb, Location.BUILTIN, null);
        //envTypes.put(objectSymb, new TypeDefinition(OBJECT, Location.BUILTIN));

        // on creer l'objet : 
        ClassDefinition objectDef = OBJECT.getDefinition();

        Signature equalsSig = new Signature();
        equalsSig.add(OBJECT);
        MethodDefinition equalsDef = new MethodDefinition(BOOLEAN, Location.BUILTIN, equalsSig, 0);
        equalsDef.setLabel(new Label("code.Object.equals"));

        try {
            // declaration de equals dans les members de object : 
            objectDef.getMembers().declare(compiler.createSymbol("equals"), equalsDef);
            objectDef.setMethod(0, equalsDef);
        } catch (EnvironmentExp.DoubleDefException e) {
            // Cette erreur ne peut pas arriver ici car c'est la toute première
            // decl dans l'environnement d'Object.
            throw new RuntimeException("Erreur fatale : impossible de déclarer Object.equals", e);
        }
        objectDef.setNumberOfMethods(1);
        envTypes.put(objectSymb, objectDef);
    
    }

    private final Map<Symbol, TypeDefinition> envTypes;

    public TypeDefinition defOfType(Symbol s) {
        return envTypes.get(s);
    }

    public void declare(Symbol s, TypeDefinition def) {
        envTypes.put(s, def);
    }

    public Set<Symbol> getAllSymbols() {
        return envTypes.keySet();
    }


    /**
     * Empile envTop sur envBottom : toutes les définitions de envTop remplacent
     * celles de envBottom si même symbole, sinon sont ajoutées.
     * Retourne un nouvel EnvironmentType résultat.
     */
    public static EnvironmentType stackWithOverwrite(EnvironmentType envTop, EnvironmentType envBottom) {
        EnvironmentType result = new EnvironmentType(null);

        // Copie d'abord tout de envBottom
        for (Symbol s : envBottom.getAllSymbols()) {
            result.declare(s, envBottom.defOfType(s));
        }

        // Copie / remplace tout de envTop
        for (Symbol s : envTop.getAllSymbols()) {
            result.declare(s, envTop.defOfType(s));  // remplace automatiquement
        }

        return result;
    }

    public final VoidType    VOID;
    public final IntType     INT;
    public final FloatType   FLOAT;
    public final StringType  STRING;
    public final BooleanType BOOLEAN;
    public final NullType NULL;
    public final ClassType OBJECT;
}
