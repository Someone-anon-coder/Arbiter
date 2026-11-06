package problem2;

public class MemberDataException extends Exception {
    private final ErrorCategory category;

    public MemberDataException(ErrorCategory category, String safeMessage, Throwable cause) {
        super(safeMessage, cause);
        this.category = category;
    }

    public ErrorCategory getCategory() {
        return category;
    }
}