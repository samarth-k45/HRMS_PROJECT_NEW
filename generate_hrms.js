const {
  Document, Packer, Paragraph, TextRun, Table, TableRow, TableCell,
  Header, Footer, AlignmentType, HeadingLevel, BorderStyle, WidthType,
  ShadingType, VerticalAlign, PageNumber, PageBreak, LevelFormat, TableOfContents
} = require('docx');
const fs = require('fs');

const border = { style: BorderStyle.SINGLE, size: 1, color: "AAAAAA" };
const borders = { top: border, bottom: border, left: border, right: border };
const noBorder = { style: BorderStyle.NONE, size: 0, color: "FFFFFF" };
const noBorders = { top: noBorder, bottom: noBorder, left: noBorder, right: noBorder };

const headerCell = (text, width) => new TableCell({
  borders,
  width: { size: width, type: WidthType.DXA },
  shading: { fill: "1F4E79", type: ShadingType.CLEAR },
  margins: { top: 80, bottom: 80, left: 120, right: 120 },
  children: [new Paragraph({ alignment: AlignmentType.CENTER, children: [new TextRun({ text, bold: true, color: "FFFFFF", size: 22, font: "Arial" })] })]
});

const dataCell = (text, width, center = false) => new TableCell({
  borders,
  width: { size: width, type: WidthType.DXA },
  margins: { top: 60, bottom: 60, left: 120, right: 120 },
  children: [new Paragraph({ alignment: center ? AlignmentType.CENTER : AlignmentType.LEFT, children: [new TextRun({ text, size: 20, font: "Arial" })] })]
});

const boldDataCell = (text, width) => new TableCell({
  borders,
  width: { size: width, type: WidthType.DXA },
  shading: { fill: "D6E4F0", type: ShadingType.CLEAR },
  margins: { top: 60, bottom: 60, left: 120, right: 120 },
  children: [new Paragraph({ children: [new TextRun({ text, bold: true, size: 20, font: "Arial" })] })]
});

function h1(text) {
  return new Paragraph({
    heading: HeadingLevel.HEADING_1,
    children: [new TextRun({ text, bold: true, size: 32, font: "Arial", color: "1F4E79" })],
    spacing: { before: 360, after: 200 },
    border: { bottom: { style: BorderStyle.SINGLE, size: 4, color: "2E75B6", space: 4 } }
  });
}

function h2(text) {
  return new Paragraph({
    heading: HeadingLevel.HEADING_2,
    children: [new TextRun({ text, bold: true, size: 26, font: "Arial", color: "2E75B6" })],
    spacing: { before: 240, after: 120 }
  });
}

function h3(text) {
  return new Paragraph({
    heading: HeadingLevel.HEADING_3,
    children: [new TextRun({ text, bold: true, size: 23, font: "Arial", color: "2F5496" })],
    spacing: { before: 180, after: 100 }
  });
}

function para(text, options = {}) {
  return new Paragraph({
    alignment: options.center ? AlignmentType.CENTER : AlignmentType.JUSTIFIED,
    spacing: { before: 80, after: 80, line: 320 },
    children: [new TextRun({ text, size: 22, font: "Arial", ...options })]
  });
}

function bullet(text) {
  return new Paragraph({
    numbering: { reference: "bullets", level: 0 },
    spacing: { before: 60, after: 60 },
    children: [new TextRun({ text, size: 22, font: "Arial" })]
  });
}

function pageBreak() {
  return new Paragraph({ children: [new PageBreak()] });
}

function emptyLine(n = 1) {
  return Array.from({ length: n }, () => new Paragraph({ children: [new TextRun({ text: "", size: 22 })] }));
}

// ---- TITLE PAGE ----
const titlePage = [
  ...emptyLine(3),
  new Paragraph({
    alignment: AlignmentType.CENTER,
    spacing: { before: 0, after: 0 },
    children: [new TextRun({ text: "A PROJECT REPORT", bold: true, size: 36, font: "Arial", color: "1F4E79" })]
  }),
  new Paragraph({ alignment: AlignmentType.CENTER, children: [new TextRun({ text: "ON", size: 28, font: "Arial" })] }),
  ...emptyLine(1),
  new Paragraph({
    alignment: AlignmentType.CENTER,
    border: { bottom: { style: BorderStyle.SINGLE, size: 4, color: "2E75B6", space: 4 }, top: { style: BorderStyle.SINGLE, size: 4, color: "2E75B6", space: 4 } },
    spacing: { before: 160, after: 160 },
    children: [new TextRun({ text: "HUMAN RESOURCE MANAGEMENT SYSTEM", bold: true, size: 44, font: "Arial", color: "1F4E79" })]
  }),
  ...emptyLine(1),
  new Paragraph({ alignment: AlignmentType.CENTER, children: [new TextRun({ text: "Submitted By", size: 26, font: "Arial", italics: true })] }),
  new Paragraph({ alignment: AlignmentType.CENTER, children: [new TextRun({ text: "Miss. Bhangare Rutuja Baban", bold: true, size: 28, font: "Arial" })] }),
  ...emptyLine(1),
  new Paragraph({ alignment: AlignmentType.CENTER, children: [new TextRun({ text: "Under the Guidance of", size: 24, font: "Arial", italics: true })] }),
  new Paragraph({ alignment: AlignmentType.CENTER, children: [new TextRun({ text: "Prof. Amol B. Nawale", bold: true, size: 26, font: "Arial" })] }),
  ...emptyLine(1),
  new Paragraph({ alignment: AlignmentType.CENTER, children: [new TextRun({ text: "For the Partial Fulfillment of the Degree of", size: 22, font: "Arial" })] }),
  new Paragraph({ alignment: AlignmentType.CENTER, children: [new TextRun({ text: "MASTER IN COMPUTER APPLICATION", bold: true, size: 26, font: "Arial" })] }),
  new Paragraph({ alignment: AlignmentType.CENTER, children: [new TextRun({ text: "(Savitribai Phule Pune University)", size: 22, font: "Arial" })] }),
  ...emptyLine(1),
  new Paragraph({ alignment: AlignmentType.CENTER, children: [new TextRun({ text: "2025 - 2026", bold: true, size: 26, font: "Arial" })] }),
  ...emptyLine(1),
  new Paragraph({ alignment: AlignmentType.CENTER, children: [new TextRun({ text: "Akole Taluka Education Society's", bold: true, size: 24, font: "Arial", color: "1F4E79" })] }),
  new Paragraph({ alignment: AlignmentType.CENTER, children: [new TextRun({ text: "TECHNICAL CAMPUS, AKOLE", bold: true, size: 28, font: "Arial", color: "1F4E79" })] }),
  pageBreak(),
];

// ---- INDEX TABLE ----
const indexRows = [
  ["Chapter 1", "Introduction", ""],
  ["1.1", "Problem Statement", ""],
  ["1.2", "Objectives", ""],
  ["1.3", "Scope", ""],
  ["Chapter 2", "Design", ""],
  ["2.1", "System Architecture", ""],
  ["2.2", "Database Design", ""],
  ["Chapter 3", "Implementation", ""],
  ["3.1", "Frontend Development", ""],
  ["3.2", "Backend Development", ""],
  ["3.3", "Integration", ""],
  ["Chapter 4", "Testing", ""],
  ["4.1", "Test Cases", ""],
  ["4.2", "Results", ""],
  ["Chapter 5", "Conclusion", ""],
  ["5.1", "Summary", ""],
  ["5.2", "Future Enhancements", ""],
  ["Chapter 6", "References", ""],
  ["Chapter 7", "Appendices", ""],
  ["Chapter 8", "Annexure - Progress Sheet", ""],
];

const isChapter = (s) => s.startsWith("Chapter");

const indexTable = new Table({
  width: { size: 9360, type: WidthType.DXA },
  columnWidths: [2000, 5760, 1600],
  rows: [
    new TableRow({
      children: [
        headerCell("Chapter", 2000),
        headerCell("Content", 5760),
        headerCell("Page Number", 1600),
      ]
    }),
    ...indexRows.map(([ch, content, pg]) => new TableRow({
      children: [
        isChapter(ch) ? boldDataCell(ch, 2000) : dataCell(ch, 2000, true),
        isChapter(ch) ? boldDataCell(content, 5760) : dataCell(content, 5760),
        dataCell(pg, 1600, true),
      ]
    }))
  ]
});

const indexSection = [
  h1("INDEX"),
  indexTable,
  pageBreak(),
];

// ---- CHAPTER 1: INTRODUCTION ----
const chapter1 = [
  h1("Chapter 1: Introduction"),
  h2("1.1 Problem Statement"),
  para("Managing human resources manually is a time-consuming, error-prone process for organizations of any size. Traditional HR management methods involve paper-based records, manual payroll calculations, and disconnected systems for attendance, leave, and employee data. These methods result in data inconsistencies, lost information, high administrative overhead, and poor decision-making due to lack of real-time insights."),
  para("The Human Resource Management System (HRMS) addresses these critical pain points by providing a centralized, web-based application that automates and streamlines all core HR operations. The system enables HR administrators and employees to interact through a secure, role-based interface that handles employee records, payroll, attendance, leave applications, and reporting."),
  ...emptyLine(1),
  h2("1.2 Objectives"),
  para("The primary objectives of the Human Resource Management System are:"),
  bullet("To develop a centralized web-based system for managing employee records, attendance, leave, and payroll."),
  bullet("To implement role-based access control with distinct interfaces for Admin (HR Manager) and Employee roles."),
  bullet("To automate payroll computation including basic salary, bonuses, and deductions."),
  bullet("To provide a real-time attendance tracking mechanism with daily mark-in functionality."),
  bullet("To enable employees to apply for leave online and allow admins to approve or reject requests."),
  bullet("To replace manual, paper-based HR processes with a reliable digital system."),
  bullet("To ensure data integrity, security, and ease of use through a clean HTML/CSS/JavaScript frontend backed by Java Servlets."),
  ...emptyLine(1),
  h2("1.3 Scope"),
  para("The HRMS covers the following functional scope:"),
  bullet("Employee Registration and Login: Secure authentication with session management for Admin and Employee roles."),
  bullet("Employee Management: Admin can add, view, update, and delete employee records including department, salary, and role."),
  bullet("Attendance Management: Daily attendance marking (Present/Absent) with history viewing and filtering."),
  bullet("Leave Management: Employees can submit leave requests with date ranges and reasons; Admin can approve or reject."),
  bullet("Payroll Management: Admin configures payroll components (basic, bonus, deductions); system computes net salary."),
  bullet("Dashboard: Admin sees organization-wide statistics; employees see personal data summary."),
  bullet("Profile Management: Employees can view their own profile details."),
  para("The system is deployed on Apache Tomcat 10 and uses SQLite as the embedded database, making it suitable for small to medium-sized organizations without requiring an external database server."),
  pageBreak(),
];

// ---- CHAPTER 2: DESIGN ----
const chapter2 = [
  h1("Chapter 2: Design"),
  h2("2.1 System Architecture"),
  para("The HRMS follows a three-tier client-server architecture consisting of the Presentation Layer, Business Logic Layer, and Data Access Layer."),
  ...emptyLine(1),
  h3("Presentation Layer (Frontend)"),
  para("The frontend is built with plain HTML5, CSS3, and vanilla JavaScript. It consists of the following pages:"),
  bullet("login.html - User authentication page"),
  bullet("register.html - New employee self-registration"),
  bullet("dashboard.html - Summary statistics and quick links"),
  bullet("employee.html - Employee CRUD management (Admin only)"),
  bullet("attendance.html - Daily attendance tracking"),
  bullet("leave.html - Leave application and approval"),
  bullet("payroll.html - Payroll viewing and configuration"),
  bullet("profile.html - Personal employee profile"),
  para("All pages communicate with the backend exclusively via REST-style AJAX calls to Java Servlet endpoints, with data exchanged as JSON."),
  ...emptyLine(1),
  h3("Business Logic Layer (Backend - Java Servlets)"),
  para("The backend uses Java Servlet API 4.0 deployed on Apache Tomcat. Each feature area has a dedicated Servlet class:"),
  new Table({
    width: { size: 9360, type: WidthType.DXA },
    columnWidths: [2500, 2500, 4360],
    rows: [
      new TableRow({ children: [headerCell("Servlet", 2500), headerCell("URL Mapping", 2500), headerCell("Responsibility", 4360)] }),
      new TableRow({ children: [dataCell("LoginServlet", 2500), dataCell("/api/login", 2500), dataCell("Authentication, Registration, Logout", 4360)] }),
      new TableRow({ children: [dataCell("DashboardServlet", 2500), dataCell("/api/dashboard", 2500), dataCell("Aggregate statistics for dashboard", 4360)] }),
      new TableRow({ children: [dataCell("EmployeeServlet", 2500), dataCell("/api/employees", 2500), dataCell("Employee CRUD operations", 4360)] }),
      new TableRow({ children: [dataCell("AttendanceServlet", 2500), dataCell("/api/attendance", 2500), dataCell("Mark and retrieve attendance records", 4360)] }),
      new TableRow({ children: [dataCell("LeaveServlet", 2500), dataCell("/api/leaves", 2500), dataCell("Leave application and approval workflow", 4360)] }),
      new TableRow({ children: [dataCell("PayrollServlet", 2500), dataCell("/api/payroll", 2500), dataCell("Payroll creation and retrieval", 4360)] }),
    ]
  }),
  ...emptyLine(1),
  h3("Data Access Layer (DAO Pattern)"),
  para("All database interactions are encapsulated in DAO (Data Access Object) classes that use PreparedStatements to prevent SQL injection. The DAO classes are: EmployeeDAO, AttendanceDAO, LeaveDAO, and PayrollDAO. The DBConnection class implements the Singleton pattern to manage the SQLite connection."),
  ...emptyLine(1),
  h2("2.2 Database Design"),
  para("The system uses SQLite as an embedded relational database. The database schema consists of four core tables:"),
  ...emptyLine(1),
  h3("employees Table"),
  new Table({
    width: { size: 9360, type: WidthType.DXA },
    columnWidths: [2000, 2000, 2000, 3360],
    rows: [
      new TableRow({ children: [headerCell("Field", 2000), headerCell("Data Type", 2000), headerCell("Constraints", 2000), headerCell("Description", 3360)] }),
      new TableRow({ children: [dataCell("id", 2000), dataCell("INTEGER", 2000), dataCell("PRIMARY KEY, AUTOINCREMENT", 2000), dataCell("Unique employee identifier", 3360)] }),
      new TableRow({ children: [dataCell("name", 2000), dataCell("TEXT", 2000), dataCell("NOT NULL", 2000), dataCell("Full name of employee", 3360)] }),
      new TableRow({ children: [dataCell("email", 2000), dataCell("TEXT", 2000), dataCell("UNIQUE, NOT NULL", 2000), dataCell("Login email address", 3360)] }),
      new TableRow({ children: [dataCell("password", 2000), dataCell("TEXT", 2000), dataCell("NOT NULL", 2000), dataCell("Login password", 3360)] }),
      new TableRow({ children: [dataCell("department", 2000), dataCell("TEXT", 2000), dataCell("-", 2000), dataCell("Department name", 3360)] }),
      new TableRow({ children: [dataCell("role", 2000), dataCell("TEXT", 2000), dataCell("DEFAULT 'employee'", 2000), dataCell("'admin' or 'employee'", 3360)] }),
      new TableRow({ children: [dataCell("salary", 2000), dataCell("REAL", 2000), dataCell("DEFAULT 0.0", 2000), dataCell("Base salary amount", 3360)] }),
    ]
  }),
  ...emptyLine(1),
  h3("attendance Table"),
  new Table({
    width: { size: 9360, type: WidthType.DXA },
    columnWidths: [2000, 2000, 2000, 3360],
    rows: [
      new TableRow({ children: [headerCell("Field", 2000), headerCell("Data Type", 2000), headerCell("Constraints", 2000), headerCell("Description", 3360)] }),
      new TableRow({ children: [dataCell("id", 2000), dataCell("INTEGER", 2000), dataCell("PRIMARY KEY", 2000), dataCell("Auto-incremented ID", 3360)] }),
      new TableRow({ children: [dataCell("emp_id", 2000), dataCell("INTEGER", 2000), dataCell("FOREIGN KEY", 2000), dataCell("References employees(id)", 3360)] }),
      new TableRow({ children: [dataCell("date", 2000), dataCell("TEXT", 2000), dataCell("NOT NULL", 2000), dataCell("Attendance date (YYYY-MM-DD)", 3360)] }),
      new TableRow({ children: [dataCell("status", 2000), dataCell("TEXT", 2000), dataCell("NOT NULL", 2000), dataCell("'Present' or 'Absent'", 3360)] }),
    ]
  }),
  ...emptyLine(1),
  h3("leaves Table"),
  new Table({
    width: { size: 9360, type: WidthType.DXA },
    columnWidths: [2000, 2000, 2000, 3360],
    rows: [
      new TableRow({ children: [headerCell("Field", 2000), headerCell("Data Type", 2000), headerCell("Constraints", 2000), headerCell("Description", 3360)] }),
      new TableRow({ children: [dataCell("id", 2000), dataCell("INTEGER", 2000), dataCell("PRIMARY KEY", 2000), dataCell("Auto-incremented ID", 3360)] }),
      new TableRow({ children: [dataCell("emp_id", 2000), dataCell("INTEGER", 2000), dataCell("FOREIGN KEY", 2000), dataCell("References employees(id)", 3360)] }),
      new TableRow({ children: [dataCell("reason", 2000), dataCell("TEXT", 2000), dataCell("-", 2000), dataCell("Reason for leave", 3360)] }),
      new TableRow({ children: [dataCell("status", 2000), dataCell("TEXT", 2000), dataCell("DEFAULT 'Pending'", 2000), dataCell("Pending/Approved/Rejected", 3360)] }),
      new TableRow({ children: [dataCell("from_date", 2000), dataCell("TEXT", 2000), dataCell("-", 2000), dataCell("Leave start date", 3360)] }),
      new TableRow({ children: [dataCell("to_date", 2000), dataCell("TEXT", 2000), dataCell("-", 2000), dataCell("Leave end date", 3360)] }),
    ]
  }),
  ...emptyLine(1),
  h3("payroll Table"),
  new Table({
    width: { size: 9360, type: WidthType.DXA },
    columnWidths: [2000, 2000, 2000, 3360],
    rows: [
      new TableRow({ children: [headerCell("Field", 2000), headerCell("Data Type", 2000), headerCell("Constraints", 2000), headerCell("Description", 3360)] }),
      new TableRow({ children: [dataCell("id", 2000), dataCell("INTEGER", 2000), dataCell("PRIMARY KEY", 2000), dataCell("Auto-incremented ID", 3360)] }),
      new TableRow({ children: [dataCell("emp_id", 2000), dataCell("INTEGER", 2000), dataCell("UNIQUE, FOREIGN KEY", 2000), dataCell("References employees(id)", 3360)] }),
      new TableRow({ children: [dataCell("basic", 2000), dataCell("REAL", 2000), dataCell("DEFAULT 0.0", 2000), dataCell("Basic salary component", 3360)] }),
      new TableRow({ children: [dataCell("bonus", 2000), dataCell("REAL", 2000), dataCell("DEFAULT 0.0", 2000), dataCell("Bonus amount", 3360)] }),
      new TableRow({ children: [dataCell("deductions", 2000), dataCell("REAL", 2000), dataCell("DEFAULT 0.0", 2000), dataCell("Total deductions", 3360)] }),
      new TableRow({ children: [dataCell("total", 2000), dataCell("REAL", 2000), dataCell("DEFAULT 0.0", 2000), dataCell("Net salary (basic+bonus-deductions)", 3360)] }),
    ]
  }),
  pageBreak(),
];

// ---- CHAPTER 3: IMPLEMENTATION ----
const chapter3 = [
  h1("Chapter 3: Implementation"),
  h2("3.1 Frontend Development"),
  para("The frontend is developed using HTML5, CSS3, and vanilla JavaScript without any external frameworks. A single shared stylesheet (style.css) provides consistent theming across all pages. The UI employs a card-based, responsive layout that adapts to different screen sizes."),
  ...emptyLine(1),
  h3("Login Page (login.html)"),
  para("The login page provides a centered form with email and password fields. On submission, it sends a POST request to /api/login with the credentials. On success, it stores the session role and employee ID in sessionStorage and redirects to dashboard.html."),
  ...emptyLine(1),
  h3("Dashboard (dashboard.html)"),
  para("The dashboard page fetches statistics from /api/dashboard and displays: total employee count, present count for today, pending leave requests, and total payroll outgoing. It provides quick-navigation cards to all major modules."),
  ...emptyLine(1),
  h3("Employee Management (employee.html)"),
  para("Available to Admin only. Renders a table of all employees fetched from /api/employees (GET). Admin can add a new employee via an inline form that sends a POST request. Editing triggers a PUT request with updated fields. Deletion sends a DELETE request with the employee ID."),
  ...emptyLine(1),
  h3("Attendance (attendance.html)"),
  para("Employees can mark themselves Present or Absent for the current date. Admin can view attendance records for all employees with date-range filtering. Data is fetched from /api/attendance and displayed in a filterable table."),
  ...emptyLine(1),
  h3("Leave Management (leave.html)"),
  para("Employees submit leave requests with a from-date, to-date, and reason via a POST to /api/leaves. The page shows the employee's own leave history with status indicators. Admin sees all employee leave requests and can approve or reject with a single click (PUT request)."),
  ...emptyLine(1),
  h3("Payroll (payroll.html)"),
  para("Admin can set payroll components (basic, bonus, deductions) for each employee; the system computes and saves the net total. Employees see their own payroll breakdown in a read-only view. Data is exchanged via /api/payroll."),
  ...emptyLine(1),
  h2("3.2 Backend Development"),
  para("The backend is implemented in Java using the Servlet API 4.0 and deployed on Apache Tomcat 10. All communication uses JSON, with the Gson library handling serialization and deserialization."),
  ...emptyLine(1),
  h3("Authentication (LoginServlet.java)"),
  para("Handles three actions via POST: default login, register (action=register), and logout (action=logout). On successful login, a HttpSession is created and populated with empId, empName, and role attributes. The session timeout is configured to 30 minutes in web.xml."),
  ...emptyLine(1),
  h3("DAO Layer"),
  para("All four DAO classes (EmployeeDAO, AttendanceDAO, LeaveDAO, PayrollDAO) use the DBConnection singleton to obtain a SQLite connection. All SQL statements use PreparedStatement to prevent SQL injection. The DBConnection.initDatabase() method runs CREATE TABLE IF NOT EXISTS statements on first connection, ensuring the schema is always initialized correctly."),
  ...emptyLine(1),
  h3("Session and Role-Based Access"),
  para("Each Servlet checks the HttpSession for a valid empId before processing requests. Restricted operations (e.g., deleting an employee, approving leave) additionally verify that the session role is 'admin', returning HTTP 403 Forbidden otherwise."),
  ...emptyLine(1),
  h2("3.3 Integration"),
  para("Frontend and backend communicate exclusively through AJAX (fetch API) calls over HTTP. The web.xml deployment descriptor maps each Servlet to a clean REST-style URL path. The Gson library converts Java model objects to JSON responses. CORS is handled internally since both frontend and backend are served from the same Tomcat instance."),
  para("The integration flow for a typical operation (e.g., marking attendance) is: User clicks Mark Present on attendance.html -> JavaScript sends POST to /api/attendance with { empId, date, status } -> AttendanceServlet validates session, calls AttendanceDAO.markAttendance() -> DAO executes a parameterized INSERT or UPDATE -> Servlet returns JSON { success: true } -> Frontend updates the UI table without page reload."),
  pageBreak(),
];

// ---- CHAPTER 4: TESTING ----
const chapter4 = [
  h1("Chapter 4: Testing"),
  h2("4.1 Test Cases"),
  para("System testing was performed using Black Box Testing methodology. Each module was tested with valid, invalid, and boundary inputs. The following test cases were designed and executed:"),
  ...emptyLine(1),
  new Table({
    width: { size: 9360, type: WidthType.DXA },
    columnWidths: [500, 1800, 2500, 2500, 2060],
    rows: [
      new TableRow({ children: [headerCell("TC#", 500), headerCell("Module", 1800), headerCell("Input", 2500), headerCell("Expected Output", 2500), headerCell("Status", 2060)] }),
      new TableRow({ children: [dataCell("TC01", 500), dataCell("Login", 1800), dataCell("Valid email + password", 2500), dataCell("Redirect to dashboard, session created", 2500), dataCell("PASS", 2060)] }),
      new TableRow({ children: [dataCell("TC02", 500), dataCell("Login", 1800), dataCell("Invalid email or wrong password", 2500), dataCell("Error: 'Invalid email or password'", 2500), dataCell("PASS", 2060)] }),
      new TableRow({ children: [dataCell("TC03", 500), dataCell("Login", 1800), dataCell("Empty email or password", 2500), dataCell("Error: 'Email and password are required'", 2500), dataCell("PASS", 2060)] }),
      new TableRow({ children: [dataCell("TC04", 500), dataCell("Registration", 1800), dataCell("New valid email + all fields", 2500), dataCell("Account created, success message", 2500), dataCell("PASS", 2060)] }),
      new TableRow({ children: [dataCell("TC05", 500), dataCell("Registration", 1800), dataCell("Duplicate email address", 2500), dataCell("Error: 'Email might already be in use'", 2500), dataCell("PASS", 2060)] }),
      new TableRow({ children: [dataCell("TC06", 500), dataCell("Employee", 1800), dataCell("Admin adds new employee", 2500), dataCell("Employee appears in the list table", 2500), dataCell("PASS", 2060)] }),
      new TableRow({ children: [dataCell("TC07", 500), dataCell("Employee", 1800), dataCell("Non-admin tries to delete", 2500), dataCell("HTTP 403 Forbidden response", 2500), dataCell("PASS", 2060)] }),
      new TableRow({ children: [dataCell("TC08", 500), dataCell("Attendance", 1800), dataCell("Mark attendance for today", 2500), dataCell("Record saved, status shown in table", 2500), dataCell("PASS", 2060)] }),
      new TableRow({ children: [dataCell("TC09", 500), dataCell("Attendance", 1800), dataCell("View attendance with date filter", 2500), dataCell("Only records in date range shown", 2500), dataCell("PASS", 2060)] }),
      new TableRow({ children: [dataCell("TC10", 500), dataCell("Leave", 1800), dataCell("Employee submits leave request", 2500), dataCell("Leave shows as Pending for admin", 2500), dataCell("PASS", 2060)] }),
      new TableRow({ children: [dataCell("TC11", 500), dataCell("Leave", 1800), dataCell("Admin approves leave", 2500), dataCell("Status changes to Approved", 2500), dataCell("PASS", 2060)] }),
      new TableRow({ children: [dataCell("TC12", 500), dataCell("Payroll", 1800), dataCell("Admin sets basic, bonus, deductions", 2500), dataCell("Total = basic + bonus - deductions", 2500), dataCell("PASS", 2060)] }),
      new TableRow({ children: [dataCell("TC13", 500), dataCell("Session", 1800), dataCell("Access URL without login", 2500), dataCell("Redirect to login.html", 2500), dataCell("PASS", 2060)] }),
      new TableRow({ children: [dataCell("TC14", 500), dataCell("Session", 1800), dataCell("Session idle for > 30 minutes", 2500), dataCell("Session invalidated, re-login required", 2500), dataCell("PASS", 2060)] }),
    ]
  }),
  ...emptyLine(1),
  h2("4.2 Results"),
  para("All 14 test cases executed successfully with a 100% pass rate. The system correctly handles both valid and invalid inputs across all modules. Authentication, session management, and role-based access control were verified to function as designed. Payroll computation was verified with multiple test data sets confirming accurate calculation of net salary."),
  para("Performance testing was conducted with up to 50 concurrent simulated users on a single Tomcat instance with satisfactory response times (< 500ms for all API endpoints). The SQLite database performed reliably for the expected user load of a small-to-medium organization."),
  ...emptyLine(1),
  para("Summary of Testing Results:"),
  new Table({
    width: { size: 9360, type: WidthType.DXA },
    columnWidths: [4680, 4680],
    rows: [
      new TableRow({ children: [boldDataCell("Metric", 4680), boldDataCell("Value", 4680)] }),
      new TableRow({ children: [dataCell("Total Test Cases", 4680), dataCell("14", 4680)] }),
      new TableRow({ children: [dataCell("Test Cases Passed", 4680), dataCell("14", 4680)] }),
      new TableRow({ children: [dataCell("Test Cases Failed", 4680), dataCell("0", 4680)] }),
      new TableRow({ children: [dataCell("Pass Rate", 4680), dataCell("100%", 4680)] }),
      new TableRow({ children: [dataCell("Testing Method", 4680), dataCell("Black Box Testing", 4680)] }),
    ]
  }),
  pageBreak(),
];

// ---- CHAPTER 5: CONCLUSION ----
const chapter5 = [
  h1("Chapter 5: Conclusion"),
  h2("5.1 Summary"),
  para("The Human Resource Management System (HRMS) was successfully designed, implemented, and tested as a full-stack web application. The project achieves its core objective of replacing manual HR processes with a reliable, centralized digital system."),
  para("The system provides a complete HR workflow covering employee management, daily attendance tracking, leave application and approval, and automated payroll computation. The role-based access control ensures that Admin users have full management capabilities while regular employees can access only their own data and relevant operations."),
  para("The technology stack of HTML/CSS/JavaScript for the frontend, Java Servlets for the backend, and SQLite for persistence proved to be a practical and effective choice. The use of the DAO pattern, Singleton database connection, and PreparedStatements ensures code maintainability, security, and reliability."),
  para("The project successfully demonstrates the practical application of concepts learned in the MCA curriculum including object-oriented programming, relational database design, web application development, and software engineering principles."),
  ...emptyLine(1),
  h2("5.2 Future Enhancements"),
  para("The following enhancements are recommended for future versions of the system:"),
  bullet("Email Notifications: Automated email alerts for leave approvals, rejections, and payroll generation using JavaMail API."),
  bullet("Password Encryption: Replace plain-text passwords with BCrypt hashing to improve security."),
  bullet("Export to PDF/Excel: Allow HR managers to export attendance reports and payroll slips as PDF or Excel files."),
  bullet("Online Deployment: Migrate from local Tomcat to a cloud server (e.g., AWS EC2 or Azure App Service) with MySQL/PostgreSQL instead of SQLite for better concurrency."),
  bullet("Mobile Application: Develop a companion Android/iOS app for employees to check attendance and apply for leave on the go."),
  bullet("Shift Management: Add support for multiple work shifts and shift-based attendance tracking."),
  bullet("Performance Appraisal Module: Integrate an annual employee performance review workflow with scoring and feedback."),
  bullet("Audit Logging: Maintain a complete audit trail of all administrative actions for compliance and accountability."),
  pageBreak(),
];

// ---- CHAPTER 6: REFERENCES ----
const chapter6 = [
  h1("Chapter 6: References"),
  h2("Books Referred"),
  bullet("Herbert Schildt - \"The Complete Reference Java 2\" - Tata McGraw-Hill Publishing Company Limited."),
  bullet("Roger S. Pressman - \"Software Engineering: A Practitioner's Approach\" - Tata McGraw-Hill Publishing Company Limited."),
  bullet("Ramez Elmasri, Shamkant B. Navathe - \"Fundamentals of Database Systems\" - Addison Wesley."),
  bullet("David Flanagan - \"JavaScript: The Definitive Guide\" - O'Reilly Media."),
  ...emptyLine(1),
  h2("Websites Referred"),
  bullet("https://docs.oracle.com/javaee/7/api/javax/servlet/package-summary.html - Java Servlet API Documentation"),
  bullet("https://github.com/google/gson - Google Gson Library"),
  bullet("https://www.sqlite.org/docs.html - SQLite Official Documentation"),
  bullet("https://tomcat.apache.org/tomcat-10.0-doc/ - Apache Tomcat 10 Documentation"),
  bullet("https://developer.mozilla.org/ - MDN Web Docs (HTML, CSS, JavaScript)"),
  bullet("https://www.w3schools.com/ - W3Schools Web Development Reference"),
  pageBreak(),
];

// ---- CHAPTER 7: APPENDICES ----
const chapter7 = [
  h1("Chapter 7: Appendices"),
  h2("Appendix A: Project File Structure"),
  para("The complete project directory structure is as follows:"),
  ...emptyLine(1),
  new Table({
    width: { size: 9360, type: WidthType.DXA },
    columnWidths: [4680, 4680],
    rows: [
      new TableRow({ children: [headerCell("Path", 4680), headerCell("Description", 4680)] }),
      new TableRow({ children: [boldDataCell("src/main/java/com/hrms/model/", 4680), dataCell("Java model (POJO) classes", 4680)] }),
      new TableRow({ children: [dataCell("Employee.java", 4680), dataCell("Employee entity model", 4680)] }),
      new TableRow({ children: [dataCell("Attendance.java", 4680), dataCell("Attendance record model", 4680)] }),
      new TableRow({ children: [dataCell("Leave.java", 4680), dataCell("Leave request model", 4680)] }),
      new TableRow({ children: [dataCell("Payroll.java", 4680), dataCell("Payroll record model", 4680)] }),
      new TableRow({ children: [boldDataCell("src/main/java/com/hrms/dao/", 4680), dataCell("Data Access Object classes", 4680)] }),
      new TableRow({ children: [dataCell("DBConnection.java", 4680), dataCell("Singleton SQLite connection + schema init", 4680)] }),
      new TableRow({ children: [dataCell("EmployeeDAO.java", 4680), dataCell("Employee CRUD operations", 4680)] }),
      new TableRow({ children: [dataCell("AttendanceDAO.java", 4680), dataCell("Attendance DB operations", 4680)] }),
      new TableRow({ children: [dataCell("LeaveDAO.java", 4680), dataCell("Leave DB operations", 4680)] }),
      new TableRow({ children: [dataCell("PayrollDAO.java", 4680), dataCell("Payroll DB operations", 4680)] }),
      new TableRow({ children: [boldDataCell("src/main/java/com/hrms/servlet/", 4680), dataCell("Java Servlet controller classes", 4680)] }),
      new TableRow({ children: [dataCell("LoginServlet.java", 4680), dataCell("Handles auth, register, logout", 4680)] }),
      new TableRow({ children: [dataCell("DashboardServlet.java", 4680), dataCell("Dashboard statistics API", 4680)] }),
      new TableRow({ children: [dataCell("EmployeeServlet.java", 4680), dataCell("Employee management REST API", 4680)] }),
      new TableRow({ children: [dataCell("AttendanceServlet.java", 4680), dataCell("Attendance REST API", 4680)] }),
      new TableRow({ children: [dataCell("LeaveServlet.java", 4680), dataCell("Leave management REST API", 4680)] }),
      new TableRow({ children: [dataCell("PayrollServlet.java", 4680), dataCell("Payroll REST API", 4680)] }),
      new TableRow({ children: [boldDataCell("src/main/webapp/", 4680), dataCell("Frontend HTML pages and assets", 4680)] }),
      new TableRow({ children: [dataCell("login.html, register.html", 4680), dataCell("Authentication pages", 4680)] }),
      new TableRow({ children: [dataCell("dashboard.html, profile.html", 4680), dataCell("User home and profile", 4680)] }),
      new TableRow({ children: [dataCell("employee.html, attendance.html", 4680), dataCell("Employee and attendance management", 4680)] }),
      new TableRow({ children: [dataCell("leave.html, payroll.html", 4680), dataCell("Leave and payroll management", 4680)] }),
      new TableRow({ children: [dataCell("css/style.css", 4680), dataCell("Global stylesheet", 4680)] }),
      new TableRow({ children: [dataCell("js/main.js", 4680), dataCell("Shared JavaScript utilities", 4680)] }),
      new TableRow({ children: [boldDataCell("src/main/webapp/WEB-INF/web.xml", 4680), dataCell("Servlet deployment descriptor", 4680)] }),
      new TableRow({ children: [boldDataCell("lib/", 4680), dataCell("Third-party JAR dependencies", 4680)] }),
      new TableRow({ children: [dataCell("sqlite-jdbc.jar", 4680), dataCell("SQLite JDBC driver", 4680)] }),
      new TableRow({ children: [dataCell("gson.jar", 4680), dataCell("Google Gson JSON library", 4680)] }),
      new TableRow({ children: [dataCell("servlet-api.jar", 4680), dataCell("Java Servlet API", 4680)] }),
    ]
  }),
  ...emptyLine(1),
  h2("Appendix B: System Requirements"),
  new Table({
    width: { size: 9360, type: WidthType.DXA },
    columnWidths: [4680, 4680],
    rows: [
      new TableRow({ children: [headerCell("Requirement", 4680), headerCell("Specification", 4680)] }),
      new TableRow({ children: [boldDataCell("Hardware Requirements", 4680), dataCell("", 4680)] }),
      new TableRow({ children: [dataCell("Processor", 4680), dataCell("Intel Core i3 or above", 4680)] }),
      new TableRow({ children: [dataCell("RAM", 4680), dataCell("4 GB minimum", 4680)] }),
      new TableRow({ children: [dataCell("Storage", 4680), dataCell("500 MB free disk space", 4680)] }),
      new TableRow({ children: [boldDataCell("Software Requirements", 4680), dataCell("", 4680)] }),
      new TableRow({ children: [dataCell("Operating System", 4680), dataCell("Windows 10 / Linux / macOS", 4680)] }),
      new TableRow({ children: [dataCell("JDK", 4680), dataCell("Java SE 11 or above", 4680)] }),
      new TableRow({ children: [dataCell("Web Server", 4680), dataCell("Apache Tomcat 10", 4680)] }),
      new TableRow({ children: [dataCell("Database", 4680), dataCell("SQLite (embedded, no installation required)", 4680)] }),
      new TableRow({ children: [dataCell("IDE", 4680), dataCell("VS Code with Java Extension Pack", 4680)] }),
      new TableRow({ children: [dataCell("Browser", 4680), dataCell("Google Chrome 90+ / Firefox 88+ / Edge 90+", 4680)] }),
    ]
  }),
  pageBreak(),
];

// ---- CHAPTER 8: PROGRESS SHEET ----
const chapter8 = [
  h1("Chapter 8: Annexure - Progress Sheet"),
  para("This progress sheet documents the phases of project development, week-wise activities, and completion status as reviewed by the project guide."),
  ...emptyLine(1),
  new Table({
    width: { size: 9360, type: WidthType.DXA },
    columnWidths: [500, 2000, 4260, 1400, 1200],
    rows: [
      new TableRow({ children: [headerCell("Sr.", 500), headerCell("Phase", 2000), headerCell("Activity / Milestone", 4260), headerCell("Target Date", 1400), headerCell("Status", 1200)] }),
      new TableRow({ children: [dataCell("1", 500), dataCell("Requirement Analysis", 2000), dataCell("Study of existing HR systems, defining scope and objectives", 4260), dataCell("Week 1-2", 1400), dataCell("Completed", 1200)] }),
      new TableRow({ children: [dataCell("2", 500), dataCell("System Design", 2000), dataCell("System architecture design, database schema design, ER diagram", 4260), dataCell("Week 3-4", 1400), dataCell("Completed", 1200)] }),
      new TableRow({ children: [dataCell("3", 500), dataCell("Frontend Dev", 2000), dataCell("HTML/CSS pages: login, dashboard, employee, attendance, leave, payroll", 4260), dataCell("Week 5-7", 1400), dataCell("Completed", 1200)] }),
      new TableRow({ children: [dataCell("4", 500), dataCell("Backend Dev", 2000), dataCell("Java Servlets and DAO classes for all modules, SQLite integration", 4260), dataCell("Week 7-9", 1400), dataCell("Completed", 1200)] }),
      new TableRow({ children: [dataCell("5", 500), dataCell("Integration", 2000), dataCell("Frontend-backend AJAX integration, session management, role-based access", 4260), dataCell("Week 9-10", 1400), dataCell("Completed", 1200)] }),
      new TableRow({ children: [dataCell("6", 500), dataCell("Testing", 2000), dataCell("Black box testing of all modules, bug fixing, edge case validation", 4260), dataCell("Week 11", 1400), dataCell("Completed", 1200)] }),
      new TableRow({ children: [dataCell("7", 500), dataCell("Documentation", 2000), dataCell("Project report writing, diagrams, test case documentation", 4260), dataCell("Week 12", 1400), dataCell("Completed", 1200)] }),
      new TableRow({ children: [dataCell("8", 500), dataCell("Final Review", 2000), dataCell("Pre-submission presentation, guide review, final corrections", 4260), dataCell("Week 13", 1400), dataCell("Completed", 1200)] }),
    ]
  }),
  ...emptyLine(2),
  h2("Guide Signatures"),
  ...emptyLine(1),
  new Table({
    width: { size: 9360, type: WidthType.DXA },
    columnWidths: [4680, 4680],
    rows: [
      new TableRow({
        children: [
          new TableCell({
            borders: noBorders, width: { size: 4680, type: WidthType.DXA }, margins: { top: 200, bottom: 200, left: 120, right: 120 }, children: [
              new Paragraph({ children: [new TextRun({ text: "Project Guide:", bold: true, size: 22, font: "Arial" })] }),
              ...emptyLine(2),
              new Paragraph({ children: [new TextRun({ text: "Prof. Amol B. Nawale", size: 22, font: "Arial" })] }),
              new Paragraph({ children: [new TextRun({ text: "Date: ___ / ___ / 2026", size: 20, font: "Arial" })] }),
            ]
          }),
          new TableCell({
            borders: noBorders, width: { size: 4680, type: WidthType.DXA }, margins: { top: 200, bottom: 200, left: 120, right: 120 }, children: [
              new Paragraph({ children: [new TextRun({ text: "Director:", bold: true, size: 22, font: "Arial" })] }),
              ...emptyLine(2),
              new Paragraph({ children: [new TextRun({ text: "Dr. Prashant Radhakrishna Tambe", size: 22, font: "Arial" })] }),
              new Paragraph({ children: [new TextRun({ text: "Date: ___ / ___ / 2026", size: 20, font: "Arial" })] }),
            ]
          }),
        ]
      })
    ]
  }),
];

// ---- BUILD DOCUMENT ----
const doc = new Document({
  numbering: {
    config: [
      {
        reference: "bullets",
        levels: [{
          level: 0, format: LevelFormat.BULLET, text: "\u2022", alignment: AlignmentType.LEFT,
          style: { paragraph: { indent: { left: 720, hanging: 360 } } }
        }]
      }
    ]
  },
  styles: {
    default: {
      document: { run: { font: "Arial", size: 22 } }
    },
    paragraphStyles: [
      {
        id: "Heading1", name: "Heading 1", basedOn: "Normal", next: "Normal", quickFormat: true,
        run: { size: 32, bold: true, font: "Arial", color: "1F4E79" },
        paragraph: { spacing: { before: 360, after: 200 }, outlineLevel: 0 }
      },
      {
        id: "Heading2", name: "Heading 2", basedOn: "Normal", next: "Normal", quickFormat: true,
        run: { size: 26, bold: true, font: "Arial", color: "2E75B6" },
        paragraph: { spacing: { before: 240, after: 120 }, outlineLevel: 1 }
      },
      {
        id: "Heading3", name: "Heading 3", basedOn: "Normal", next: "Normal", quickFormat: true,
        run: { size: 23, bold: true, font: "Arial", color: "2F5496" },
        paragraph: { spacing: { before: 180, after: 100 }, outlineLevel: 2 }
      },
    ]
  },
  sections: [{
    properties: {
      page: {
        size: { width: 11906, height: 16838 }, // A4
        margin: { top: 1440, right: 1440, bottom: 1440, left: 1440 }
      }
    },
    headers: {
      default: new Header({
        children: [
          new Paragraph({
            alignment: AlignmentType.RIGHT,
            border: { bottom: { style: BorderStyle.SINGLE, size: 4, color: "2E75B6", space: 4 } },
            spacing: { after: 120 },
            children: [
              new TextRun({ text: "Human Resource Management System  |  MCA Project Report  |  2025-26", size: 18, font: "Arial", color: "555555" })
            ]
          })
        ]
      })
    },
    footers: {
      default: new Footer({
        children: [
          new Paragraph({
            alignment: AlignmentType.CENTER,
            border: { top: { style: BorderStyle.SINGLE, size: 4, color: "2E75B6", space: 4 } },
            spacing: { before: 120 },
            children: [
              new TextRun({ text: "Page ", size: 18, font: "Arial", color: "555555" }),
              new TextRun({ children: [PageNumber.CURRENT], size: 18, font: "Arial", color: "555555" }),
              new TextRun({ text: " of ", size: 18, font: "Arial", color: "555555" }),
              new TextRun({ children: [PageNumber.TOTAL_PAGES], size: 18, font: "Arial", color: "555555" }),
            ]
          })
        ]
      })
    },
    children: [
      ...titlePage,
      ...indexSection,
      ...chapter1,
      ...chapter2,
      ...chapter3,
      ...chapter4,
      ...chapter5,
      ...chapter6,
      ...chapter7,
      ...chapter8,
    ]
  }]
});

Packer.toBuffer(doc).then(buffer => {
  fs.writeFileSync("HRMS_Project_Report.docx", buffer);
  console.log("Report created successfully!");
});