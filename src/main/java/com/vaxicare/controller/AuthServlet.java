package com.vaxicare.controller;

import com.vaxicare.dao.UserDAO;
import com.vaxicare.exception.AuthenticationException;
import com.vaxicare.exception.VaxiCareException;
import com.vaxicare.model.Patient;
import com.vaxicare.model.Person;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.time.LocalDate;

/**
 * Controller handling Patient & Staff Authentication (Login, Registration, Logout).
 * Demonstrates: Java Servlets, Session Management (HttpSession), Request Dispatching.
 */
@WebServlet(name = "AuthServlet", urlPatterns = {"/auth", "/login", "/logout"})
public class AuthServlet extends HttpServlet {
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        String path = req.getServletPath();
        if ("/logout".equals(path) || "logout".equals(req.getParameter("action"))) {
            HttpSession session = req.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            resp.sendRedirect(req.getContextPath() + "/index.jsp?msg=Logged+out+successfully");
            return;
        }
        resp.sendRedirect(req.getContextPath() + "/index.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        String action = req.getParameter("action");

        try {
            if ("register".equalsIgnoreCase(action)) {
                handleRegistration(req, resp);
            } else {
                handleLogin(req, resp);
            }
        } catch (VaxiCareException e) {
            req.setAttribute("errorMessage", e.getMessage());
            req.getRequestDispatcher("/index.jsp").forward(req, resp);
        }
    }

    private void handleLogin(HttpServletRequest req, HttpServletResponse resp) 
            throws VaxiCareException, IOException, ServletException {
        String user = req.getParameter("username");
        String pass = req.getParameter("password");

        if (user == null || pass == null || user.trim().isEmpty() || pass.trim().isEmpty()) {
            throw new AuthenticationException("Username and Password are required.");
        }

        Person person = userDAO.authenticate(user.trim(), pass.trim());
        HttpSession session = req.getSession(true);
        session.setAttribute("user", person);
        session.setAttribute("userRole", person.getRole());

        if ("ADMIN".equalsIgnoreCase(person.getRole())) {
            resp.sendRedirect(req.getContextPath() + "/admin.jsp");
        } else {
            resp.sendRedirect(req.getContextPath() + "/dashboard");
        }
    }

    private void handleRegistration(HttpServletRequest req, HttpServletResponse resp) 
            throws VaxiCareException, IOException {
        String fullName = req.getParameter("fullName");
        String phone = req.getParameter("phone");
        String dobStr = req.getParameter("dob");
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        String aadhar = req.getParameter("aadhar");
        String bloodGroup = req.getParameter("bloodGroup");

        LocalDate dob = LocalDate.now().minusYears(20);
        if (dobStr != null && !dobStr.trim().isEmpty()) {
            dob = LocalDate.parse(dobStr.trim());
        }

        Patient patient = new Patient(
            0, username, fullName, phone, 0, dob,
            aadhar != null ? aadhar : "IND-" + System.currentTimeMillis() % 1000000,
            bloodGroup != null ? bloodGroup : "O+",
            "New self-registered patient"
        );

        userDAO.registerPatient(patient, password);

        HttpSession session = req.getSession(true);
        session.setAttribute("user", patient);
        session.setAttribute("userRole", "PATIENT");

        resp.sendRedirect(req.getContextPath() + "/dashboard?registered=true");
    }
}
