package com.hrms.dao;

import com.hrms.model.Leave;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Leave operations.
 * Handles applying, listing, and approving/rejecting leave requests.
 */
public class LeaveDAO {

    /**
     * Inserts a new leave application into the database.
     * Status defaults to 'Pending'.
     *
     * @return true if insert succeeded
     */
    public boolean applyLeave(Leave leave) {
        String sql = "INSERT INTO leaves (emp_id, reason, status, from_date, to_date) " +
                     "VALUES (?, ?, 'Pending', ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, leave.getEmpId());
            ps.setString(2, leave.getReason());
            ps.setString(3, leave.getFromDate());
            ps.setString(4, leave.getToDate());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[LeaveDAO] applyLeave error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Returns all leave requests (Admin view).
     * Joins employees table to include employee names.
     */
    public List<Leave> getAllLeaves() {
        List<Leave> list = new ArrayList<>();
        String sql = "SELECT l.*, e.name as emp_name FROM leaves l " +
                     "JOIN employees e ON l.emp_id = e.id " +
                     "ORDER BY l.id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Leave leave = mapRow(rs);
                leave.setEmpName(rs.getString("emp_name"));
                list.add(leave);
            }
        } catch (SQLException e) {
            System.err.println("[LeaveDAO] getAllLeaves error: " + e.getMessage());
        }
        return list;
    }

    /**
     * Returns leave requests belonging to a specific employee.
     */
    public List<Leave> getLeavesByEmp(int empId) {
        List<Leave> list = new ArrayList<>();
        String sql = "SELECT l.*, e.name as emp_name FROM leaves l " +
                     "JOIN employees e ON l.emp_id = e.id " +
                     "WHERE l.emp_id = ? ORDER BY l.id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, empId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Leave leave = mapRow(rs);
                leave.setEmpName(rs.getString("emp_name"));
                list.add(leave);
            }
        } catch (SQLException e) {
            System.err.println("[LeaveDAO] getLeavesByEmp error: " + e.getMessage());
        }
        return list;
    }

    /**
     * Updates the status of a leave request (Approved or Rejected).
     * Only admin should call this.
     *
     * @param leaveId the ID of the leave record
     * @param status  'Approved' or 'Rejected'
     * @return true if update succeeded
     */
    public boolean updateLeaveStatus(int leaveId, String status) {
        String sql = "UPDATE leaves SET status = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setInt(2, leaveId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[LeaveDAO] updateLeaveStatus error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Returns the count of all Pending leave requests.
     * Used by the Dashboard.
     */
    public int getPendingLeaveCount() {
        String sql = "SELECT COUNT(*) FROM leaves WHERE status = 'Pending'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) return rs.getInt(1);

        } catch (SQLException e) {
            System.err.println("[LeaveDAO] getPendingLeaveCount error: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Maps a ResultSet row to a Leave object.
     */
    private Leave mapRow(ResultSet rs) throws SQLException {
        return new Leave(
            rs.getInt("id"),
            rs.getInt("emp_id"),
            rs.getString("reason"),
            rs.getString("status"),
            rs.getString("from_date"),
            rs.getString("to_date")
        );
    }
}
