package account.exceptions;

public class DuplicatePasswordException extends RuntimeException {
    public DuplicatePasswordException() {
        super("The passwords must be different!");
    }
}
