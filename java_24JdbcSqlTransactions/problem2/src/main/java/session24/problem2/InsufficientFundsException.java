package session24.problem2;

public final class InsufficientFundsException extends TransferException {

    public InsufficientFundsException(String message, String sqlState, Throwable cause) {
        super(Category.INSUFFICIENT_FUNDS, message, sqlState, cause);
    }
}
