package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassType;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.EnvironmentType;
import fr.ensimag.deca.context.TypeDefinition;
import fr.ensimag.deca.tools.IndentPrintStream;
import java.io.PrintStream;

import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.tools.SymbolTable.Symbol;

import org.apache.commons.lang.Validate;


/**
 * Declaration of a class (<code>class name extends superClass {members}<code>).
 * 
 * @author gl54
 * @date 01/01/2026
 */
public class DeclClass extends AbstractDeclClass {

    //On a besoin de ces champs pour pouvoir appliquer la regle 
    private final AbstractIdentifier name;
    private final AbstractIdentifier superClass;
    private final ListDeclField fields;
    private final ListDeclMethod methods;
    
    

    public DeclClass(AbstractIdentifier name, AbstractIdentifier superClass,
            ListDeclField fields, ListDeclMethod methods) {
            // le nom de la classe ne peut pas être null
            Validate.notNull(name);
            this.name = name;
            this.superClass = superClass;
            this.fields = fields;
            this.methods = methods;
    }

    public AbstractIdentifier getName() {
        return name;
    }

    public AbstractIdentifier getSuperClass() {
        return superClass;
    }

    public ListDeclField getFields() {
        return fields;
    }

    public ListDeclMethod getMethods() {
        return methods;
    }
    

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("class ");
        name.decompile(s);
        if (superClass != null) {
            s.print(" extends ");
            superClass.decompile(s);
        }
        s.println(" {");
        s.indent();
        fields.decompile(s);
        methods.decompile(s);
        s.unindent();
        s.println("}");
    }

    @Override
    protected void verifyClass(DecacCompiler compiler) throws ContextualError {
        EnvironmentType envType = compiler.environmentType;
        Symbol classSymbol = name.getName();

        // 1) La classe ne doit pas déjà exister
        if (envType.defOfType(classSymbol) != null) {
            throw new ContextualError("Class " + classSymbol + " already defined", getLocation());
        }

        ClassDefinition superClassDef;

        // 2) Vérification de la super-classe
        if (superClass != null) {
            Symbol superSymbol = superClass.getName();
            TypeDefinition def = envType.defOfType(superSymbol);

            if (def == null) {
                throw new ContextualError("Superclass " + superSymbol + " is undefined", superClass.getLocation());
            }

            superClassDef = def.asClassDefinition(superSymbol + " is not a class", superClass.getLocation());
            superClass.setDefinition(superClassDef);
        } 
        else {
            // 3) Pas de extends → Object
            superClassDef = compiler.environmentType.OBJECT.getDefinition();
        }

        // 4) Création de la définition de la classe
        ClassType classType = new ClassType(classSymbol, getLocation(), superClassDef);

        // 5) Ajout dans l'environnement des types
        envType.declare(classSymbol, classType.getDefinition());

        // 6) Lien AST → définition
        name.setDefinition(classType.getDefinition());
    }


    @Override
    protected void verifyClassMembers(DecacCompiler compiler)
            throws ContextualError {
        ClassDefinition currentClass = name.getClassDefinition();
        ClassDefinition superClass = currentClass.getSuperClass();

        if (superClass != null) {

            // On recopie TOUTES les méthodes du parent (index -> Definition)
            currentClass.getMethodTable().putAll(superClass.getMethodTable());
            
            // On initialise le compteur de méthodes au même niveau que le parent
            currentClass.setNumberOfMethods(superClass.getNumberOfMethods());

            // ajouter par @Ousmane pour @Safwane
            currentClass.setNumberOfFields(superClass.getNumberOfFields());
        }

            EnvironmentExp envExpF = fields.verifyListDeclField(compiler, superClass, currentClass);
            
            // Attention : il faut que Safwane passe l'index courant à verifyListMethodMembers
            EnvironmentExp envExpM = methods.verifyListMethodMembers(compiler, superClass, currentClass); 
            
            try {
                envExpF.add(envExpM);
            } catch (EnvironmentExp.DoubleDefException e) {
                // ...
            }
            EnvironmentExp newEnvExp = EnvironmentExp.stack(envExpF, superClass.getMembers());
            currentClass.setMembers(newEnvExp);
    }
    
    @Override
    protected void verifyClassBody(DecacCompiler compiler) throws ContextualError {
        ClassDefinition currentClass = name.getClassDefinition();
        EnvironmentExp envExp = currentClass.getMembers();
        fields.verifyListFieldBody(compiler, envExp, currentClass);
        methods.verifyListMethodBody(compiler, envExp, currentClass);
    }


    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        name.prettyPrint(s, prefix, false);
        if (superClass != null) {
            superClass.prettyPrint(s, prefix, false);
        }
        fields.prettyPrint(s, prefix, false);
        methods.prettyPrint(s, prefix, true); 
        }

    @Override
    protected void iterChildren(TreeFunction f) {
        name.iter(f);
        if (superClass != null) {
            superClass.iter(f);
        }
        fields.iter(f);
        methods.iter(f);
    }

}
