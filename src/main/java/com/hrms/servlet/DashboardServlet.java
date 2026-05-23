package com.hrms.servlet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hrms.dao.AttendanceDAO;
import com.hrms.dao.EmployeeDAO;
import com.hrms.dao.LeaveDAO;
import com.hrms.dao.PayrollDAO;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;

/**
 * DashboardServlet handles GET /api/dashboard.
 *
 * Returns summary statistics for the admin dashboard:
 *   - Total employee count
 *   - Total salary expenditure
 *   - Pending leave requests count
 *   - Employees present today
 *
 * Requires an active admin session.
 */
public class DashboardServlet extends HttpServlet {

    private final EmployeeDAO   employeeDAO   = new EmployeeDAO();
    private final AttendanceDAO attendanceDAO = new AttendanceDAO();
    private final LeaveDAO      leaveDAO      = new LeaveDAO();
    private final PayrollDAO    payrollDAO    = new PayrollDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        // Validate session
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("empId") == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            JsonObject err = new JsonObject();
            err.addProperty("error", "Unauthorized. Please log in.");
            out.print(gson.toJson(err));
            return;
        }

        // Get today's date in yyyy-MM-dd format
        String today = LocalDate.now().toString();

        // Build dashboard stats object
        JsonObject stats = new JsonObject();
        stats.addProperty("totalEmployees",  employeeDAO.getEmployeeCount());
        stats.addProperty("totalSalary",     employeeDAO.getTotalSalary());
        stats.addProperty("pendingLeaves",   leaveDAO.getPendingLeaveCount());
        stats.addProperty("presentToday",    attendanceDAO.getPresentCountForDate(today));
        stats.addProperty("totalPayroll",    payrollDAO.getTotalPayroll());

        out.print(gson.toJson(stats));
    }
}
