
package fr.ensimag.deca.syntax;

public class MyClass {
    private int value;

    public MyClass(int value) {
        this.value = value;
    }

    public boolean equals(Object obj) {
        return this == obj;
    }

    public int getValue() {
        return value;
    }
}
