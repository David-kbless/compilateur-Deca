package fr.ensimag.deca.codegen;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.PUSH;

/**
 * Gestionnaire des registres
 * 
 * @author gl54
 * @date 01/01/2026
 */

public class GestionnaireRegistre {
    // gestion des utilisation des registres : 
    private int compteurRegistre = 2;
    private int nbRegStokes = 0;
    private int rMax;


    public GestionnaireRegistre(){
    }

    public void setRMax(int rMax){
        this.rMax = rMax;
    }


    
    public GPRegister utiliserRegistre(DecacCompiler compiler) {

        if(compteurRegistre < rMax){
            return Register.getR(compteurRegistre++);
        } else {
            nbRegStokes++;
            // on push le dernier registre dans la pile et on libere le Rmax
            compiler.addInstruction(new PUSH(Register.getR(rMax - 1)));
            return Register.getR(rMax - 1);
        }
        
    }

    public void libererRegistre(DecacCompiler compiler) {

        // s'il ya des registre dans la pile
        if(nbRegStokes > 0){
            nbRegStokes--;
        }else if(compteurRegistre > 2){
            compteurRegistre--;
        }
    }

    public int simulerAllocationRegistre(){
        if(compteurRegistre < rMax){
            return compteurRegistre++;
        } else {
            nbRegStokes++;
            return rMax - 1;
        }
        
    }
    public void simulerLiberationRegistre(){
        if(nbRegStokes > 0){
            nbRegStokes--;
        }else if(compteurRegistre >= 2){
            compteurRegistre--;
        }
    }

    protected int saveCompteurRegistre = 0;
    protected int saveNbRegStokes = 0;



    public void resetGestionnaireRegistre(boolean reset){
        if(reset){
            compteurRegistre = saveCompteurRegistre;
            nbRegStokes = saveNbRegStokes;
        }else{
            compteurRegistre = 2;
            nbRegStokes = 0;
        }
    }

    public void commencerSimulationRegistre(boolean reset){
        // on sauvegarde le contexte d'allocation de registre : 
        saveCompteurRegistre = compteurRegistre;
        saveNbRegStokes = nbRegStokes;
        
        if(reset){

            compteurRegistre = 2;
            nbRegStokes = 0;
        }

    }



    public int getCompteurRegistre() { 
        return compteurRegistre; 
    }

    public void setCompteurRegistre(int v) { 
        this.compteurRegistre = v; 
    }

    public int getNbRegStokes() { 
        return nbRegStokes; 
    }

    public void setNbRegStokes(int v) { 
        this.nbRegStokes = v; 
    }

}
