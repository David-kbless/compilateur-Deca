package fr.ensimag.ima.gencodeobjet;

import java.util.List;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.MethodDefinition;
import fr.ensimag.deca.tree.AbstractDeclClass;
import fr.ensimag.deca.tree.AbstractDeclMethod;
import fr.ensimag.deca.tree.AbstractDeclVar;
import fr.ensimag.deca.tree.DeclClass;
import fr.ensimag.deca.tree.DeclMethod;
import fr.ensimag.deca.tree.DeclVar;
import fr.ensimag.ima.pseudocode.InlinePortion;
import fr.ensimag.ima.pseudocode.Instruction;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.Line;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.ADDSP;
import fr.ensimag.ima.pseudocode.instructions.BOV;
import fr.ensimag.ima.pseudocode.instructions.BRA;
import fr.ensimag.ima.pseudocode.instructions.CMP;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.POP;
import fr.ensimag.ima.pseudocode.instructions.PUSH;
import fr.ensimag.ima.pseudocode.instructions.RTS;
import fr.ensimag.ima.pseudocode.instructions.SEQ;
import fr.ensimag.ima.pseudocode.instructions.TSTO;

public class genCodeBodyMethods {


    public genCodeBodyMethods(DecacCompiler compiler, List<AbstractDeclClass> classes){
        genCodeEqualsObject(compiler);
        genCodeClassBodyMethods(compiler, classes);
    }

    // on genere le code de la methode equals de la classe Object
    public static void genCodeEqualsObject(DecacCompiler compiler){
        // ajout du label avant tout:
        compiler.addLabel(new Label("code.Object.equals"));  
        compiler.addInstruction(new LOAD(new RegisterOffset(-2, Register.LB), Register.R0)); // chargement de this.
        compiler.addInstruction(new CMP(new RegisterOffset(-3, Register.LB), Register.R0)); // on compare this avec l'objet en parm (à -3(LB) du coup !)
        compiler.addInstruction(new SEQ(Register.R0));                      // puis on ret le res de la cmp
        compiler.addInstruction(new RTS());
    }

    public void genCodeClassBodyMethods(DecacCompiler compiler, List<AbstractDeclClass> classes){
        for(AbstractDeclClass abstractClass : classes){
            DeclClass classe = (DeclClass)abstractClass;
            genCodeBodyMethod(compiler, classe);
        }
    }


    private void genCodeBodyMethod(DecacCompiler compiler, DeclClass classe){
        
        // on parcours toutes les methodes de la classes 
        for(AbstractDeclMethod abstractMethod : classe.getMethods().getList()){
            // on reset la taille de la pile max utilisée pour chaque méthode
            compiler.resetTaillePileMaxUtilisee();

            DeclMethod method = (DeclMethod)abstractMethod;
            MethodDefinition methodDef = method.getName().getMethodDefinition();

            methodDef.setLabelDebut(new Label("code." + classe.getName().getName().getName() + "." + method.getName().getName().getName()));
            methodDef.setLabelFin(new Label("fin." + classe.getName().getName().getName() + "." + method.getName().getName().getName()));

            
            compiler.addLabel(methodDef.getLabelDebut());

            if (method.getAsmCode() != null) {
                // On retire les guillemets au début et à la fin de la String asm
                String rawAsm = method.getAsmCode().substring(1, method.getAsmCode().length() - 1);

                // On ecrit le code asm brut
                compiler.add(new InlinePortion(rawAsm)); 
                continue; // On passe à la méthode suivante, plus rien a faire après !! 
            }
            // on informe le compilateur du label qui sera ecrit plus tard : 
            compiler.setCurrentLabel(methodDef.getLabelFin());

            // on test la pile : 
            int max = method.getNbMaxRegUtilises();
            int nbLocalVar = method.getLocalVars().getList().size();

            int d1 = max - 1 + nbLocalVar;
            Line lineTsto = null;

            if(d1 > 0){
                lineTsto = new Line((Instruction) null);
                compiler.add(lineTsto);
                compiler.addInstruction(new BOV(new Label("stack_overflow_error")));
            }
            

            int localOffset = 1; 
            for (AbstractDeclVar abstractVar : method.getLocalVars().getList()) {
                DeclVar var = (DeclVar) abstractVar;
                // On dit que cette variable est à 1(LB), puis 2(LB), etc.
                var.getVarName().getExpDefinition().setOperand(new RegisterOffset(localOffset, Register.LB));
                localOffset++;
            }

            // IMPORTANT : on dois aussi réserver la place sur la pile !
            if (nbLocalVar > 0) {
                compiler.addInstruction(new ADDSP(nbLocalVar));
            }


            
            // on sauvegarde les registre utilisé
            for(int i = 2; i <= max ; i++){
                compiler.addInstruction(new PUSH(Register.getR(i)));
            }


            for (AbstractDeclVar abstractVar : method.getLocalVars().getList()) {
                DeclVar var = (DeclVar) abstractVar;
                var.codeGenDecl(compiler); // Génère le LOAD #val, R0 et STORE R0, n(LB)
            }


            // on genere ensuite le body
            // on s'assure d'avoir remis a zero le gestionnaire de registre pour eviter d'utiliser le tas
            compiler.resetGestionnaireRegistres(false);
            method.getBody().codeGenListInst(compiler);


            // gestion de l'overflow avec une methode retournant un truc mais pas tout le temps : 
            if(!method.isVoid()){
                compiler.addInstruction(new BRA(new Label("absence_return_error")));
                compiler.addAbsenceReturnLabel();
            }
            
            compiler.addLabel(methodDef.getLabelFin());

            // restaurations des registres pushés: 
            for(int i = max; i >= 2; i--){
                compiler.addInstruction(new POP(Register.getR(i)));
            }

            // liberation de l'espace des variables locales
            if (nbLocalVar > 0) {
                compiler.addInstruction(new fr.ensimag.ima.pseudocode.instructions.SUBSP(nbLocalVar));
            }

            if(d1 > 0){
                lineTsto.setInstruction(new TSTO(compiler.getTaillePileMaxUtilisee()));
            }



            compiler.addInstruction(new RTS());


        }
    }



}
