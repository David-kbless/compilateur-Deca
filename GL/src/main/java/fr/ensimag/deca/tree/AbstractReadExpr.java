package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.BOV;
import fr.ensimag.ima.pseudocode.instructions.LOAD;

/**
 * read...() statement.
 *
 * @author gl54
 * @date 01/01/2026
 */
public abstract class AbstractReadExpr extends AbstractExpr {

    public AbstractReadExpr() {
        super();
    }

    protected GPRegister codeGenInst(DecacCompiler compiler){
      GPRegister reg =  compiler.utiliserRegistre();
      
      // on lit dans r1 puis on move vers le registre reg
      codeGenRead(compiler);

      // on gere les debordements ou erreur de lecture : 
      if (!compiler.getCompilerOptions().getNoCheck()) {
            compiler.addInstruction(new BOV(new Label("io_error")));
            compiler.addIOErreurLabel();
        }
      compiler.addInstruction(new LOAD(Register.R1, reg));

      return reg;
    }

        public abstract void codeGenRead(DecacCompiler compiler);

    

        @Override

        public int simuleExecutionNbRegistres(DecacCompiler compiler){

            return compiler.simulerAllocationRegistre();

        }

    }
