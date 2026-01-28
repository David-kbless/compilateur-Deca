package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;

public class ListDeclField extends TreeList<AbstractDeclField> {


    


    public EnvironmentExp verifyListDeclField(DecacCompiler compiler, ClassDefinition superClass,
                                       ClassDefinition currentClass)
            throws ContextualError {
        EnvironmentExp envExpR = new EnvironmentExp(null);
        for (AbstractDeclField field : getList()) {
           field.verifyDeclField(compiler, superClass, currentClass, envExpR); // je ne sais pas quoi faire avec
           
           compiler.commencerSimulationRegistres(true);
           currentClass.setNbNbRegEstimeeIfMax(((DeclField)field).simuleExecutionNbRegistres(compiler));
           compiler.resetGestionnaireRegistres(false);
        }
        return envExpR;
    }

    public void verifyListFieldBody(DecacCompiler compiler, EnvironmentExp envExp,
                                    ClassDefinition currentClass)
            throws ContextualError {
        for (AbstractDeclField field : getList()) {
            field.verifyFieldBody(compiler, currentClass, envExp); // je ne sais pas quoi faire avec
        }
    }

    public void codeGenListField(DecacCompiler compiler) {
        throw new UnsupportedOperationException("Unimplemented method 'codeGen'");
    }

    @Override
    public void decompile(IndentPrintStream s) {
        for (AbstractDeclField field : getList()) {
            field.decompile(s);
            s.println();
        }
    }
}
