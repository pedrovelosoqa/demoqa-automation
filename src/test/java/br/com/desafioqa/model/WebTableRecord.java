package br.com.desafioqa.model;

import java.util.Objects;

/**
 * Immutable representation of a row in DemoQA's Web Tables component.
 */
public final class WebTableRecord {

    private final String firstName;
    private final String lastName;
    private final String email;
    private final int age;
    private final int salary;
    private final String department;

    public WebTableRecord(
            String firstName,
            String lastName,
            String email,
            int age,
            int salary,
            String department) {
        this.firstName = Objects.requireNonNull(firstName, "firstName must not be null");
        this.lastName = Objects.requireNonNull(lastName, "lastName must not be null");
        this.email = Objects.requireNonNull(email, "email must not be null");
        this.age = age;
        this.salary = salary;
        this.department = Objects.requireNonNull(department, "department must not be null");
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public int getAge() {
        return age;
    }

    public int getSalary() {
        return salary;
    }

    public String getDepartment() {
        return department;
    }

    /**
     * Convenience copy used by update scenarios while preserving identity fields.
     */
    public WebTableRecord withUpdatedFields(
            String newFirstName,
            int newSalary,
            String newDepartment) {
        return new WebTableRecord(
                newFirstName,
                lastName,
                email,
                age,
                newSalary,
                newDepartment);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof WebTableRecord)) {
            return false;
        }
        WebTableRecord that = (WebTableRecord) other;
        return age == that.age
                && salary == that.salary
                && firstName.equals(that.firstName)
                && lastName.equals(that.lastName)
                && email.equals(that.email)
                && department.equals(that.department);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstName, lastName, email, age, salary, department);
    }

    @Override
    public String toString() {
        return "WebTableRecord{"
                + "firstName='" + firstName + '\''
                + ", lastName='" + lastName + '\''
                + ", email='" + email + '\''
                + ", age=" + age
                + ", salary=" + salary
                + ", department='" + department + '\''
                + '}';
    }
}
