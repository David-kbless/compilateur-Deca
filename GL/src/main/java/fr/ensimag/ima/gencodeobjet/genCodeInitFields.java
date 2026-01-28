package fr.ensimag.ima.gencodeobjet;

import fr.ensimag.deca.context.ClassDefinition;
import java.util.List;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.FieldDefinition;
import fr.ensimag.deca.tree.AbstractDeclClass;
import fr.ensimag.deca.tree.AbstractDeclField;
import fr.ensimag.deca.tree.AbstractIdentifier;
import fr.ensimag.deca.tree.DeclClass;
import fr.ensimag.deca.tree.DeclField;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.ImmediateFloat;
import fr.ensimag.ima.pseudocode.Instruction;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.Line;
import fr.ensimag.ima.pseudocode.NullOperand;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.BOV;
import fr.ensimag.ima.pseudocode.instructions.BSR;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.POP;
import fr.ensimag.ima.pseudocode.instructions.PUSH;
import fr.ensimag.ima.pseudocode.instructions.RTS;
import fr.ensimag.ima.pseudocode.instructions.STORE;
import fr.ensimag.ima.pseudocode.instructions.TSTO;

public class genCodeInitFields{

    /**
     * Initialisation s'il s'agit des champs d'une classe
     * @param compiler
     * @param classes
     */
    public genCodeInitFields(DecacCompiler compiler, List<AbstractDeclClass> classes){
        //compiler.addComment( "On a choisi de mettre l'objet dans R2 Pour toutes les initialisations");
        for(AbstractDeclClass abstractClass : classes){
            DeclClass classe = (DeclClass)abstractClass;
            initFields(compiler, classe);
        }
    }
    

    private void initFields(DecacCompiler compiler, DeclClass classe){
        
        compiler.resetTaillePileMaxUtilisee();

        Label labelInit = new Label("init." + classe.getName().getName().getName());

        classe.getName().getClassDefinition().setLabelInit(labelInit);
        compiler.addLabel(labelInit);

            // on test la pile 
            ClassDefinition classDef = classe.getName().getClassDefinition();
            Line lineTsto = null;

            if(classDef.getNumberOfFields() > 0){
                lineTsto = new Line((Instruction) null);
                compiler.add(lineTsto);
                // compiler.addInstruction(new TSTO(classe.getName().getClassDefinition().getNumberOfFields()));
                compiler.addInstruction(new BOV(new Label("stack_overflow_error")));
            }


            //Sauvegarde du contexte (R2, R3...)
            int nbRegASauvegarder = classDef.getNbNbRegEstimee(); 
            if(nbRegASauvegarder > 0){
                //compiler.addInstruction(new TSTO(nbRegASauvegarder));
                compiler.setAutrePush(nbRegASauvegarder);
                for(int i = 2; i < 2 + nbRegASauvegarder; i++){
                    compiler.addInstruction(new PUSH(Register.getR(i)));
                }
            }


            // on initialise à 0 dabord toutes les vars de la classe fille 
            GPRegister objet = compiler.utiliserRegistre();
            GPRegister nullReg = compiler.utiliserRegistre();
            compiler.addInstruction(new LOAD(new RegisterOffset(-2, Register.LB), objet));
            boolean nullChargee = false;
            boolean zeroRegChargee = false;
            boolean zeroFloatChargee = false;

            
            for(AbstractDeclField abstractField : classe.getFields().getList()){
                FieldDefinition fieldDef = ((DeclField)abstractField).getFieldName().getFieldDefinition();
                // selon le type de field
                Type type = fieldDef.getType();
                RegisterOffset fieldOffsetRegistre = new RegisterOffset(fieldDef.getIndex() + 1, objet);


                // on stocke 0 (pour int et bool) ou 0.0f (pour float) ici :
                // petite optim, on ne charge q'une fois ces valeurs !
                if(type.isInt() || type.isBoolean()){
                    if(!zeroRegChargee){
                        compiler.addInstruction(new LOAD(0, Register.R0));
                        zeroRegChargee = true;
                    }
                    compiler.addInstruction(new STORE(Register.R0, fieldOffsetRegistre));
                }else if(type.isFloat()){
                    if(!zeroFloatChargee){
                        compiler.addInstruction(new LOAD(new ImmediateFloat(0.0f), Register.R1));
                        zeroFloatChargee = true;
                    }
                    compiler.addInstruction(new STORE(Register.R1, fieldOffsetRegistre));
                }else{
                    if(!nullChargee){
                        compiler.addInstruction(new LOAD(new NullOperand(), nullReg));
                        nullChargee = true;
                    }
                    compiler.addInstruction(new STORE(nullReg, fieldOffsetRegistre));
                }
                        
            }

            // liberation des registres utiliser pour le nullreg
            compiler.libererRegistre(); // nullReg




            // s'il la classe a une mere alors on appelle son init
            AbstractIdentifier superclass = classe.getSuperClass();
            if(superclass != null){
                compiler.addInstruction(new PUSH(objet));
                compiler.addInstruction(new BSR(superclass.getClassDefinition().getLabelInit()));
                compiler.addInstruction(new POP(objet));
            }

            // on restaure le gestionnaire afin d'optimiser l'utilisation des registres
            compiler.resetGestionnaireRegistres(false);
            objet =compiler.utiliserRegistre();
            compiler.addInstruction(new LOAD(new RegisterOffset(-2, Register.LB), objet));


            // et enfin on initialise les champs de la classe fille qui sont explicitement initialisées
            for(AbstractDeclField abstractField : classe.getFields().getList()){
                DeclField field = (DeclField)abstractField;

                DVal expr = field.getInitialization().codeGen(compiler);

                

                // s'il ya bien une initialisation, alors elle ecrase ce qui est deja dans le champs de ce objet
                if(expr != null){
                    // on gere le fait que  reg peut etre un immediat :
                    if(expr instanceof GPRegister == false){
                        compiler.addInstruction(new LOAD(expr, Register.R0));
                        expr = Register.R0;
                    }
                    compiler.addInstruction(new STORE((GPRegister)expr, new RegisterOffset(field.getFieldName().getFieldDefinition().getIndex() + 1, objet)));

                    // on libere le registre utiliser pour. expr dans ce cas
                    compiler.libererRegistre();
                }

            }

            // on libere le registre utiliser pour l'objet
            compiler.libererRegistre();
        

            // on restaure le contexte (R2, R3...)// Restauration du contexte
            if(nbRegASauvegarder > 0){
                for(int i = 1 + nbRegASauvegarder; i >= 2; i--){
                    
                    compiler.addInstruction(new POP(Register.getR(i)));
                } 
            }

            // on set la taille max de la pile utilisée
            if(classDef.getNumberOfFields() > 0){
                lineTsto.setInstruction(new TSTO(compiler.getTaillePileMaxUtilisee()));
            }

        compiler.addInstruction(new RTS());
    }


}
