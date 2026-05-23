package com.hrms.model;

/**
 * Model class representing a Payroll record.
 * Maps to the 'payroll' table in the SQLite database.
 * Formula: total = basic + bonus - deductions
 */
public class Payroll {

    private int id;
    private int empId;
    private double basic;
    private double bonus;
    private double deductions;
    private double total;
    private String empName;   // Joined field for display (not a DB column)

    // ---- Constructors ----

    public Payroll() {}

    public Payroll(int id, int empId, double basic, double bonus,
                   double deductions, double total) {
        this.id = id;
        this.empId = empId;
        this.basic = basic;
        this.bonus = bonus;
        this.deductions = deductions;
        this.total = total;
    }

    // ---- Getters ----

    public int getId()              { return id; }
    public int getEmpId()           { return empId; }
    public double getBasic()        { return basic; }
    public double getBonus()        { return bonus; }
    public double getDeductions()   { return deductions; }
    public double getTotal()        { return total; }
    public String getEmpName()      { return empName; }

    // ---- Setters ----

    public void setId(int id)                    { this.id = id; }
    public void setEmpId(int empId)              { this.empId = empId; }
    public void setBasic(double basic)           { this.basic = basic; }
    public void setBonus(double bonus)           { this.bonus = bonus; }
    public void setDeductions(double deductions) { this.deductions = deductions; }
    public void setTotal(double total)           { this.total = total; }
    public void setEmpName(String empName)       { this.empName = empName; }

    /** Calculates and sets total = basic + bonus - deductions */
    public void calculateTotal() {
        this.total = this.basic + this.bonus - this.deductions;
    }

    @Override
    public String toString() {
        return "Payroll{id=" + id + ", empId=" + empId + ", basic=" + basic
                + ", bonus=" + bonus + ", deductions=" + deductions + ", total=" + total + "}";
    }
}
