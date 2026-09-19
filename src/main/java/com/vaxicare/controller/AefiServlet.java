package com.vaxicare.controller;

import com.vaxicare.dao.AefiDAO;
import com.vaxicare.exception.VaxiCareException;
import com.vaxicare.model.AefiReport;
import com.vaxicare.model.Patient;
import com.vaxicare.service.AefiTriageService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Controller handling Adverse Event Following Immunization (AEFI) reports.
 * Connects web request with the multithreaded triage background engine.
 */
@WebServlet(name = "AefiServlet", urlPatterns = {"/aefi", "/report-adverse-effect"})
public class AefiServlet extends HttpServlet {
    private final AefiDAO aefiDAO = new AefiDAO();
    private final AefiTriageService triageService = AefiTriageService.getInstance();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        Object userObj = session != null ? session.getAttribute("user") : null;

        int patientId = 1;
        if (userObj instanceof Patient) {
            patientId = ((Patient) userObj).getPatientId();
        }

        String name = req.getParameter("aefiName");
        String phone = req.getParameter("aefiPhone");
        String severity = req.getParameter("aefiSeverity");
        String symptoms = req.getParameter("aefiSymptoms");

        if (symptoms == null || symptoms.trim().isEmpty()) {
            symptoms = "Patient reported post-inoculation reaction (" + severity + ")";
        }

        AefiReport report = new AefiReport();
        report.setPatientId(patientId);
        report.setVaccineId(1); // Default to primary vaccine administered
        report.setSeverity(severity != null ? severity : "MILD");
        report.setSymptoms(symptoms);
        report.setCallbackPhone(phone != null ? phone : "+91 1075");
        report.setPatientName(name != null ? name : "Anonymous Patient");

        try {
            int reportId = aefiDAO.recordAefiReport(report);
            report.setReportId(reportId);

            // Asynchronously dispatch to multithreaded priority triage worker
            triageService.submitReportForTriage(report);

            String msg = "AEFI Report #" + reportId + " logged successfully. Our 24/7 Clinical Triage officer has been notified.";
            resp.sendRedirect(req.getContextPath() + "/index.jsp?msg=" + URLEncoder.encode(msg, StandardCharsets.UTF_8));
        } catch (VaxiCareException e) {
            String err = "AEFI Log Error: " + e.getMessage();
            resp.sendRedirect(req.getContextPath() + "/index.jsp?error=" + URLEncoder.encode(err, StandardCharsets.UTF_8));
        }
    }
}
