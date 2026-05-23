package com.hrms.servlet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hrms.dao.PayrollDAO;
import com.hrms.model.Payroll;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * PayrollServlet handles payroll operations via /api/payroll.
 *
 * GET  /api/payroll → list (all for admin, own payslip for employee)
 * POST /api/payroll → add/update payroll (admin only)
 */
public class PayrollServlet extends HttpServlet {

    private final PayrollDAO payrollDAO = new PayrollDAO();
    private final Gson gson = new Gson();

    /** GET: retrieve payroll records */
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

        if ("admin".equals(role)) {
            List<Payroll> list = payrollDAO.getAllPayroll();
            out.print(gson.toJson(list));
        } else {
            // Employee: return own payslip only
            Payroll p = payrollDAO.getPayrollByEmp(empId);
            out.print(gson.toJson(p));
        }
    }

    /** POST: add or update payroll record (admin only) */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        HttpSession session = req.getSession(false);
        if (session == null || !"admin".equals(session.getAttribute("role"))) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            out.print("{\"error\":\"Forbidden. Admin access only.\"}");
            return;
        }

        JsonObject result = new JsonObject();

        int    empId      = parseInt(req.getParameter("empId"), 0);
        double basic      = parseDouble(req.getParameter("basic"), 0.0);
        double bonus      = parseDouble(req.getParameter("bonus"), 0.0);
        double deductions = parseDouble(req.getParameter("deductions"), 0.0);

        if (empId == 0) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            result.addProperty("success", false);
            result.addProperty("message", "empId is required.");
            out.print(gson.toJson(result));
            return;
        }

        Payroll payroll = new Payroll();
        payroll.setEmpId(empId);
        payroll.setBasic(basic);
        payroll.setBonus(bonus);
        payroll.setDeductions(deductions);
        // total is calculated inside addOrUpdatePayroll()

        boolean ok = payrollDAO.addOrUpdatePayroll(payroll);
        result.addProperty("success", ok);
        result.addProperty("message", ok ? "Payroll saved." : "Failed to save payroll.");
        if (ok) {
            result.addProperty("total", basic + bonus - deductions);
        }

        out.print(gson.toJson(result));
    }

    private int parseInt(String s, int def) {
        try { return Integer.parseInt(s); } catch (Exception e) { return def; }
    }

    private double parseDouble(String s, double def) {
        try { return Double.parseDouble(s); } catch (Exception e) { return def; }
    }
}
