package fr.ensimag.deca;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * User-specified options influencing the compilation.
 *
 * @author gl54
 * @date 01/01/2026
 */
public class CompilerOptions {
    public static final int QUIET = 0;
    public static final int INFO  = 1;
    public static final int DEBUG = 2;
    public static final int TRACE = 3;
    public int getDebug() {
        return debug;
    }

    public boolean getParallel() {
        return parallel;
    }

    public boolean getPrintBanner() {
        return printBanner;
    }
    
    public List<File> getSourceFiles() {
        return Collections.unmodifiableList(sourceFiles);
    }

    private int debug = 0;
    private boolean parallel = false;
    private boolean printBanner = false;
    private boolean parse = false;
    private boolean verify = false;
    private boolean pasCheck = false;
    private int nbRegs = 16;
    private List<File> sourceFiles = new ArrayList<File>();

    
    public void parseArgs(String[] args) throws CLIException {
        // A FAIRE : parcourir args pour positionner les options correctement. -------------------------------------------------
        int i = 0;
        while (i < args.length) {
            String arg = args[i];

            if (arg.equals("-b")) {
                this.printBanner = true;
                // j'affiche la bannniere que dans le main,
            } else if (arg.equals("-p")) {
                this.parse = true;
            } else if (arg.equals("-v")) {
                this.verify = true;
            } else if (arg.equals("-n")) {
                this.pasCheck = true;
            }else if (arg.equals("-P")) {
                this.parallel = true;
            }else if (arg.equals("-r")) {
                i++; // On avance pour lire le nombre
                if (i >= args.length) {
                    throw new CLIException("L'option -r attend un nombre de registres.");
                }
                try {
                    int r = Integer.parseInt(args[i]);
                    if (r < 4 || r > 16) {
                        throw new CLIException("Nombre de registres invalide (doit être entre 4 et 16).");
                    }
                    this.nbRegs = r;
                } catch (NumberFormatException e) {
                    throw new CLIException("L'option -r attend un entier.");
                }
            } else if (arg.equals("-d")) {
                this.debug++;
            } else if (arg.startsWith("-")) {
                // Option inconnue
                throw new CLIException("Option inconnue : " + arg);
            } else {
                // C'est un fichier .deca
                sourceFiles.add(new File(arg));
            }
            i++;
        }
        
        // A FAIRE : parcourir args pour positionner les options correctement. -------------------------------------------------

        // Ajoute les autres options ici...
    
        // exculsion mutuelle entre -p et -v
        if (this.parse && this.verify) {
            throw new CLIException("Options -p et -v sont incompatible.");
        }

        if (sourceFiles.isEmpty() && !printBanner) {
            System.out.println("Syntaxe de compilation : \ndecac [[-p | -v] [-n] [-r X] [-d]* [-P] [-w] <fichier deca>...] | [-b]");
            // System.exit(0);
            return;
        }

        Logger logger = Logger.getRootLogger();
        // map command-line debug option to log4j's level.
        switch (getDebug()) {
        case QUIET: break; // keep default
        case INFO:
            logger.setLevel(Level.INFO); break;
        case DEBUG:
            logger.setLevel(Level.DEBUG); break;
        case TRACE:
            logger.setLevel(Level.TRACE); break;
        default:
            logger.setLevel(Level.ALL); break;
        }
        logger.info("Application-wide trace level set to " + logger.getLevel());

        boolean assertsEnabled = false;
        assert assertsEnabled = true; // Intentional side effect!!!
        if (assertsEnabled) {
            logger.info("Java assertions enabled");
        } else {
            logger.info("Java assertions disabled");
        }

    }

    protected void displayUsage() {
        System.out.println("Syntaxe de compilation : \ndecac [[-p | -v] [-n] [-r X] [-d]* [-P] [-w] <fichier deca>...] | [-b]");
        
    }

    public boolean getNoCheck() {
        return pasCheck;
    }

    public int getNRegisters() {
        return nbRegs;
    }

    public boolean getParse() {
        return parse;
    }

    public boolean getVerify() {
        return verify;
    }
}
