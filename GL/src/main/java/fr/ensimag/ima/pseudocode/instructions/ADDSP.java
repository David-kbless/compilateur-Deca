package fr.ensimag.ima.pseudocode.instructions;

import fr.ensimag.ima.pseudocode.ImmediateInteger;
import fr.ensimag.ima.pseudocode.UnaryInstructionImmInt;

/**
 * Add a value to stack pointer.
 * 
 * @author Ensimag
 * @date 01/01/2026
 */
public class ADDSP extends UnaryInstructionImmInt {

    private int value = 0;

    public ADDSP(ImmediateInteger operand) {
        super(operand);
        this.value = operand.getValue();
        enleveLinter(true);
    }

    public ADDSP(int i) {
        super(i);
        this.value = i;
    }

    private void enleveLinter( boolean b){
        if(!b){
            System.out.println(value);
        }
    }

}
