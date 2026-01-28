package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.ClassDefinition;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.MethodDefinition;
import fr.ensimag.deca.tools.IndentPrintStream;

public class ListDeclMethod extends TreeList<AbstractDeclMethod> {

    // // Pass 1 of [SyntaxeContextuelle] A revoir ...
    // public EnvironmentExp verifyListMethodMembers(DecacCompiler compiler, ClassDefinition superClass,
    //                                      ClassDefinition currentClass)
    //         throws ContextualError {

    //     EnvironmentExp envExpR = new EnvironmentExp(null);
    //     int index = currentClass.getNumberOfMethods();
    //     for (AbstractDeclMethod method : getList()) {
    //         EnvironmentExp envExp = method.verifyDeclMethodMembers(compiler, superClass, index);
    //         try {
    //             envExpR.add(envExp);
    //         } catch (EnvironmentExp.DoubleDefException e) {
    //             //throw new ContextualError("Method " + method.getName().getName() + " is already defined", method.getLocation());
    //         }
    //         currentClass.incNumberOfMethods();
    //         index++;
    //     }
    //     return envExpR;
    // }

    public EnvironmentExp verifyListMethodMembers(DecacCompiler compiler, ClassDefinition superClass,
                                     ClassDefinition currentClass)
        throws ContextualError {

        EnvironmentExp envExpR = new EnvironmentExp(null);
        
        for (AbstractDeclMethod method : getList()) {
            // On récupère l'index actuel DISPONIBLE dans la classe
            int index = currentClass.getNumberOfMethods();
            
            // On passe cet index à la méthode
            EnvironmentExp envExp = method.verifyDeclMethodMembers(compiler, currentClass, index);
            try {
                envExpR.add(envExp);
                
                // --- MODIFICATION MINIMALE ICI ---
                // On récupère la définition qu'on vient de créer pour vérifier son index
                DeclMethod m = (DeclMethod) method;
                MethodDefinition def = envExp.get(m.getName().getName()).asMethodDefinition("error", m.getLocation());
                
                // Si l'index de la méthode est celui qu'on a proposé (index), 
                // c'est une nouvelle méthode : on incrémente le compteur global.
                if (def.getIndex() == index) {
                    currentClass.incNumberOfMethods();
                }
                // Si def.getIndex() est plus petit que index, c'est un override,
                // donc on ne touche pas au compteur de la classe !
                // ----------------------------------
                
            } catch (EnvironmentExp.DoubleDefException e) {
                // ...
            }
        }
        return envExpR;
    }
    // Pass 2 of [SyntaxeContextuelle] A revoir ...
    public void verifyListMethodBody(DecacCompiler compiler, EnvironmentExp envExp,
                                     ClassDefinition currentClass)
            throws ContextualError {
        for (AbstractDeclMethod method : getList()) {
            method.verifyMethodBody(compiler, envExp, currentClass);
        }
    }
    
    @Override
    public void decompile(IndentPrintStream s) {
        for (AbstractDeclMethod method : getList()) {
            method.decompile(s);
            s.println();
        }
    }
}