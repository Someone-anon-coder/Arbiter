class Book {
    private String title;
    private String author;
    private int pages;

    public Book(String title, String author, int pages) {
        this.title = title;
        this.author = author;
        this.pages = pages;
    }

    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public int getPages() { return pages; }

    public void setPages(int pages) {
        if (pages < 1) {
            return;
        } 
        
        this.pages = pages;
    }
}

class BankAccount {
    private String ownerName;
    private double balance;
    private int accountId;

    private static double totalBalance;
    private static int accountCounter;

    public BankAccount(String ownerName, double balance) {
        this.ownerName = ownerName;
        this.balance = balance;
        this.accountId = ++accountCounter;

        totalBalance += balance;
    }

    public String getOwnerName() { return ownerName; }
    public double getBalance() { return balance; }
    public int getAccountId() { return accountId; }

    public void deposit(double amount) {
        balance += amount;
        totalBalance += amount;
    }

    public void withdraw(double amount) {
        if (amount > balance) {
            return;
        } 

        balance -= amount;
        totalBalance -= amount;
    }

    public static double getTotalBankBalance() {
        return totalBalance;
    }
}

public class java_4OopCore {
    public static void main(String[] args) {
        Book book1 = new Book("Book1", "Author1", 37);
        Book book2 = new Book("Book2", "Author2", 20);

        System.out.println("Book 1 ==================== 1 Book");
        System.out.println("Book1 Title: " + book1.getTitle());
        System.out.println("Book1 Author: " + book1.getAuthor());
        System.out.println("Book1 Pages: " + book1.getPages());
        System.out.println("Book 1 ==================== 1 Book");

        System.out.println("\nBook 2 ==================== 2 Book");
        System.out.println("Book2 Title: " + book2.getTitle());
        System.out.println("Book2 Author: " + book2.getAuthor());
        System.out.println("Book2 Pages: " + book2.getPages());
        System.out.println("Book 2 ==================== 2 Book");

        System.out.println("\nOriginal Book1 Pages: " + book1.getPages());
        
        System.out.println("\nSetting Book1 Pages to -5");
        book1.setPages(-5);
        System.out.println("New Book1 Pages (If setting to -5): " + book1.getPages());

        System.out.println("\nSetting Book1 Pages to 40");
        book1.setPages(40);
        System.out.println("New Book1 Pages (If setting to 40): " + book1.getPages());
        System.out.println();

        BankAccount account1 = new BankAccount("Owner1", 2000);
        BankAccount account2 = new BankAccount("Owner2", 3000);
        BankAccount account3 = new BankAccount("Owner3", 5000);

        System.out.println("Auto Assigned Id for Account1: " + account1.getAccountId());
        System.out.println("Auto Assigned Id for Account2: " + account2.getAccountId());
        System.out.println("Auto Assigned Id for Account3: " + account3.getAccountId());
        System.out.println();

        System.out.println("Bank ==================== Bank");
        System.out.println("Total Bank Balance (2000 + 3000 + 5000 = 10000) : " + BankAccount.getTotalBankBalance());
        
        account1.deposit(1000);
        account2.withdraw(2000);
        account2.withdraw(2000); // No action on balance (current balance is 1000)
        
        System.out.println();
        System.out.println("Total Bank Balance: " + BankAccount.getTotalBankBalance());
        System.out.println();

        System.out.println("Bank1 Account Balance: " + account1.getBalance());
        System.out.println("Bank2 Account Balance: " + account2.getBalance());
        System.out.println("Bank3 Account Balance: " + account3.getBalance());

        System.out.println("Bank ==================== Bank");
        System.out.println();
    }
}
