package fr.ensimag.deca.codegen;

public class GestionnaireLabel {
    
    private int compteurLabelWhile = 1;

    public int getCompteurLabelWhile() {
        return compteurLabelWhile;
    }

    public int incrementerCompteurLabelWhile() {
        return compteurLabelWhile++;
    }

}
