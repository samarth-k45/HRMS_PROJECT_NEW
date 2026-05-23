package com.hrms.servlet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hrms.dao.EmployeeDAO;
import com.hrms.model.Employee;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * LoginServlet handles POST /api/login and POST /api/logout.
 *
 * POST /api/login  → Validates credentials and starts a session.
 * POST /api/logout → Invalidates the current session.
 *
 * JSON Request Body (login):
 *   { "email": "...", "password": "..." }
 *
 * JSON Response (login success):
 *   { "success": true, "role": "admin|employee", "name": "...", "empId": 1 }
 */
public class LoginServlet extends HttpServlet {

    private final EmployeeDAO employeeDAO = new EmployeeDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Set response content type to JSON
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        // Check the action parameter to distinguish login vs logout
        String action = req.getParameter("action");

        if ("logout".equalsIgnoreCase(action)) {
            // --- Logout: Invalidate session ---
            HttpSession session = req.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            JsonObject result = new JsonObject();
            result.addProperty("success", true);
            result.addProperty("message", "Logged out successfully.");
            out.print(gson.toJson(result));
            return;
        }

        if ("register".equalsIgnoreCase(action)) {
            // --- Register: Create new employee account ---
            String name       = req.getParameter("name");
            String email      = req.getParameter("email");
            String password   = req.getParameter("password");
            String department = req.getParameter("department");

            JsonObject result = new JsonObject();

            if (name == null || email == null || password == null || name.isEmpty() || email.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                result.addProperty("success", false);
                result.addProperty("message", "All fields are required.");
                out.print(gson.toJson(result));
                return;
            }

            Employee emp = new Employee();
            emp.setName(name);
            emp.setEmail(email);
            emp.setPassword(password);
            emp.setDepartment(department);
            emp.setRole("employee");
            emp.setSalary(0.0);

            boolean ok = employeeDAO.addEmployee(emp);
            result.addProperty("success", ok);
            result.addProperty("message", ok ? "Registration successful! You can now log in." : "Registration failed. Email might already be in use.");
            out.print(gson.toJson(result));
            return;
        }

        // --- Login: Validate credentials ---
        String email    = req.getParameter("email");
        String password = req.getParameter("password");

        JsonObject result = new JsonObject();

        // Basic input validation
        if (email == null || password == null || email.isEmpty() || password.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            result.addProperty("success", false);
            result.addProperty("message", "Email and password are required.");
            out.print(gson.toJson(result));
            return;
        }

        // Query database for matching credentials
        Employee emp = employeeDAO.validateLogin(email.trim(), password.trim());

        if (emp != null) {
            // Credentials are valid — create a session and store user info
            HttpSession session = req.getSession(true);
            session.setAttribute("empId",   emp.getId());
            session.setAttribute("empName", emp.getName());
            session.setAttribute("role",    emp.getRole());

            result.addProperty("success", true);
            result.addProperty("role",    emp.getRole());
            result.addProperty("name",    emp.getName());
            result.addProperty("empId",   emp.getId());
        } else {
            // Invalid credentials
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            result.addProperty("success", false);
            result.addProperty("message", "Invalid email or password.");
        }

        out.print(gson.toJson(result));
    }

    /**
     * GET /api/login → Returns the current session user info.
     * Useful for the frontend to check if a session is still active.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();
        JsonObject result = new JsonObject();

        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("empId") != null) {
            result.addProperty("loggedIn", true);
            result.addProperty("empId",    (int) session.getAttribute("empId"));
            result.addProperty("empName",  (String) session.getAttribute("empName"));
            result.addProperty("role",     (String) session.getAttribute("role"));
        } else {
            result.addProperty("loggedIn", false);
        }
        out.print(gson.toJson(result));
    }
}
