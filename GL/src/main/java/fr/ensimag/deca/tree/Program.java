package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.MethodDefinition;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.gencodeobjet.genCodeBodyMethods;
import fr.ensimag.ima.gencodeobjet.genCodeInitFields;
import fr.ensimag.ima.pseudocode.DAddr;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.LabelOperand;
import fr.ensimag.ima.pseudocode.NullOperand;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.*;
import java.io.PrintStream;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import fr.ensimag.deca.context.ClassDefinition;

/**
 * Deca complete program (class definition plus main block)
 *
 * @author gl54
 * @date 01/01/2026
 */
public class Program extends AbstractProgram {
    private static final Logger LOG = Logger.getLogger(Program.class);
    
    public Program(ListDeclClass classes, AbstractMain main) {
        Validate.notNull(classes);
        Validate.notNull(main);
        this.classes = classes;
        this.main = main;
    }
    public ListDeclClass getClasses() {
        return classes;
    }
    public AbstractMain getMain() {
        return main;
    }
    private ListDeclClass classes;
    private AbstractMain main;

    @Override
    public void verifyProgram(DecacCompiler compiler) throws ContextualError {
        LOG.debug("verify program: start");
        classes.verifyListClass(compiler); 
        classes.verifyListClassMembers(compiler);
        classes.verifyListClassBody(compiler);
        main.verifyMain(compiler);
        LOG.debug("verify program: end");
    }

    @Override
    public void codeGenProgram(DecacCompiler compiler) {
        // A FAIRE: compléter ce squelette très rudimentaire de code

        // 1ere passe du prog
        // on genere la vtable
        genCodeVTable(compiler);


        // pour que les NEW puissent fonctionner dans le main : 
        for (AbstractDeclClass abstractClass : classes.getList()) {
            DeclClass classe = (DeclClass) abstractClass;
            Label labelInit = new Label("init." + classe.getName().getName().getName());
            classe.getName().getClassDefinition().setLabelInit(labelInit);
        }

        main.codeGenMain(compiler);
        compiler.addInstruction(new HALT());
        compiler.addComment("end main program");

        
        compiler.addPrettyComment(compiler, "Generation des Inits et des body Methodes des classes");
        

        // partie bodymethod des classes 

        new genCodeInitFields(compiler, classes.getList());
        new genCodeBodyMethods(compiler, classes.getList());


        // ajout des labels d'erreurs demandées par le programme Deca
        compiler.addPrettyComment(compiler, "Ajout des labels erreurs demandées par le programme Deca");
        ajouteLabelErreurDemandee(compiler);

    }


    private void genCodeVTable(DecacCompiler compiler){
        ClassDefinition objectDef = (ClassDefinition) compiler.environmentType.defOfType(compiler.createSymbol("Object"));
    
        // 2. Générer la VTable d'Object d'abord
        DAddr addrObj = new RegisterOffset(compiler.getAndAddIndexGB(), Register.GB);
        objectDef.setVtableAddr(addrObj);
        
        

        compiler.addPrettyComment(compiler, "Construction des VTables des classes");
        compiler.addComment("Construction de la VTable de la classe Object");
        // vTable de l'objet Object qui est tjrs present meme sans aucune classe dans le prog.deca
        compiler.addInstruction(new LOAD(new NullOperand(), Register.R0),"    // On garde Dans R0 l'addr de la classe Object pour eviter de le loader a chaque fois !! ");  // on garde la valeur 0 dans R0 pendant tout le process
                                                                            // ceci nous permet d'eviter de multiple load 0, RO
        compiler.addInstruction(new STORE(Register.R0, addrObj));

        // on charge egalement l'adresse de la methode equal de objet juste après: 
        compiler.addInstruction(new LOAD(new LabelOperand(new Label("code.Object.equals")), Register.R1), "    // On garde Dans R1 le label 'code.Object.equals' pour la meme raison ");  // egalement, j'ai decider de garder la valeur du label equal 
                                                                                                                    // dans le registre R1 car il ya plusieur load de celui ci !
        compiler.addInstruction(new STORE(Register.R1, new RegisterOffset(compiler.getAndAddIndexGB(), Register.GB)));




        // on construit les autres tables des methodes des autres classes 
        for(AbstractDeclClass abstractClass : classes.getList()){
            

            // @ousmane :  je fais ça pour avoir acces au classes getname et getdefinition : 
            DeclClass classe = (DeclClass)abstractClass;

            ClassDefinition defClasse = (ClassDefinition)classe.getName().getDefinition();

            // commentaire que j'ai  @Ousmane fait pour mieux debugger les .ass
            compiler.addComment("Construction de la VTable de " + classe.getName().getName());
            //compiler.addPrettyComment(compiler, "Construction de la VTable de " + classe.getName().getName());

            // on creer une nouvelle entree pour la classe actuelle :
            DAddr classeAddr = new RegisterOffset(compiler.getAndAddIndexGB(), Register.GB);
            // on enregistre l'addr pour garder en Mem afin de generer bien les instructions lié a cette clss 
            defClasse.setVtableAddr(classeAddr);

            // stockage de l'adresse de la mere en en premiere case : 
            DAddr addressParent = defClasse.getSuperClass().getVTableAddr();
            if (addressParent == null) {
                    // Si c'est encore null, c'est que l'ordre est mauvais ou superClass est mal mis.
                    // On force l'adresse d'Object si superClass est null 
                    addressParent = objectDef.getVTableAddr();
                }
            
            GPRegister reg = Register.getR(2);
            compiler.addInstruction(new LEA(addressParent, reg));
            compiler.addInstruction(new STORE(reg, classeAddr));
            

            for (MethodDefinition meth : defClasse.getMethodTable().values()) {
                // Si c'est equals, on utilise R1 (déjà chargé au début du programme)
                if (meth.getLabel().toString().equals("code.Object.equals")) {
                    compiler.addInstruction(new STORE(Register.R1, new RegisterOffset(compiler.getAndAddIndexGB(), Register.GB)));
                } else {
                    // Sinon on charge l'étiquette spécifique
                    compiler.addInstruction(new LOAD(new LabelOperand(meth.getLabel()), reg));
                    compiler.addInstruction(new STORE(reg, new RegisterOffset(compiler.getAndAddIndexGB(), Register.GB)));
                }
            }
        
        }

    }


    private void ajouteLabelErreurDemandee(DecacCompiler compiler){

        // definition des content des labels d'erreurs d'overflow du stack : 
        Label StackOverflowErr = new Label("stack_overflow_error");
        compiler.addLabel(StackOverflowErr);
        compiler.addInstruction(new WSTR("Error: Stack Overflow"));
        compiler.addInstruction(new WNL());
        compiler.addInstruction(new ERROR());


        // ajout des labels si le programme l'exige, c'est lui le bossssss :
        if(compiler.getOverflowLabel()){
            compiler.addLabel(new Label("overflow_error"));
            compiler.addInstruction(new WSTR("Error: Overflow sur Float"));
            compiler.addInstruction(new WNL());
            compiler.addInstruction(new ERROR());
        }

        if(compiler.getDivisionByZeroLabel()){
            compiler.addLabel(new Label("div_zero_error"));
            compiler.addInstruction(new WSTR("Erreur : division par 0"));
            compiler.addInstruction(new WNL());
            compiler.addInstruction(new ERROR());
        }

        if(compiler.getTasOverflowLabel()){
            compiler.addLabel(new Label("tas_plein"));
            compiler.addInstruction(new WSTR("Erreur : le tas est plein, impossible d'allouer de la memoire"));
            compiler.addInstruction(new WNL());
            compiler.addInstruction(new ERROR());
        }

       if(compiler.getAbsenceReturnLabel()){
            compiler.addLabel(new Label("absence_return_error"));
            compiler.addInstruction(new WSTR("Erreur : Absence de return dans le corps d'une methode "));
            compiler.addInstruction(new WNL());
            compiler.addInstruction(new ERROR());
        }

        if(compiler.getConversionImpossibleLabel()){
            compiler.addLabel(new Label("conversion_impossible_error"));
            compiler.addInstruction(new WSTR("Erreur : Conversion impossible"));
            compiler.addInstruction(new WNL());
            compiler.addInstruction(new ERROR());
        }

        if(compiler.getDereferencementNullLabel()){
            compiler.addLabel(new Label("dereferencement.null"));
            compiler.addInstruction(new WSTR("Erreur : Dereferencement null"));
            compiler.addInstruction(new WNL());
            compiler.addInstruction(new ERROR());
        }

        if(compiler.getIOErreurLabel()){
            compiler.addLabel(new Label("io_error"));
            compiler.addInstruction(new WSTR("Erreur: D'entree incompatible avec la variable "));
            compiler.addInstruction(new WNL());
            compiler.addInstruction(new ERROR());
        }


        if(compiler.getAccesVarNonInitLabel()){
            compiler.addLabel(new Label("acces_var_non_init"));
            compiler.addInstruction(new WSTR("Erreur : Accès à une variable non initialisée"));
            compiler.addInstruction(new WNL());
            compiler.addInstruction(new ERROR());
        }


        if(compiler.getMethodeIncompatibleLabel()){
            compiler.addLabel(new Label("methode_incompatible"));
            compiler.addInstruction(new WSTR("Erreur : Methode incompatible"));
            compiler.addInstruction(new WNL());
            compiler.addInstruction(new ERROR());
        }
    }

    @Override
    public void decompile(IndentPrintStream s) {
        getClasses().decompile(s);
        getMain().decompile(s);
    }
    
    @Override
    protected void iterChildren(TreeFunction f) {
        classes.iter(f);
        main.iter(f);
    }
    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        classes.prettyPrint(s, prefix, false);
        main.prettyPrint(s, prefix, true);
    }

    
}
