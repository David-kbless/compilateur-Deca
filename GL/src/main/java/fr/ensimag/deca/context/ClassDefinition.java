package fr.ensimag.deca.context;

import fr.ensimag.deca.tree.Location;
import fr.ensimag.ima.pseudocode.DAddr;
import fr.ensimag.ima.pseudocode.Label;

import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.Validate;

/**
 * Definition of a class.
 *
 * @author gl54
 * @date 01/01/2026
 */
public class ClassDefinition extends TypeDefinition {

    private Label labelInit;

    public Label getLabelInit() {
        return labelInit;
    }
    
    public void setLabelInit(Label labelInit) {
        this.labelInit = labelInit;
    }

    // @safwane j'ai du rajouter une façon ordonné de bien gérer les index de façon ordonné
    private Map<Integer, MethodDefinition> methodTable = new TreeMap<>();

    public void setMethod(int index, MethodDefinition method) {
        this.methodTable.put(index, method);
    }

    public MethodDefinition getMethodByIndex(int index) {
        return methodTable.get(index);
    }
    
    public Map<Integer, MethodDefinition> getMethodTable() {
        return methodTable;
    }
//_____________________________________-------------------_________________________

    public void setNumberOfFields(int numberOfFields) {
        this.numberOfFields = numberOfFields;
    }

    public int getNumberOfFields() {
        return numberOfFields;
    }

    public void incNumberOfFields() {
        this.numberOfFields++;
    }

    public int getNumberOfMethods() {
        return numberOfMethods;
    }

    public void setNumberOfMethods(int n) {
        Validate.isTrue(n >= 0);
        numberOfMethods = n;
    }
    
    public int incNumberOfMethods() {
        numberOfMethods++;
        return numberOfMethods;
    }

    public void setMembers(EnvironmentExp members){
        Validate.notNull(members, "members cannot be null");
        this.members = members;
    }

    private int numberOfFields = 0;
    private int numberOfMethods = 0;
    
    @Override
    public boolean isClass() {
        return true;
    }
    
    @Override
    public ClassType getType() {
        // Cast succeeds by construction because the type has been correctly set
        // in the constructor.
        return (ClassType) super.getType();
    };

    public ClassDefinition getSuperClass() {
        return superClass;
    }

    private EnvironmentExp members;
    private final ClassDefinition superClass; 

    public EnvironmentExp getMembers() {
        return members;
    }

    public ClassDefinition(ClassType type, Location location, ClassDefinition superClass) {
        super(type, location);
        EnvironmentExp parent;
        if (superClass != null) {
            parent = superClass.getMembers();
        } else {
            parent = null;
        }
        members = new EnvironmentExp(parent); //Cet EnvironmentExp est local à la classe
        this.superClass = superClass;
    }

    @Override
    public ClassDefinition asClassDefinition(String errorMessage, Location l) {
        return this;
    }
    
    // addr dans la pile 
    private DAddr vTableAddr;

    public DAddr getVTableAddr(){
        return vTableAddr;
    }

    public void setVtableAddr(DAddr vtAddr){
        this.vTableAddr = vtAddr;
    }


    private int nbNbRegEstimee = 0;

    public int getNbNbRegEstimee() {
        return nbNbRegEstimee;
    }

    public void setNbNbRegEstimeeIfMax(int newNbNbRegEstimee) {
        if(newNbNbRegEstimee > nbNbRegEstimee){
            this.nbNbRegEstimee = newNbNbRegEstimee;
        }
    }
}
