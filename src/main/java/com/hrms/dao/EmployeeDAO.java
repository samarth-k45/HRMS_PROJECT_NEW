package com.hrms.dao;

import com.hrms.model.Employee;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Employee CRUD operations.
 * All SQL queries use PreparedStatement to prevent SQL injection.
 */
public class EmployeeDAO {

    /**
     * Retrieves all employees from the database.
     * Excludes admin accounts from the regular employee list view.
     */
    public List<Employee> getAllEmployees() {
        List<Employee> employees = new ArrayList<>();
        String sql = "SELECT * FROM employees WHERE role = 'employee' ORDER BY name";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                employees.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[EmployeeDAO] getAllEmployees error: " + e.getMessage());
        }
        return employees;
    }

    /**
     * Retrieves all users including admins (used for dashboard stats).
     */
    public List<Employee> getAllUsers() {
        List<Employee> employees = new ArrayList<>();
        String sql = "SELECT * FROM employees ORDER BY name";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                employees.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[EmployeeDAO] getAllUsers error: " + e.getMessage());
        }
        return employees;
    }

    /**
     * Finds a single employee by their ID.
     * Returns null if no employee is found.
     */
    public Employee getEmployeeById(int id) {
        String sql = "SELECT * FROM employees WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("[EmployeeDAO] getEmployeeById error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Finds an employee by their email address.
     * Used for login validation.
     */
    public Employee getEmployeeByEmail(String email) {
        String sql = "SELECT * FROM employees WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("[EmployeeDAO] getEmployeeByEmail error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Validates login credentials.
     * Returns the Employee object if credentials match, otherwise null.
     */
    public Employee validateLogin(String email, String password) {
        String sql = "SELECT * FROM employees WHERE email = ? AND password = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("[EmployeeDAO] validateLogin error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Inserts a new employee record into the database.
     * Returns true if the insert was successful.
     */
    public boolean addEmployee(Employee emp) {
        String sql = "INSERT INTO employees (name, email, password, department, role, salary) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, emp.getName());
            ps.setString(2, emp.getEmail());
            ps.setString(3, emp.getPassword());
            ps.setString(4, emp.getDepartment());
            ps.setString(5, emp.getRole() != null ? emp.getRole() : "employee");
            ps.setDouble(6, emp.getSalary());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[EmployeeDAO] addEmployee error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Updates an existing employee record.
     * Returns true if the update was successful.
     */
    public boolean updateEmployee(Employee emp) {
        String sql = "UPDATE employees SET name=?, email=?, department=?, role=?, salary=? WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, emp.getName());
            ps.setString(2, emp.getEmail());
            ps.setString(3, emp.getDepartment());
            ps.setString(4, emp.getRole());
            ps.setDouble(5, emp.getSalary());
            ps.setInt(6, emp.getId());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[EmployeeDAO] updateEmployee error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Deletes an employee by their ID.
     * Returns true if the deletion was successful.
     */
    public boolean deleteEmployee(int id) {
        String sql = "DELETE FROM employees WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[EmployeeDAO] deleteEmployee error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Returns the count of all non-admin employees.
     */
    public int getEmployeeCount() {
        String sql = "SELECT COUNT(*) FROM employees WHERE role = 'employee'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) return rs.getInt(1);

        } catch (SQLException e) {
            System.err.println("[EmployeeDAO] getEmployeeCount error: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Returns total salary expenditure across all employees.
     */
    public double getTotalSalary() {
        String sql = "SELECT SUM(salary) FROM employees WHERE role = 'employee'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) return rs.getDouble(1);

        } catch (SQLException e) {
            System.err.println("[EmployeeDAO] getTotalSalary error: " + e.getMessage());
        }
        return 0.0;
    }

    /**
     * Maps a ResultSet row to an Employee object.
     * Centralises field extraction to avoid repetition.
     */
    private Employee mapRow(ResultSet rs) throws SQLException {
        return new Employee(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getString("email"),
            rs.getString("password"),
            rs.getString("department"),
            rs.getString("role"),
            rs.getDouble("salary")
        );
    }
}
