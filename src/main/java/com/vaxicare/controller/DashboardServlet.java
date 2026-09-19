package com.vaxicare.controller;

import com.vaxicare.dao.AefiDAO;
import com.vaxicare.dao.AppointmentDAO;
import com.vaxicare.dao.CentreDAO;
import com.vaxicare.dao.VaccineDAO;
import com.vaxicare.exception.VaxiCareException;
import com.vaxicare.model.*;
import com.vaxicare.service.VaccinationService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;

/**
 * Controller preparing data for Patient Dashboard and Admin Portal.
 */
@WebServlet(name = "DashboardServlet", urlPatterns = {"/dashboard"})
public class DashboardServlet extends HttpServlet {
    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private final VaccineDAO vaccineDAO = new VaccineDAO();
    private final CentreDAO centreDAO = new CentreDAO();
    private final AefiDAO aefiDAO = new AefiDAO();
    private final VaccinationService vaccinationService = new VaccinationService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        Person user = session != null ? (Person) session.getAttribute("user") : null;

        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/index.jsp?login=required");
            return;
        }

        try {
            if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                List<Appointment> allAppts = appointmentDAO.getAllAppointments();
                List<AefiReport> pendingAefi = aefiDAO.getAllPendingReports();
                List<Centre> centres = centreDAO.getAllCentres();
                List<Vaccine> vaccines = vaccineDAO.getAllVaccines();

                req.setAttribute("allAppointments", allAppts);
                req.setAttribute("pendingAefi", pendingAefi);
                req.setAttribute("centres", centres);
                req.setAttribute("vaccines", vaccines);

                req.getRequestDispatcher("/admin.jsp").forward(req, resp);
            } else {
                Patient patient = (Patient) user;
                List<Appointment> history = appointmentDAO.getAppointmentsByPatient(patient.getPatientId());
                List<Vaccine> vaccines = vaccineDAO.getAllVaccines();
                List<Centre> centres = centreDAO.getAllCentres();

                // Evaluate primary vaccine status
                Vaccine primaryVaccine = vaccines.isEmpty() ? null : vaccines.get(0);
                if (!history.isEmpty()) {
                    Vaccine match = vaccineDAO.getVaccineById(history.get(0).getVaccineId());
                    if (match != null) primaryVaccine = match;
                }

                VaccinationService.ImmunizationStatus status = 
                    vaccinationService.evaluateStatus(patient, primaryVaccine, history);

                req.setAttribute("patient", patient);
                req.setAttribute("appointments", history);
                req.setAttribute("vaccines", vaccines);
                req.setAttribute("centres", centres);
                req.setAttribute("status", status);
                req.setAttribute("primaryVaccine", primaryVaccine);

                req.getRequestDispatcher("/dashboard.jsp").forward(req, resp);
            }
        } catch (VaxiCareException e) {
            req.setAttribute("errorMessage", "Error loading dashboard: " + e.getMessage());
            req.getRequestDispatcher("/index.jsp").forward(req, resp);
        }
    }
}
