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
import java.util.List;

/**
 * EmployeeServlet handles CRUD operations on employees via /api/employees.
 *
 * GET    /api/employees          → list all employees (admin) or self (employee)
 * POST   /api/employees          → add a new employee (admin only)
 * POST   /api/employees?_method=PUT    → update employee (admin only)
 * POST   /api/employees?_method=DELETE → delete employee (admin only)
 */
public class EmployeeServlet extends HttpServlet {

    private final EmployeeDAO employeeDAO = new EmployeeDAO();
    private final Gson gson = new Gson();

    /** GET: retrieve employees */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        // Session check
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("empId") == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print("{\"error\":\"Unauthorized\"}");
            return;
        }

        String role  = (String) session.getAttribute("role");
        int    empId = (int)    session.getAttribute("empId");

        if ("admin".equals(role)) {
            // Admin: return full employee list
            List<Employee> list = employeeDAO.getAllEmployees();
            // Mask passwords before sending
            list.forEach(e -> e.setPassword(""));
            out.print(gson.toJson(list));
        } else {
            // Employee: return only own profile
            Employee emp = employeeDAO.getEmployeeById(empId);
            if (emp != null) emp.setPassword("");
            out.print(gson.toJson(emp));
        }
    }

    /** POST: add, update, or delete based on _method parameter */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        // Session + role check — only admins can modify employees
        HttpSession session = req.getSession(false);
        if (session == null || !"admin".equals(session.getAttribute("role"))) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            out.print("{\"error\":\"Forbidden\"}");
            return;
        }

        // Use _method param to simulate PUT / DELETE over POST (HTML form limitation)
        String method = req.getParameter("_method");
        if (method == null) method = "";
        
        System.out.println("[EmployeeServlet] Method: " + method + ", ID: " + req.getParameter("id"));

        JsonObject result = new JsonObject();

        if ("DELETE".equalsIgnoreCase(method)) {
            // --- DELETE employee ---
            String idStr = req.getParameter("id");
            int id = parseInt(idStr, 0);
            System.out.println("[EmployeeServlet] Deleting ID: " + id);
            boolean ok = employeeDAO.deleteEmployee(id);
            result.addProperty("success", ok);
            result.addProperty("message", ok ? "Employee deleted." : "Delete failed.");

        } else if ("PUT".equalsIgnoreCase(method)) {
            // --- UPDATE employee ---
            int id = parseInt(req.getParameter("id"), 0);
            System.out.println("[EmployeeServlet] Updating ID: " + id);
            Employee emp = buildFromRequest(req);
            emp.setId(id);
            boolean ok = employeeDAO.updateEmployee(emp);
            result.addProperty("success", ok);
            result.addProperty("message", ok ? "Employee updated." : "Update failed.");

        } else {
            // --- ADD employee ---
            System.out.println("[EmployeeServlet] Adding new employee");
            Employee emp = buildFromRequest(req);
            // password is required for new employees
            emp.setPassword(req.getParameter("password"));
            boolean ok = employeeDAO.addEmployee(emp);
            result.addProperty("success", ok);
            result.addProperty("message", ok ? "Employee added." : "Add failed (email may already exist).");
        }

        out.print(gson.toJson(result));
    }

    /** Builds an Employee from HTTP request parameters. */
    private Employee buildFromRequest(HttpServletRequest req) {
        Employee emp = new Employee();
        emp.setName(req.getParameter("name"));
        emp.setEmail(req.getParameter("email"));
        emp.setDepartment(req.getParameter("department"));
        emp.setRole(req.getParameter("role") != null ? req.getParameter("role") : "employee");
        emp.setSalary(parseDouble(req.getParameter("salary"), 0.0));
        return emp;
    }

    private int parseInt(String s, int def) {
        try { return Integer.parseInt(s); } catch (Exception e) { return def; }
    }

    private double parseDouble(String s, double def) {
        try { return Double.parseDouble(s); } catch (Exception e) { return def; }
    }
}
