package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;

import java.util.List;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;


/**
 * 
 * @author gl54
 * @date 01/01/2026
 */
public class ListInst extends TreeList<AbstractInst> {

    private int maxNumRegUtilises;
    public int getmaxNumRegUtilises() {
        return maxNumRegUtilises;
    }


    /**
     * Implements non-terminal "list_inst" of [SyntaxeContextuelle] in pass 3
     * @param compiler contains "env_types" attribute
     * @param localEnv corresponds to "env_exp" attribute
     * @param currentClass 
     *          corresponds to "class" attribute (null in the main bloc).
     * @param returnType
     *          corresponds to "return" attribute (void in the main bloc).
     */    
    public void verifyListInst(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass, Type returnType)
            throws ContextualError {

                List<AbstractInst> list = getList(); //On récupère la liste des instructions
                for (AbstractInst inst : list) {
                    inst.verifyInst(compiler, localEnv, currentClass, returnType); //On vérifie chaque instruction
                }                                                                  //de la liste en lui passant les attributs hérités 
    }

    public void codeGenListInst(DecacCompiler compiler) {
        for (AbstractInst i : getList()) {
            i.codeGenInst(compiler);

            // on libère le dernier registre utilisé par une affectation
            if(i instanceof Assign){
                compiler.libererRegistre();
            }
        }
    }

    public int simuleExecutionNbRegistres(DecacCompiler compiler){
        int maxNumRegUtilises = 0;
        for (AbstractInst i : getList()) {
            int nbReg = i.simuleExecutionNbRegistres(compiler);
            if(nbReg > maxNumRegUtilises){
                maxNumRegUtilises = nbReg;
            }
        }
        return maxNumRegUtilises;
    }

    @Override
    public void decompile(IndentPrintStream s) {
        for (AbstractInst i : getList()) {
            i.decompileInst(s);
            s.println();
        }
    }
}
