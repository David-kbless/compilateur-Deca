package fr.ensimag.deca;

import fr.ensimag.deca.codegen.GestionnaireLabel;
import fr.ensimag.deca.codegen.GestionnairePile;
import fr.ensimag.deca.codegen.GestionnaireRegistre;
import fr.ensimag.deca.context.EnvironmentType;
import fr.ensimag.deca.syntax.DecaLexer;
import fr.ensimag.deca.syntax.DecaParser;
import fr.ensimag.deca.tools.DecacInternalError;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tools.SymbolTable;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.deca.tree.AbstractProgram;
import fr.ensimag.deca.tree.LocationException;
import fr.ensimag.ima.pseudocode.AbstractLine;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.IMAProgram;
import fr.ensimag.ima.pseudocode.ImmediateInteger;
import fr.ensimag.ima.pseudocode.Instruction;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.Operand;
import fr.ensimag.ima.pseudocode.instructions.ADDSP;
import fr.ensimag.ima.pseudocode.instructions.BSR;
import fr.ensimag.ima.pseudocode.instructions.POP;
import fr.ensimag.ima.pseudocode.instructions.PUSH;
import fr.ensimag.ima.pseudocode.instructions.SUBSP;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.apache.log4j.Logger;

/**
 * Decac compiler instance.
 *
 * This class is to be instantiated once per source file to be compiled. It
 * contains the meta-data used for compiling (source file name, compilation
 * options) and the necessary utilities for compilation (symbol tables, abstract
 * representation of target file, ...).
 *
 * It contains several objects specialized for different tasks. Delegate methods
 * are used to simplify the code of the caller (e.g. call
 * compiler.addInstruction() instead of compiler.getProgram().addInstruction()).
 *
 * @author gl54
 * @date 01/01/2026
 */
public class DecacCompiler {

    // gestion de la creation unique des label d'eerreur (overflow, ....) au besoin
    private boolean overflowAdded = false;
    private boolean divisionByZeroAdded = false;
    private boolean absenceReturnAdded = false;
    private boolean conversionImpossibleAdded = false;
    private boolean dereferencementNullAdded = false;
    private boolean ioErreurAdded = false;
    private boolean accesVarNonInitAdded = false;
    private boolean methodeIncompatibleAdded = false;
    private boolean tasOverflowAdded = false;


    public boolean getTasOverflowLabel() {
        return tasOverflowAdded;
    }

    public void addTasOverflowLabel() {
        tasOverflowAdded = true;
    }


    public boolean getOverflowLabel() {
        return overflowAdded;
    }

    public boolean getDivisionByZeroLabel() {
        return divisionByZeroAdded;
    }

    public boolean getAbsenceReturnLabel() {
        return absenceReturnAdded;
    }


    public boolean getConversionImpossibleLabel() {
        return conversionImpossibleAdded;
    }

    public boolean getDereferencementNullLabel() {
        return dereferencementNullAdded;
    }

    public boolean getIOErreurLabel() {
        return ioErreurAdded;
    }

    // public boolean getStackOverflowLabel() {
    //     return stack_overflow_errorAdded;
    // }

    public boolean getAccesVarNonInitLabel() {
        return accesVarNonInitAdded;
    }

    public boolean getMethodeIncompatibleLabel() {
        return methodeIncompatibleAdded;
    }



    public void addOverflowLabel() {
        overflowAdded = true;
    }
    public void addDivisionByZeroLabel() {
        divisionByZeroAdded = true;
    }

    public void addAbsenceReturnLabel() {
        absenceReturnAdded = true;
    }

    public void addConversionImpossibleLabel() {
        conversionImpossibleAdded = true;
    }

    public void addDereferencementNullLabel() {
        dereferencementNullAdded = true;
    }


    public void addIOErreurLabel() {
        ioErreurAdded = true;
    }

    // public void addStackOverflowLabel() {
    //     stack_overflow_errorAdded = true;
    // }

        
    // public void addAccesVarNonInitLabel() {
    //     accesVarNonInitAdded = true;
    // }


    // public void addMethodeIncompatibleLabel() {
    //     methodeIncompatibleAdded = true;
    // }

    


    

    // gestion des compteur de labels en fonction de sa nature : 
    // private int compteurLabelIF = 1;
    // private int compteurOpBool = 1;


    public Label getLabelOpBool() {
        return new Label("E_Fin." + compteurOpBool);
    }

    public void addLabelOpBool() {
        compteurOpBool++;
    }


    public void incCOmpteurLabelIF() {
        compteurLabelIF++;
    }

    public int getCompteurLabelIF() {
        return compteurLabelIF;
    }

    public void addLabelIF() {
        compteurLabelIF++;
    }



    // Dans DecacCompiler.java
    private int compteurOpBool = 1;
    private int compteurLabelIF = 1;

    // Donne un numéro unique et l'incrémente immédiatement
    public int getAndIncOpBool() {
        return compteurOpBool++;
    }

    public int getAndIncIf() {
        return compteurLabelIF++;
    }



    


    private static final Logger LOG = Logger.getLogger(DecacCompiler.class);
    
    /**
     * Portable newline character.
     */
    private static final String nl = System.getProperty("line.separator", "\n");

    private final CompilerOptions compilerOptions;
    private final GestionnaireRegistre gestionnaireRegistre = new GestionnaireRegistre();
    private final GestionnairePile gestionnairePile = new GestionnairePile();


    public void resetTaillePileMaxUtilisee() {
        gestionnairePile.reset();
    }

    public int getTaillePileMaxUtilisee() {
        return gestionnairePile.getMax();
    }


    

    public GPRegister utiliserRegistre() {
        return gestionnaireRegistre.utiliserRegistre(this);
    }

    public void libererRegistre() {
        gestionnaireRegistre.libererRegistre(this);
    }

    public int simulerAllocationRegistre(){
        return gestionnaireRegistre.simulerAllocationRegistre();
    }
    public void simulerLiberationRegistre(){
        gestionnaireRegistre.simulerLiberationRegistre();
    }

    public int getCompteurRegistre(){
        return gestionnaireRegistre.getCompteurRegistre();
    }

    public void setCompteurRegistre(int compteurRegistre){
        gestionnaireRegistre.setCompteurRegistre(compteurRegistre);
    }

    public int getNbRegStokes(){
        return gestionnaireRegistre.getNbRegStokes();
    }

    public void setNbRegStokes(int nbRegStokes){
        gestionnaireRegistre.setNbRegStokes(nbRegStokes);
    }

    public void commencerSimulationRegistres(boolean reset){
        gestionnaireRegistre.commencerSimulationRegistre(reset);
    }

    public void resetGestionnaireRegistres(boolean reset){
        gestionnaireRegistre.resetGestionnaireRegistre(reset);
    }

    private final GestionnaireLabel gestionnaireLabel = new GestionnaireLabel();

    public int getCompteurLabelWhile(){
        return gestionnaireLabel.getCompteurLabelWhile();
    }

    public int incrementerCompteurLabelWhile(){
        return gestionnaireLabel.incrementerCompteurLabelWhile();
    }


    public DecacCompiler(CompilerOptions compilerOptions, File source) {
        super();
        this.compilerOptions = compilerOptions;
        this.source = source;
        int rMax;
        if (compilerOptions != null) {
            rMax = compilerOptions.getNRegisters();
        } else {
            rMax = 16; // Valeur par défaut pour les tests
        }
        this.gestionnaireRegistre.setRMax(rMax);
        environmentType = new EnvironmentType(this);
    }

    /**
     * Source file associated with this compiler instance.
     */
    public File getSource() {
        return source;
    }

    /**
     * Compilation options (e.g. when to stop compilation, number of registers
     * to use, ...).
     */
    public CompilerOptions getCompilerOptions() {
        return compilerOptions;
    }

    /**
     * @see
     * fr.ensimag.ima.pseudocode.IMAProgram#add(fr.ensimag.ima.pseudocode.AbstractLine)
     */
    public void add(AbstractLine line) {
        program.add(line);
    }

    /**
     * @see fr.ensimag.ima.pseudocode.IMAProgram#addComment(java.lang.String)
     */
    public void addComment(String comment) {
        program.addComment(comment);
    }

    /**
     * @see
     * fr.ensimag.ima.pseudocode.IMAProgram#addLabel(fr.ensimag.ima.pseudocode.Label)
     */
    public void addLabel(Label label) {
        program.addLabel(label);
    }

    /**
     * @see
     * fr.ensimag.ima.pseudocode.IMAProgram#addInstruction(fr.ensimag.ima.pseudocode.Instruction)
     */
    public void addInstruction(Instruction instruction) {
        program.addInstruction(instruction);
        // Mise à jour automatique de la pile
        if (instruction instanceof PUSH) {
            gestionnairePile.instructionPush();
        } else if (instruction instanceof POP) {
            gestionnairePile.instructionPop();
        } else if (instruction instanceof ADDSP) {
            // Extraction de la valeur immédiate
            Operand op = ((ADDSP) instruction).getOperand();
            if (op instanceof ImmediateInteger) {
                int val = ((ImmediateInteger) op).getValue();
                gestionnairePile.instructionAddsp(val);
            }
        } 
        else if (instruction instanceof SUBSP) {
            Operand op = ((SUBSP) instruction).getOperand();
            if (op instanceof ImmediateInteger) {
                int val = ((ImmediateInteger) op).getValue();
                gestionnairePile.instructionSubsp(val);
            }
        }else if (instruction instanceof BSR) {
            gestionnairePile.instructionBsr();
        }
    }

    /**
     * @see
     * fr.ensimag.ima.pseudocode.IMAProgram#addInstruction(fr.ensimag.ima.pseudocode.Instruction)
     */
    public void addFirstComment(String comment) {
        program.addFirstComment(comment);;
    }

    /**
     * @see
     * fr.ensimag.ima.pseudocode.IMAProgram#addFirstInstruction(fr.ensimag.ima.pseudocode.Instruction)
     */
    public void addFirstInstruction(Instruction instruction) {
        program.addFirst(instruction);
    }


    /**
     * @see
     * fr.ensimag.ima.pseudocode.IMAProgram#addInstruction(fr.ensimag.ima.pseudocode.Instruction,
     * java.lang.String)
     */
    public void addInstruction(Instruction instruction, String comment) {
        program.addInstruction(instruction, comment);
    }
    
    /**
     * @see 
     * fr.ensimag.ima.pseudocode.IMAProgram#display()
     */
    public String displayIMAProgram() {
        return program.display();
    }
    
    private final File source;
    /**
     * The main program. Every instruction generated will eventually end up here.
     */
    private final IMAProgram program = new IMAProgram();
 

    public final IMAProgram getProgram() {
        return program;
    }

    /** The global environment for types (and the symbolTable) */
    public final SymbolTable symbolTable = new SymbolTable();
    public final EnvironmentType environmentType;
    

    public Symbol createSymbol(String name) {
        return symbolTable.create(name);
    }

    /**
     * Run the compiler (parse source file, generate code)
     *
     * @return true on error
     */
    public boolean compile() {
        String sourceFile = source.getAbsolutePath();
        String destFile = null;






        // A FAIRE: calculer le nom du fichier .ass à partir du nom du
        // A FAIRE: fichier .deca.

        // ------------------------------------------------------------------------------------------------------------
        if (sourceFile.endsWith(".deca")) {
            // on ajoute l'extension .ass la place de .deca dns le nom du fichier a generer
            destFile = sourceFile.substring(0, sourceFile.length() - ".deca".length()) + ".ass";
        } else {
            // throw new IllegalArgumentException(" le fichier source ne se termine pas par .deca, entree incorrecte ...");
            throw new DecacInternalError("Erreur: le fichier source ne se termine pas par .deca");
        }
        // ------------------------------------------------------------------------------------------------------------






        PrintStream err = System.err;
        PrintStream out = System.out;
        LOG.debug("Compiling file " + sourceFile + " to assembly file " + destFile);
        try {
            return doCompile(sourceFile, destFile, out, err);
        } catch (LocationException e) {
            e.display(err);
            return true;
        } catch (DecacFatalError e) {
            err.println(e.getMessage());
            return true;
        } catch (StackOverflowError e) {
            LOG.debug("stack overflow", e);
            err.println("Stack overflow while compiling file " + sourceFile + ".");
            return true;
        } catch (Exception e) {
            LOG.fatal("Exception raised while compiling file " + sourceFile
                    + ":", e);
            err.println("Internal compiler error while compiling file " + sourceFile + ", sorry.");
            return true;
        } catch (AssertionError e) {
            LOG.fatal("Assertion failed while compiling file " + sourceFile
                    + ":", e);
            err.println("Internal compiler error while compiling file " + sourceFile + ", sorry.");
            return true;
        }
    }

    /**
     * Internal function that does the job of compiling (i.e. calling lexer,
     * verification and code generation).
     *
     * @param sourceName name of the source (deca) file
     * @param destName name of the destination (assembly) file
     * @param out stream to use for standard output (output of decac -p)
     * @param err stream to use to display compilation errors
     *
     * @return true on error
     */
    private boolean doCompile(String sourceName, String destName,
            PrintStream out, PrintStream err)
            throws DecacFatalError, LocationException {
        AbstractProgram prog = doLexingAndParsing(sourceName, err);
        
        if (prog == null) {
            LOG.info("Parsing failed");
            return true;
        }
        assert(prog.checkAllLocations());


        // on arrete le programe dans le cas ou il ya l'option -p 
        if(getCompilerOptions().getParse()){
            IndentPrintStream ips = new IndentPrintStream(out);
            prog.decompile(ips);  // on affiche avant tout l'arbre de decompile
            System.exit(0);
        }


        prog.verifyProgram(this);
        assert(prog.checkAllDecorations());

        // si l'option -v est active alors : ....
        if(getCompilerOptions().getVerify()){
            // on sort sans afficher !
            System.exit(0);
        }

        prog.codeGenProgram(this);
        //addComment("end main program");
        LOG.debug("Generated assembly code:" + nl + program.display());
        LOG.info("Output file assembly file is: " + destName);

        FileOutputStream fstream = null;
        try {
            fstream = new FileOutputStream(destName);
        } catch (FileNotFoundException e) {
            throw new DecacFatalError("Failed to open output file: " + e.getLocalizedMessage());
        }

        LOG.info("Writing assembler file ...");

        program.display(new PrintStream(fstream));
        LOG.info("Compilation of " + sourceName + " successful.");
        return false;
    }

    /**
     * Build and call the lexer and parser to build the primitive abstract
     * syntax tree.
     *
     * @param sourceName Name of the file to parse
     * @param err Stream to send error messages to
     * @return the abstract syntax tree
     * @throws DecacFatalError When an error prevented opening the source file
     * @throws DecacInternalError When an inconsistency was detected in the
     * compiler.
     * @throws LocationException When a compilation error (incorrect program)
     * occurs.
     */
    protected AbstractProgram doLexingAndParsing(String sourceName, PrintStream err)
            throws DecacFatalError, DecacInternalError {
        DecaLexer lex;
        try {
            lex = new DecaLexer(CharStreams.fromFileName(sourceName));
        } catch (IOException ex) {
            throw new DecacFatalError("Failed to open input file: " + ex.getLocalizedMessage());
        }
        lex.setDecacCompiler(this);
        CommonTokenStream tokens = new CommonTokenStream(lex);
        DecaParser parser = new DecaParser(tokens);
        parser.setDecacCompiler(this);
        return parser.parseProgramAndManageErrors(err);
    }


    
    private int indexGB = 1;

    public int getIndexGB(){
        return indexGB;
    }


    public int getAndAddIndexGB(){
        return indexGB++;
    }

    private Label currentLabel;

    public Label getCurrentLabel() {
        return currentLabel;
    }

    public void setCurrentLabel(Label currentLabel) {
        this.currentLabel = currentLabel;
    }

    protected int autrePush = 0;

    public int getAutrePush() {
        return autrePush;
    }

    public void setAutrePush(int autrePush) {
        if(autrePush > this.autrePush){
            this.autrePush = autrePush;
        }
    }

    // label pour les casts dynamiques
    private int labelCastCounter = 1;

    public int getLabelCastId() {
        return labelCastCounter++;
    }




    public void addPrettyComment(DecacCompiler compiler, String message) {
        int width = 60; // Largeur totale du bloc
        String border = " " + "*".repeat(width);
        
        compiler.addComment(border);
        
        // On calcule l'espace nécessaire pour centrer le texte
        int contentWidth = width - 4; // On retire "; * " et " *"
        int paddingTotal = contentWidth - message.length();
        int paddingLeft = paddingTotal / 2;
        int paddingRight = paddingTotal - paddingLeft;

        String middleLine = " * " + " ".repeat(paddingLeft) + message.toUpperCase() 
                            + " ".repeat(paddingRight) + " *";
        
        compiler.addComment(middleLine);
        compiler.addComment(border);
    }

}
