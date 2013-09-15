package tyrant.mikera.tyrant.util;

public class Count {
    public int value = 0;

    public Count() {
        this(0);
    }
    public Count(int value) {
        this.value = value;
    }

    public String toString() {
        return String.valueOf(value);
    }

    public void increment() {
        value++;
    }
}