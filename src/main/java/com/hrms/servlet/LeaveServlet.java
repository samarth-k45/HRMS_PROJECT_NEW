package com.hrms.servlet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hrms.dao.LeaveDAO;
import com.hrms.model.Leave;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * LeaveServlet handles leave operations via /api/leaves.
 *
 * GET  /api/leaves                        → list (all for admin, own for employee)
 * POST /api/leaves                        → apply for leave (employee)
 * POST /api/leaves?_method=PUT            → approve/reject (admin)
 */
public class LeaveServlet extends HttpServlet {

    private final LeaveDAO leaveDAO = new LeaveDAO();
    private final Gson gson = new Gson();

    /** GET: retrieve leave records */
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

        List<Leave> list = "admin".equals(role)
            ? leaveDAO.getAllLeaves()
            : leaveDAO.getLeavesByEmp(empId);

        out.print(gson.toJson(list));
    }

    /** POST: apply leave or update leave status */
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

        String role  = (String) session.getAttribute("role");
        int    empId = (int)    session.getAttribute("empId");
        String method = req.getParameter("_method");
        JsonObject result = new JsonObject();

        if ("PUT".equalsIgnoreCase(method) && "admin".equals(role)) {
            // --- Admin: Approve or Reject a leave ---
            int    leaveId    = parseInt(req.getParameter("leaveId"), 0);
            String status     = req.getParameter("status"); // 'Approved' or 'Rejected'

            if (leaveId == 0 || status == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                result.addProperty("success", false);
                result.addProperty("message", "leaveId and status are required.");
            } else {
                boolean ok = leaveDAO.updateLeaveStatus(leaveId, status);
                result.addProperty("success", ok);
                result.addProperty("message", ok
                    ? "Leave " + status.toLowerCase() + "."
                    : "Failed to update leave status.");
            }

        } else {
            // --- Employee: Apply for leave ---
            String reason   = req.getParameter("reason");
            String fromDate = req.getParameter("fromDate");
            String toDate   = req.getParameter("toDate");

            if (reason == null || fromDate == null || toDate == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                result.addProperty("success", false);
                result.addProperty("message", "reason, fromDate and toDate are required.");
            } else {
                Leave leave = new Leave();
                leave.setEmpId(empId);
                leave.setReason(reason);
                leave.setFromDate(fromDate);
                leave.setToDate(toDate);
                boolean ok = leaveDAO.applyLeave(leave);
                result.addProperty("success", ok);
                result.addProperty("message", ok ? "Leave applied successfully." : "Failed to apply leave.");
            }
        }

        out.print(gson.toJson(result));
    }

    private int parseInt(String s, int def) {
        try { return Integer.parseInt(s); } catch (Exception e) { return def; }
    }
}
