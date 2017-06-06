package ca.projecthermes.projecthermes.exceptions;

public class IntValueException extends Exception {
    public final int value;
    public IntValueException(int value) {
        this.value = value;
    }
}
