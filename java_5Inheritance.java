class Shape {
    public double area() {
        System.out.println("Generic shape, no area.");
        return 0.0;
    }
}

class Circle extends Shape {
    private double radius;

    public Circle(double radius) {
        this.radius = radius;
    }

    @Override
    public double area() {
        double circleArea = Math.PI * (radius * radius);
        System.out.println("Circle Area: " + circleArea);

        return circleArea;
    }
}

class Employee {
    protected String name;
    protected double baseSalary;

    public Employee(String name, double baseSalary) {
        this.name = name;
        this.baseSalary = baseSalary;
    }

    public double calculatePay() {
        return baseSalary;
    }
}

class Manager extends Employee {
    private int teamSize;

    public Manager(String name, double baseSalary, int teamSize) {
        super(name, baseSalary);
        this.teamSize = teamSize;
    }

    @Override
    public double calculatePay() {
        return baseSalary + (teamSize * 200.0);
    }
}

class Contractor extends Employee {
    private int hoursWorked;
    private double hourlyRate;

    public Contractor(String name, double baseSalary, int hoursWorked, double hourlyRate) {
        super(name, baseSalary);

        this.hoursWorked = hoursWorked;
        this.hourlyRate = hourlyRate;
    }

    @Override
    public double calculatePay() {
        return baseSalary + (hoursWorked * hourlyRate);
    }
}

class Intern extends Employee {
    public Intern(String name, double baseSalary) {
        super(name, baseSalary);
    }
}

public class java_5Inheritance {
    public static void main(String[] args) {
        Shape shape = new Shape();
        double area = shape.area();
        System.out.println("Shape returned area: " + area);

        Shape circle = new Circle(5.0);
        System.out.println("\nArea of Circle: " + circle.area());
        System.out.println();

        Manager manager = new Manager("Manager1", 25000.0, 5);
        Contractor contractor1 = new Contractor("Contractor1", 5000.0, 36, 2000.0);
        Contractor contractor2 = new Contractor("Contractor2", 8000.0, 48, 4000.0);
        Intern intern1 = new Intern("Intern1", 10000.0);
        Intern intern2 = new Intern("Intern2", 12000.0);
        Intern intern3 = new Intern("Intern3", 8000.0);
        
        Employee[] employees = new Employee[]{intern1, manager, intern2, contractor1, intern3, contractor2};

        double totalPayroll = 0.0;
        for (int i = 0; i < employees.length; i++) {
            System.out.println("Employee Name: " + employees[i].name);
            System.out.println("Employee Pay: " + employees[i].calculatePay());
            System.out.println();

            totalPayroll += employees[i].calculatePay();
        }

        System.out.println("Total Payroll: " + totalPayroll);
    }
}
