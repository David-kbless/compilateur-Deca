package fr.ensimag.deca.codegen;


public class GestionnairePile {
    private int maxTaillePile = 0;
    private int tailleCourante = 0;

    // Utilisé au début de chaque bloc (main, méthode, init)
    public void reset() {
        this.maxTaillePile = 0;
        this.tailleCourante = 0;
    }

    public void instructionPush() {
        incrementer(1);
    }

    public void instructionBsr() {
        incrementer(2);

        decrementer(2);
    }


    public void instructionPop() {
        decrementer(1);
    }

    public void instructionAddsp(int n) {
        incrementer(n);
    }

    public void instructionSubsp(int n) {
        decrementer(n);
    }

    private void incrementer(int n) {
        this.tailleCourante += n;
        if (this.tailleCourante > this.maxTaillePile) {
            this.maxTaillePile = this.tailleCourante;
        }
    }

    private void decrementer(int n) {
        this.tailleCourante -= n;
    }

    public int getMax() {
        return maxTaillePile;
    }
}