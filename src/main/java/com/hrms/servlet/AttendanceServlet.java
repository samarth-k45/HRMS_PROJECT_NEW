package com.hrms.servlet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.hrms.dao.AttendanceDAO;
import com.hrms.dao.EmployeeDAO;
import com.hrms.model.Attendance;
import com.hrms.model.Employee;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.List;

/**
 * AttendanceServlet handles attendance operations via /api/attendance.
 *
 * GET  /api/attendance → list records (all for admin, own for employee)
 * POST /api/attendance → mark attendance for today
 */
public class AttendanceServlet extends HttpServlet {

    private final AttendanceDAO attendanceDAO = new AttendanceDAO();
    private final EmployeeDAO employeeDAO = new EmployeeDAO();
    private final Gson gson = new Gson();

    /** GET: retrieve attendance records */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("empId") == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print("{\"error\":\"Unauthorized\"}");
            return;
        }

        String role  = (String) session.getAttribute("role");
        int    empId = (int)    session.getAttribute("empId");
        String today = LocalDate.now().toString();

        if ("admin".equals(role)) {
            List<Attendance> list = attendanceDAO.getAllAttendance();
            // Also include employee list for the dropdown
            List<Employee> employees = employeeDAO.getAllEmployees();
            JsonArray empArray = new JsonArray();
            for (Employee emp : employees) {
                JsonObject ej = new JsonObject();
                ej.addProperty("id", emp.getId());
                ej.addProperty("name", emp.getName());
                empArray.add(ej);
            }
            JsonObject wrapper = new JsonObject();
            wrapper.add("records", gson.toJsonTree(list));
            wrapper.add("employees", empArray);
            out.print(gson.toJson(wrapper));
        } else {
            List<Attendance> list = attendanceDAO.getAttendanceByEmp(empId);
            JsonObject wrapper = new JsonObject();
            wrapper.add("records", gson.toJsonTree(list));
            out.print(gson.toJson(wrapper));
        }
    }

    /** POST: mark attendance (admin only) */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("empId") == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print("{\"error\":\"Unauthorized\"}");
            return;
        }

        String role = (String) session.getAttribute("role");
        int sessionEmpId = (int) session.getAttribute("empId");

        String empIdParam = req.getParameter("empId");
        String status = req.getParameter("status");  // 'Present' or 'Absent'
        String date   = LocalDate.now().toString();  // Always use server date

        JsonObject result = new JsonObject();

        // Employees mark their own attendance (use session empId)
        // Admins can mark for any employee (use request param empId)
        if (!"admin".equals(role)) {
            // Employee: always use their own empId
            empIdParam = String.valueOf(sessionEmpId);
        }

        if (empIdParam == null || empIdParam.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            result.addProperty("success", false);
            result.addProperty("message", "Employee ID is required.");
            out.print(gson.toJson(result));
            return;
        }

        if (status == null || (!status.equals("Present") && !status.equals("Absent"))) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            result.addProperty("success", false);
            result.addProperty("message", "Status must be 'Present' or 'Absent'.");
            out.print(gson.toJson(result));
            return;
        }

        int targetEmpId;
        try {
            targetEmpId = Integer.parseInt(empIdParam);
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            result.addProperty("success", false);
            result.addProperty("message", "Invalid Employee ID.");
            out.print(gson.toJson(result));
            return;
        }

        boolean ok = attendanceDAO.markAttendance(targetEmpId, date, status);
        result.addProperty("success", ok);
        result.addProperty("message", ok
            ? "Attendance marked as " + status + " for " + date
            : "Failed to mark attendance.");
        result.addProperty("date", date);
        result.addProperty("status", status);

        out.print(gson.toJson(result));
    }
}
