package fr.ensimag.deca;

import java.io.File;
import org.apache.log4j.Logger;

/**
 * Main class for the command-line Deca compiler.
 *
 * @author gl54
 * @date 01/01/2026
 */
public class DecacMain {
    private static Logger LOG = Logger.getLogger(DecacMain.class);
    
    public static void main(String[] args) {
        // example log4j message.
        LOG.info("Decac compiler started");
        boolean error = false;
        final CompilerOptions options = new CompilerOptions();
        try {
            options.parseArgs(args);
            

        } catch (CLIException e) {
            System.err.println("Error during option parsing:\n"
                    + e.getMessage());
            options.displayUsage();
            System.exit(1);
        }
        if (options.getPrintBanner()) {


            System.out.println("============================================================\n" + //
                                "                     DECA COMPILER  -  GROUPE gl54\n" + //
                                "============================================================\n" + //
                                "   Projet Compilateur Deca (ANTLR4) — ENSIMAG\n" + //
                                "\n" + //
                                "   Membres :\n" + //
                                "     - Yao David Kossi\n" + //
                                "     - Yassine Fliss\n" + //
                                "     - Safwane Oudrhiri Idrissi\n" + //
                                "     - Alpha Ousmane Diakite\n" + //
                                "     - Nathan Dreuille \n" + //
                               
                                "============================================================\n" + //
                                 "");

            return;
        }
        if (options.getParallel()) {
            // on creer un pool de threads pour parralleliser
            int nbProcesseurs = Runtime.getRuntime().availableProcessors();
            java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newFixedThreadPool(nbProcesseurs);
            
            // pour pouvoir suivre et faire les join à la fin, on cr"er cette liste
            java.util.List<java.util.concurrent.Future<Boolean>> results = new java.util.ArrayList<>();

            for (java.io.File source : options.getSourceFiles()) {
                // on creer une tache pour chaque fichier
                java.util.concurrent.Callable<Boolean> task = () -> {
                    // /Erreur corrige par @Ousmane, on creer un compilateur pour chaque fichier 
                    DecacCompiler compiler = new DecacCompiler(options, source);
                    return compiler.compile();
                };
                // On lance la tache dans le pool
                results.add(executor.submit(task));
            }

            // On attend que tout le monde ait fini et on vérifie les erreurs
            boolean erreur = false;
            for (java.util.concurrent.Future<Boolean> future : results) {
                try {
                    // astuce de blocage
                    if (future.get()) erreur = true; 
                } catch (java.util.concurrent.ExecutionException | InterruptedException e) {
                    erreur = true;
                    System.err.println("Erreur fatale lors de la compilation parallèle : " + e.getMessage());
                }
            }

            // cloture de la pool pour lib les ressources usées
            executor.shutdown();
            if (erreur) System.exit(1);

        } else {
            for (File source : options.getSourceFiles()) {
                DecacCompiler compiler = new DecacCompiler(options, source);
                if (compiler.compile()) {
                    error = true;
                }
            }
        }
        System.exit(error ? 1 : 0);
    }
}
