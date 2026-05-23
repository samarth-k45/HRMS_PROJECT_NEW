package com.hrms.model;

/**
 * Model class representing an Attendance record.
 * Maps to the 'attendance' table in the SQLite database.
 */
public class Attendance {

    private int id;
    private int empId;
    private String date;      // Format: yyyy-MM-dd
    private String status;    // 'Present' or 'Absent'
    private String empName;   // Joined field for display (not a DB column)

    // ---- Constructors ----

    public Attendance() {}

    public Attendance(int id, int empId, String date, String status) {
        this.id = id;
        this.empId = empId;
        this.date = date;
        this.status = status;
    }

    // ---- Getters ----

    public int getId()         { return id; }
    public int getEmpId()      { return empId; }
    public String getDate()    { return date; }
    public String getStatus()  { return status; }
    public String getEmpName() { return empName; }

    // ---- Setters ----

    public void setId(int id)             { this.id = id; }
    public void setEmpId(int empId)       { this.empId = empId; }
    public void setDate(String date)      { this.date = date; }
    public void setStatus(String status)  { this.status = status; }
    public void setEmpName(String empName){ this.empName = empName; }

    @Override
    public String toString() {
        return "Attendance{id=" + id + ", empId=" + empId
                + ", date='" + date + "', status='" + status + "'}";
    }
}
