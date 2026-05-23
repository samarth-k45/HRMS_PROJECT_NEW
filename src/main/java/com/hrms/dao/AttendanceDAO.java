package com.hrms.dao;

import com.hrms.model.Attendance;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Attendance operations.
 * Handles marking attendance and querying attendance records.
 */
public class AttendanceDAO {

    /**
     * Marks attendance for an employee on a specific date.
     * Prevents duplicate entries: if the employee already has a record
     * for the given date, the existing record is updated instead.
     *
     * @return true if the operation succeeded
     */
    public boolean markAttendance(int empId, String date, String status) {
        // Check if a record already exists for this employee on this date
        if (hasMarkedToday(empId, date)) {
            // Update existing record
            String sql = "UPDATE attendance SET status = ? WHERE emp_id = ? AND date = ?";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setString(1, status);
                ps.setInt(2, empId);
                ps.setString(3, date);
                return ps.executeUpdate() > 0;

            } catch (SQLException e) {
                System.err.println("[AttendanceDAO] updateAttendance error: " + e.getMessage());
            }
        } else {
            // Insert new record
            String sql = "INSERT INTO attendance (emp_id, date, status) VALUES (?, ?, ?)";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setInt(1, empId);
                ps.setString(2, date);
                ps.setString(3, status);
                return ps.executeUpdate() > 0;

            } catch (SQLException e) {
                System.err.println("[AttendanceDAO] markAttendance error: " + e.getMessage());
            }
        }
        return false;
    }

    /**
     * Checks whether an employee has already marked attendance for a given date.
     *
     * @return true if a record already exists
     */
    public boolean hasMarkedToday(int empId, String date) {
        String sql = "SELECT COUNT(*) FROM attendance WHERE emp_id = ? AND date = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, empId);
            ps.setString(2, date);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;

        } catch (SQLException e) {
            System.err.println("[AttendanceDAO] hasMarkedToday error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Returns all attendance records for a specific employee,
     * ordered by date descending (most recent first).
     */
    public List<Attendance> getAttendanceByEmp(int empId) {
        List<Attendance> list = new ArrayList<>();
        String sql = "SELECT a.*, e.name as emp_name FROM attendance a " +
                     "JOIN employees e ON a.emp_id = e.id " +
                     "WHERE a.emp_id = ? ORDER BY a.date DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, empId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Attendance att = mapRow(rs);
                att.setEmpName(rs.getString("emp_name"));
                list.add(att);
            }
        } catch (SQLException e) {
            System.err.println("[AttendanceDAO] getAttendanceByEmp error: " + e.getMessage());
        }
        return list;
    }

    /**
     * Returns all attendance records for all employees (Admin view).
     * Joins with employees table to include employee names.
     */
    public List<Attendance> getAllAttendance() {
        List<Attendance> list = new ArrayList<>();
        String sql = "SELECT a.*, e.name as emp_name FROM attendance a " +
                     "JOIN employees e ON a.emp_id = e.id " +
                     "ORDER BY a.date DESC, e.name";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Attendance att = mapRow(rs);
                att.setEmpName(rs.getString("emp_name"));
                list.add(att);
            }
        } catch (SQLException e) {
            System.err.println("[AttendanceDAO] getAllAttendance error: " + e.getMessage());
        }
        return list;
    }

    /**
     * Returns the count of employees marked Present on a specific date.
     * Used by the Dashboard for today's attendance stat.
     */
    public int getPresentCountForDate(String date) {
        String sql = "SELECT COUNT(*) FROM attendance WHERE date = ? AND status = 'Present'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, date);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);

        } catch (SQLException e) {
            System.err.println("[AttendanceDAO] getPresentCountForDate error: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Returns the attendance status of an employee for a specific date.
     * Returns null if no record found.
     */
    public String getStatusForDate(int empId, String date) {
        String sql = "SELECT status FROM attendance WHERE emp_id = ? AND date = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, empId);
            ps.setString(2, date);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("status");

        } catch (SQLException e) {
            System.err.println("[AttendanceDAO] getStatusForDate error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Maps a ResultSet row to an Attendance object.
     */
    private Attendance mapRow(ResultSet rs) throws SQLException {
        return new Attendance(
            rs.getInt("id"),
            rs.getInt("emp_id"),
            rs.getString("date"),
            rs.getString("status")
        );
    }
}
