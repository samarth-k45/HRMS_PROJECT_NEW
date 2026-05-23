package com.hrms.dao;

import com.hrms.model.Payroll;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Payroll operations.
 * Supports upsert (add or update) payroll records and querying payslips.
 * Formula: total = basic + bonus - deductions
 */
public class PayrollDAO {

    /**
     * Returns the payroll record for a specific employee.
     * Returns null if no payroll record exists.
     */
    public Payroll getPayrollByEmp(int empId) {
        String sql = "SELECT p.*, e.name as emp_name FROM payroll p " +
                     "JOIN employees e ON p.emp_id = e.id " +
                     "WHERE p.emp_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, empId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Payroll p = mapRow(rs);
                p.setEmpName(rs.getString("emp_name"));
                return p;
            }
        } catch (SQLException e) {
            System.err.println("[PayrollDAO] getPayrollByEmp error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Returns all payroll records (Admin view).
     * Joins employees table to include employee names.
     */
    public List<Payroll> getAllPayroll() {
        List<Payroll> list = new ArrayList<>();
        String sql = "SELECT p.*, e.name as emp_name FROM payroll p " +
                     "JOIN employees e ON p.emp_id = e.id " +
                     "ORDER BY e.name";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Payroll p = mapRow(rs);
                p.setEmpName(rs.getString("emp_name"));
                list.add(p);
            }
        } catch (SQLException e) {
            System.err.println("[PayrollDAO] getAllPayroll error: " + e.getMessage());
        }
        return list;
    }

    /**
     * Inserts or updates a payroll record (upsert).
     * Automatically calculates total = basic + bonus - deductions.
     * Uses INSERT OR REPLACE for SQLite upsert behavior.
     *
     * @return true if operation succeeded
     */
    public boolean addOrUpdatePayroll(Payroll payroll) {
        // Auto-calculate total before saving
        double total = payroll.getBasic() + payroll.getBonus() - payroll.getDeductions();
        payroll.setTotal(total);

        // Check if a record already exists for this employee
        String checkSql = "SELECT id FROM payroll WHERE emp_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement checkPs = conn.prepareStatement(checkSql)) {

            checkPs.setInt(1, payroll.getEmpId());
            ResultSet rs = checkPs.executeQuery();

            if (rs.next()) {
                // UPDATE existing record
                String sql = "UPDATE payroll SET basic=?, bonus=?, deductions=?, total=? WHERE emp_id=?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setDouble(1, payroll.getBasic());
                    ps.setDouble(2, payroll.getBonus());
                    ps.setDouble(3, payroll.getDeductions());
                    ps.setDouble(4, payroll.getTotal());
                    ps.setInt(5, payroll.getEmpId());
                    return ps.executeUpdate() > 0;
                }
            } else {
                // INSERT new record
                String sql = "INSERT INTO payroll (emp_id, basic, bonus, deductions, total) VALUES (?,?,?,?,?)";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, payroll.getEmpId());
                    ps.setDouble(2, payroll.getBasic());
                    ps.setDouble(3, payroll.getBonus());
                    ps.setDouble(4, payroll.getDeductions());
                    ps.setDouble(5, payroll.getTotal());
                    return ps.executeUpdate() > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("[PayrollDAO] addOrUpdatePayroll error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Returns the total payroll expenditure (sum of all totals).
     * Used by the Dashboard.
     */
    public double getTotalPayroll() {
        String sql = "SELECT SUM(total) FROM payroll";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) return rs.getDouble(1);

        } catch (SQLException e) {
            System.err.println("[PayrollDAO] getTotalPayroll error: " + e.getMessage());
        }
        return 0.0;
    }

    /**
     * Maps a ResultSet row to a Payroll object.
     */
    private Payroll mapRow(ResultSet rs) throws SQLException {
        return new Payroll(
            rs.getInt("id"),
            rs.getInt("emp_id"),
            rs.getDouble("basic"),
            rs.getDouble("bonus"),
            rs.getDouble("deductions"),
            rs.getDouble("total")
        );
    }
}
