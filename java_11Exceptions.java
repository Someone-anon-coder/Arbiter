class NegativeAmountException extends Exception {
    public NegativeAmountException(String message) {
        super(message);
    }
}

class DataFormatException extends Exception {
    public DataFormatException(String message) {
        super("Data Format Exception: " + message);
    }
}

class DataCorruptionException extends RuntimeException {
    public DataCorruptionException(String message) {
        super("Data Corruption Exception: " + message);
    }
}

class TrackedResource implements AutoCloseable {
    private String name;
    
    public TrackedResource(String name) {
        this.name = name;
    }

    @Override
    public void close() {
        System.out.println("Closing " + name);
    }

    public void process(int code) throws DataFormatException, DataCorruptionException {
        if (code == 1) throw new DataFormatException("Code = " + code);
        else if (code == 2) throw new DataCorruptionException("Code = " + code);
        else System.out.println("Processed " + code + " in " + name);
    }
}

public class java_11Exceptions {
    public static void main(String[] args) {
        try {
            int result = withdraw(100, 40);
            System.out.println("Result: " + result);
        } catch (NegativeAmountException e) {
            System.out.println(e.getMessage());
        } finally {
            System.out.println("Attempt finished.");
        }

        try {
            int result = withdraw(100, -10);
            System.out.println("Result: " + result);
        } catch (NegativeAmountException e) {
            System.out.println(e.getMessage());
        } finally {
            System.out.println("Attempt finished.");
        }

        System.out.println();

        try (
            TrackedResource tr1 = new TrackedResource("Tracked Resource 1");
            TrackedResource tr2 = new TrackedResource("Tracked Resource 2");
        ) {
            tr1.process(0);
        } catch (DataCorruptionException e) {
            System.out.println(e.getMessage());
        } catch (DataFormatException e) {
            System.out.println(e.getMessage());
        }

        try (
            TrackedResource tr1 = new TrackedResource("Tracked Resource 1");
            TrackedResource tr2 = new TrackedResource("Tracked Resource 2");
        ) {
            tr1.process(1);
        } catch (DataCorruptionException e) {
            System.out.println(e.getMessage());
        } catch (DataFormatException e) {
            System.out.println(e.getMessage());
        }

        try (
            TrackedResource tr1 = new TrackedResource("Tracked Resource 1");
            TrackedResource tr2 = new TrackedResource("Tracked Resource 2");
        ) {
            tr1.process(2);
        } catch (DataCorruptionException e) {
            System.out.println(e.getMessage());
        } catch (DataFormatException e) {
            System.out.println(e.getMessage());
        }
    }

    public static int withdraw(int balance, int amount) throws NegativeAmountException {
        if (amount < 0) {
            throw new NegativeAmountException("Amount cannot be negative: " + amount);
        }
        return balance - amount;
    }
}
