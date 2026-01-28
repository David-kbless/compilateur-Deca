package fr.ensimag.deca.tree;

import java.io.PrintStream;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ClassType;
import fr.ensimag.deca.context.Signature;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.ExpDefinition;
import fr.ensimag.deca.context.MethodDefinition;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.deca.context.EnvironmentExp.DoubleDefException;



public class DeclMethod extends AbstractDeclMethod {
    private final Visibility visibility;
    private final AbstractIdentifier type;
    private final AbstractIdentifier name;
    private final ListDeclParam params;
    private final ListDeclVar localVars;
    private final ListInst body;
    private final String asmCode;
    private int nbMaxRegUtilises = 0;
   

    public DeclMethod(Visibility v, AbstractIdentifier type, AbstractIdentifier name,
                      ListDeclParam params, ListDeclVar localVars, ListInst body) {
        this.visibility = v;
        this.type = type;
        this.name = name;
        this.params = params;
        this.localVars = localVars;
        this.body = body;
        this.asmCode = null;
    }

    public DeclMethod(Visibility v, AbstractIdentifier type, AbstractIdentifier name,
                      ListDeclParam params, ListDeclVar localVars, ListInst body,
                      String asmCode) {
        this.visibility = v;
        this.type = type;
        this.name = name;
        this.params = params;
        this.localVars = localVars;
        this.body = body;
        this.asmCode = asmCode;
    }

    @Override
    public void decompile(IndentPrintStream s) {
        if (asmCode != null) {
            s.print("asm(");
            s.print(asmCode);
            s.println(");");
        } else {
            if (visibility != null) {
                visibility.decompile(s);
                s.print(" ");
            }
            s.print(" ");
            type.decompile(s);
            s.print(" ");
            name.decompile(s);
            s.print("(");   
            params.decompile(s);
            s.println(") {");
            s.indent();
            localVars.decompile(s);
            body.decompile(s);
            s.unindent();
            s.println("}");
        }
    }

    public boolean isVoid() {
        // On récupère le type associé à l'identificateur du type de retour
        return this.name.getMethodDefinition().getType().isVoid();
    }

    // Getters
    public AbstractIdentifier getType() {
        return type;
    }

    public AbstractIdentifier getName() {
        return name;
    }

    public ListDeclParam getParams() {
        return params;
    }

    public ListDeclVar getLocalVars() {
        return localVars;
    }

    public ListInst getBody() {
        return body;
    }

    public String getAsmCode() {
        return asmCode;
    }

    public Visibility getVisibility() {
        return visibility;
    }

    public int getNbMaxRegUtilises() {
        return nbMaxRegUtilises;
    }

    

    @Override
    protected EnvironmentExp verifyDeclMethodMembers(DecacCompiler compiler, ClassDefinition currentClass, int index)
            throws ContextualError {
        Type realType = type.verifyType(compiler);
        Symbol methodName = name.getName();
        Signature sig = params.verifyListDeclParam(compiler);
        
        ClassDefinition superClass = currentClass.getSuperClass();
        int finalIndex = index; // On garde l'index passé par défaut

        if (superClass != null){
            ExpDefinition def = superClass.getMembers().get(methodName);
            MethodDefinition superMethod = null;
            
            if (def != null) {
                // CAS REDÉFINITION (Override) : on cast et on récupère l'index
                superMethod = def.asMethodDefinition(
                    "L'identificateur " + methodName + " n'est pas une méthode.", 
                    this.getLocation()
                );
                finalIndex = superMethod.getIndex();
                
            }
            if (superMethod != null) {
                // Si c'est un override, on REPREND l'index du parent !
                finalIndex = superMethod.getIndex();
            }
            
            EnvironmentExp envExpSuper = superClass.getMembers();
            ExpDefinition superDef = envExpSuper.get(methodName);
            if (superDef != null){
                if(!superDef.isMethod()){
                    throw new ContextualError("Method " + methodName + " is not a method in the superClass", getLocation());
                }
                MethodDefinition superMethodDef = superDef.asMethodDefinition("could not convert to MethodDefinition", getLocation());
                
                // --- AJOUT : Gestion de l'index pour la redéfinition ---
                finalIndex = superMethodDef.getIndex(); 
                
                Signature superSig = superMethodDef.getSignature();
                if (!sig.equals(superSig)){
                    throw new ContextualError("Method " + methodName + " has a different signature than in superclass", getLocation());
                }
                Type type2 = superMethodDef.getType();
                if (!realType.sameType(type2)) {
                    if (!(realType.isClass()&& ((ClassType) realType).isSubClassOf((ClassType) type2))) {
                        throw new ContextualError("Method " + methodName+ " has incompatible return type with superclass",getLocation());
                    }
                }
            }
        }
        
        // On utilise finalIndex (soit le nouveau, soit celui du parent si override)
        MethodDefinition methodDef = new MethodDefinition(realType, getLocation(), sig, finalIndex);

        currentClass.setMethod(finalIndex, methodDef); // Enregistre dans la TreeMap
        methodDef.setLabel(new Label("code." + currentClass.getType().getName().getName() + "." + methodName.getName()));

        try {
            EnvironmentExp envExp = new EnvironmentExp(null);
            envExp.declare(methodName, methodDef);
            name.setDefinition(methodDef);
            return envExp;
        } catch (DoubleDefException e) {
            return null; // ou gérer l'erreur selon le style de Safwane
        }
    }

    @Override
    protected void verifyMethodBody(DecacCompiler compiler, EnvironmentExp envExp, ClassDefinition currentClass)
            throws ContextualError {
        // Implementation of the verification logic for the method body
        // This is a placeholder; actual implementation would depend on the compiler's context
        EnvironmentExp envExpParams = params.verifyListDeclParamBody(compiler);
        Type returnType = type.verifyType(compiler);
        if (asmCode != null) {
            // If the method is defined using asm, we skip body verification
            return;
        }
        localVars.verifyListDeclVariable(compiler, envExpParams, currentClass);
        body.verifyListInst(compiler, EnvironmentExp.stack(envExpParams, envExp), currentClass, returnType);
        nbMaxRegUtilises = body.simuleExecutionNbRegistres(compiler);
        
    }

    @Override
    protected void codeGenMethod(DecacCompiler compiler) {
        // Implementation of the code generation logic for the method declaration
        // This is a placeholder; actual implementation would depend on the compiler's context
        throw new UnsupportedOperationException("Unimplemented method 'verifyMethodBody'");
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        type.prettyPrint(s, prefix, false);
        name.prettyPrint(s, prefix, false);
        params.prettyPrint(s, prefix, false);
        localVars.prettyPrint(s, prefix, false);
        body.prettyPrint(s, prefix, true);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        type.iter(f);
        name.iter(f);
        params.iter(f);
        localVars.iter(f);
        body.iter(f);
    }
    
}
