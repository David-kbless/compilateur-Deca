package fr.ensimag.deca.tree;

/**
 * Visibility of a field.
 *
 * @author gl54
 * @date 01/01/2026
 */

public enum Visibility {
    PUBLIC,
    PROTECTED;

    public void decompile(fr.ensimag.deca.tools.IndentPrintStream s) {
        switch (this) {
            case PUBLIC:
                s.print("public");
                break;
            case PROTECTED:
                s.print("protected");
                break;
            default:
                // This should never happen
                throw new IllegalStateException("Unexpected value: " + this);
        }
    }
}


