package fr.ensimag.deca.tree;

import java.io.PrintStream;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.NullOperand;
import fr.ensimag.ima.pseudocode.instructions.LOAD;

public class Null extends AbstractExpr {
   public Null() {
   }

   @Override
   public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass)
        throws ContextualError {
      Type t = compiler.environmentType.NULL; // Retriving the string type defined in the global environment
      this.setType(t); // Updating the expression's type 
      return t; // Returning the synthetized attribut
   }

   @Override
   public void decompile(IndentPrintStream s) {
      s.print("null");
   }

   @Override
   protected void prettyPrintChildren(PrintStream s, String prefix) {
      // No children to print
   }

   @Override
   protected void iterChildren(TreeFunction f) {
      // No children to iterate over
   }


   protected GPRegister codeGenInst(DecacCompiler compiler){
      GPRegister reg =  compiler.utiliserRegistre();
      
      // on met 0 dans le registre puis on le retourne : 
      compiler.addInstruction(new LOAD(new NullOperand(), reg));

      return reg;
   }

   @Override
    public int simuleExecutionNbRegistres(DecacCompiler compiler){
        return compiler.simulerAllocationRegistre();
    }
    
}
