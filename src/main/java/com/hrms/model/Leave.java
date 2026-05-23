package com.hrms.model;

/**
 * Model class representing a Leave request.
 * Maps to the 'leaves' table in the SQLite database.
 */
public class Leave {

    private int id;
    private int empId;
    private String reason;
    private String status;    // 'Pending', 'Approved', 'Rejected'
    private String fromDate;  // Format: yyyy-MM-dd
    private String toDate;    // Format: yyyy-MM-dd
    private String empName;   // Joined field for display (not a DB column)

    // ---- Constructors ----

    public Leave() {}

    public Leave(int id, int empId, String reason, String status,
                 String fromDate, String toDate) {
        this.id = id;
        this.empId = empId;
        this.reason = reason;
        this.status = status;
        this.fromDate = fromDate;
        this.toDate = toDate;
    }

    // ---- Getters ----

    public int getId()           { return id; }
    public int getEmpId()        { return empId; }
    public String getReason()    { return reason; }
    public String getStatus()    { return status; }
    public String getFromDate()  { return fromDate; }
    public String getToDate()    { return toDate; }
    public String getEmpName()   { return empName; }

    // ---- Setters ----

    public void setId(int id)               { this.id = id; }
    public void setEmpId(int empId)         { this.empId = empId; }
    public void setReason(String reason)    { this.reason = reason; }
    public void setStatus(String status)    { this.status = status; }
    public void setFromDate(String fromDate){ this.fromDate = fromDate; }
    public void setToDate(String toDate)    { this.toDate = toDate; }
    public void setEmpName(String empName)  { this.empName = empName; }

    @Override
    public String toString() {
        return "Leave{id=" + id + ", empId=" + empId + ", reason='" + reason
                + "', status='" + status + "', from='" + fromDate + "', to='" + toDate + "'}";
    }
}
