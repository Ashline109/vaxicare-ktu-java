package com.vaxicare.controller;

import com.vaxicare.dao.AppointmentDAO;
import com.vaxicare.dao.UserDAO;
import com.vaxicare.dao.VaccineDAO;
import com.vaxicare.model.Appointment;
import com.vaxicare.model.Patient;
import com.vaxicare.model.Vaccine;
import com.vaxicare.service.XmlDataService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller generating dynamically serialized XML Vaccination Certificates.
 * Demonstrates: Direct XML response streaming, XML compliance, and syllabus requirement for XML integration.
 */
@WebServlet(name = "XmlExportServlet", urlPatterns = {"/export-certificate", "/download-xml-certificate"})
public class XmlExportServlet extends HttpServlet {
    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private final UserDAO userDAO = new UserDAO();
    private final VaccineDAO vaccineDAO = new VaccineDAO();
    private final XmlDataService xmlDataService = new XmlDataService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        Patient patient = null;

        if (session != null && session.getAttribute("user") instanceof Patient) {
            patient = (Patient) session.getAttribute("user");
        } else {
            // Fallback default sample patient for direct demo/viva testing
            try {
                patient = userDAO.getPatientById(1);
            } catch (Exception e) {
                // Ignore fallback error
            }
        }

        try {
            List<Appointment> completedDoses = new ArrayList<>();
            Vaccine vaccine = null;

            if (patient != null) {
                List<Appointment> history = appointmentDAO.getAppointmentsByPatient(patient.getPatientId());
                for (Appointment a : history) {
                    if ("COMPLETED".equalsIgnoreCase(a.getStatus())) {
                        completedDoses.add(a);
                    }
                }
                if (!completedDoses.isEmpty()) {
                    vaccine = vaccineDAO.getVaccineById(completedDoses.get(0).getVaccineId());
                }
            }

            if (vaccine == null) {
                vaccine = vaccineDAO.getVaccineById(1); // Default to Covishield
            }

            String xmlOutput = xmlDataService.generateDigitalCertificateXml(patient, vaccine, completedDoses);

            resp.setContentType("application/xml; charset=UTF-8");
            String download = req.getParameter("download");
            if ("true".equalsIgnoreCase(download)) {
                resp.setHeader("Content-Disposition", "attachment; filename=\"vaxicare_certificate_" + 
                               (patient != null ? patient.getUsername() : "sample") + ".xml\"");
            }

            try (PrintWriter out = resp.getWriter()) {
                out.write(xmlOutput);
            }

        } catch (Exception e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "XML Generation Failed: " + e.getMessage());
        }
    }
}
