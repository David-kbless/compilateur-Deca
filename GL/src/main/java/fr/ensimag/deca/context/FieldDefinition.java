package fr.ensimag.deca.context;

import fr.ensimag.deca.tree.Location;
import fr.ensimag.deca.tree.Visibility;

/**
 * Definition of a field (data member of a class).
 *
 * @author gl54
 * @date 01/01/2026
 */
public class FieldDefinition extends ExpDefinition {
    public int getIndex() {
        return index;
    }

    private int index;
    private int nbNbRegEstimee = 0;

    public int getNbNbRegEstimee() {
        return nbNbRegEstimee;
    }

    public void setNbNbRegEstimeeIfMax(int newNbNbRegEstimee) {
        if(newNbNbRegEstimee > nbNbRegEstimee){
            this.nbNbRegEstimee = newNbNbRegEstimee;
        }
    }
    
    @Override
    public boolean isField() {
        return true;
    }

    private final Visibility visibility;
    private final ClassDefinition containingClass;
    
    public FieldDefinition(Type type, Location location, Visibility visibility,
            ClassDefinition memberOf, int index) {
        super(type, location);
        this.visibility = visibility;
        this.containingClass = memberOf;
        this.index = index;
    }
    
    @Override
    public FieldDefinition asFieldDefinition(String errorMessage, Location l)
            throws ContextualError {
        return this;
    }

    public Visibility getVisibility() {
        return visibility;
    }

    public ClassDefinition getContainingClass() {
        return containingClass;
    }

    @Override
    public String getNature() {
        return "field";
    }

    @Override
    public boolean isExpression() {
        return true;
    }

}
