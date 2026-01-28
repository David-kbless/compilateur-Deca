package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.instructions.WNL;
import fr.ensimag.ima.pseudocode.instructions.WSTR;
import java.io.PrintStream;
import org.apache.commons.lang.Validate;

/**
 * String literal
 *
 * @author gl54
 * @date 01/01/2026
 */
public class StringLiteral extends AbstractStringLiteral {

    @Override
    public String getValue() {
        return value;
    }

    private String value;

    public StringLiteral(String value) {
        Validate.notNull(value);
        this.value = value.substring(1, value.length()-1); // remove the quotes
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
                
                Type t = compiler.environmentType.STRING; 
                this.setType(t); 
                return t; 
    }

    @Override
    protected void codeGenPrint(DecacCompiler compiler, boolean printHex) {
        String content = getValue(); 
        if (content == null) return;

        // On remplace d'abord les doubles backslashes par un seul
        content = content.replace("\\\\", "\\");

        // On remplace le backslash-guillemet par un guillemet simple
        content = content.replace("\\\"", "\"");

        // on decoupe pour les sauts de ligne (EOL physique ou \n écrit)
        String[] lines = content.split("\n|\\\\n", -1);

        for (int i = 0; i < lines.length; i++) {
            if (!lines[i].isEmpty()) {
                compiler.addInstruction(new WSTR(lines[i]));
            }
            if (i < lines.length - 1) {
                compiler.addInstruction(new WNL());
            }
        }
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("\"" + value + "\"");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        // leaf node => nothing to do
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        // leaf node => nothing to do
    }
    
    @Override
    String prettyPrintNode() {
        return "StringLiteral (" + value + ")";
    }

    @Override
    protected GPRegister codeGenInst(DecacCompiler compiler){
        return null;
    }

    @Override
    public int simuleExecutionNbRegistres(DecacCompiler compiler){
        return 0;
    }
}
