package com.hrms.model;

/**
 * Model class representing an Employee entity.
 * Maps to the 'employees' table in the SQLite database.
 */
public class Employee {

    private int id;
    private String name;
    private String email;
    private String password;
    private String department;
    private String role;       // 'admin' or 'employee'
    private double salary;

    // ---- Constructors ----

    public Employee() {}

    public Employee(int id, String name, String email, String password,
                    String department, String role, double salary) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.department = department;
        this.role = role;
        this.salary = salary;
    }

    // ---- Getters ----

    public int getId()           { return id; }
    public String getName()      { return name; }
    public String getEmail()     { return email; }
    public String getPassword()  { return password; }
    public String getDepartment(){ return department; }
    public String getRole()      { return role; }
    public double getSalary()    { return salary; }

    // ---- Setters ----

    public void setId(int id)                 { this.id = id; }
    public void setName(String name)           { this.name = name; }
    public void setEmail(String email)         { this.email = email; }
    public void setPassword(String password)   { this.password = password; }
    public void setDepartment(String department){ this.department = department; }
    public void setRole(String role)           { this.role = role; }
    public void setSalary(double salary)       { this.salary = salary; }

    @Override
    public String toString() {
        return "Employee{id=" + id + ", name='" + name + "', email='" + email
                + "', department='" + department + "', role='" + role + "', salary=" + salary + "}";
    }
}
