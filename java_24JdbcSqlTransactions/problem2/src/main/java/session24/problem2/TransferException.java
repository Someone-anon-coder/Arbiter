package session24.problem2;

public class TransferException extends Exception {

    public enum Category {
        INSUFFICIENT_FUNDS,
        OTHER
    }

    private final Category category;
    private final String sqlState;

    public TransferException(Category category, String message, String sqlState, Throwable cause) {
        super(message, cause);
        this.category = category;
        this.sqlState = sqlState;
    }

    public TransferException(Category category, String message) {
        super(message);
        this.category = category;
        this.sqlState = null;
    }

    public Category getCategory() {
        return category;
    }

    public String getSqlState() {
        return sqlState;
    }
}
