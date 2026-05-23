package com.hrms.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Singleton database connection manager for SQLite.
 * Automatically creates all required tables and seeds initial admin data
 * on the very first connection.
 */
public class DBConnection {

    // SQLite database file path (relative to Tomcat working directory)
    private static final String DB_URL = "jdbc:sqlite:hrms.db";

    // Single shared connection instance
    private static Connection connection = null;

    /**
     * Returns the singleton SQLite Connection.
     * Creates tables and seeds data if the DB is newly created.
     */
    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                // Load the SQLite JDBC driver
                Class.forName("org.sqlite.JDBC");
                connection = DriverManager.getConnection(DB_URL);

                // Enable foreign key support in SQLite
                connection.createStatement().execute("PRAGMA foreign_keys = ON");

                // Create all tables and seed initial data
                initDatabase(connection);
            }
        } catch (ClassNotFoundException e) {
            System.err.println("[DBConnection] SQLite JDBC driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("[DBConnection] SQL error: " + e.getMessage());
        }
        return connection;
    }

    /**
     * Creates tables and inserts seed data if they do not already exist.
     */
    private static void initDatabase(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();

        // ---- Create Tables ----

        // employees table
        stmt.execute(
            "CREATE TABLE IF NOT EXISTS employees (" +
            "  id         INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  name       TEXT    NOT NULL," +
            "  email      TEXT    UNIQUE NOT NULL," +
            "  password   TEXT    NOT NULL," +
            "  department TEXT," +
            "  role       TEXT    DEFAULT 'employee'," +  // 'admin' or 'employee'
            "  salary     REAL    DEFAULT 0.0" +
            ")"
        );

        // attendance table
        stmt.execute(
            "CREATE TABLE IF NOT EXISTS attendance (" +
            "  id      INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  emp_id  INTEGER NOT NULL," +
            "  date    TEXT    NOT NULL," +
            "  status  TEXT    NOT NULL," +              // 'Present' or 'Absent'
            "  FOREIGN KEY(emp_id) REFERENCES employees(id) ON DELETE CASCADE" +
            ")"
        );

        // leaves table
        stmt.execute(
            "CREATE TABLE IF NOT EXISTS leaves (" +
            "  id        INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  emp_id    INTEGER NOT NULL," +
            "  reason    TEXT," +
            "  status    TEXT    DEFAULT 'Pending'," +   // 'Pending','Approved','Rejected'
            "  from_date TEXT," +
            "  to_date   TEXT," +
            "  FOREIGN KEY(emp_id) REFERENCES employees(id) ON DELETE CASCADE" +
            ")"
        );

        // payroll table
        stmt.execute(
            "CREATE TABLE IF NOT EXISTS payroll (" +
            "  id          INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  emp_id      INTEGER UNIQUE NOT NULL," +
            "  basic       REAL    DEFAULT 0.0," +
            "  bonus       REAL    DEFAULT 0.0," +
            "  deductions  REAL    DEFAULT 0.0," +
            "  total       REAL    DEFAULT 0.0," +
            "  FOREIGN KEY(emp_id) REFERENCES employees(id) ON DELETE CASCADE" +
            ")"
        );

        // ---- Seed Admin User (only if table is empty) ----
        stmt.execute(
            "INSERT OR IGNORE INTO employees (name, email, password, department, role, salary) " +
            "VALUES ('Admin User', 'admin@hrms.com', 'admin123', 'Management', 'admin', 0.0)"
        );

        // ---- Seed Sample Employees (Commented out for clean state) ----
        /*
        stmt.execute(
            "INSERT OR IGNORE INTO employees (name, email, password, department, role, salary) " +
            "VALUES ('Alice Johnson', 'alice@hrms.com', 'alice123', 'Engineering', 'employee', 75000.0)"
        );
        stmt.execute(
            "INSERT OR IGNORE INTO employees (name, email, password, department, role, salary) " +
            "VALUES ('Bob Smith', 'bob@hrms.com', 'bob123', 'Marketing', 'employee', 65000.0)"
        );
        stmt.execute(
            "INSERT OR IGNORE INTO employees (name, email, password, department, role, salary) " +
            "VALUES ('Carol White', 'carol@hrms.com', 'carol123', 'HR', 'employee', 60000.0)"
        );

        // ---- Seed Sample Payroll Records ----
        stmt.execute(
            "INSERT OR IGNORE INTO payroll (emp_id, basic, bonus, deductions, total) " +
            "SELECT id, 75000, 5000, 3000, 77000 FROM employees WHERE email='alice@hrms.com'"
        );
        stmt.execute(
            "INSERT OR IGNORE INTO payroll (emp_id, basic, bonus, deductions, total) " +
            "SELECT id, 65000, 3000, 2500, 65500 FROM employees WHERE email='bob@hrms.com'"
        );
        stmt.execute(
            "INSERT OR IGNORE INTO payroll (emp_id, basic, bonus, deductions, total) " +
            "SELECT id, 60000, 2000, 2000, 60000 FROM employees WHERE email='carol@hrms.com'"
        );
        */

        stmt.close();
        System.out.println("[DBConnection] Database initialized successfully.");
    }
}
