package com.vaxicare.controller;

import com.vaxicare.dao.AppointmentDAO;
import com.vaxicare.dao.CentreDAO;
import com.vaxicare.dao.VaccineDAO;
import com.vaxicare.exception.InsufficientGapException;
import com.vaxicare.exception.SlotUnavailableException;
import com.vaxicare.exception.VaxiCareException;
import com.vaxicare.model.Appointment;
import com.vaxicare.model.Centre;
import com.vaxicare.model.Patient;
import com.vaxicare.model.Vaccine;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

/**
 * Controller handling appointment bookings and dose scheduling.
 * Demonstrates: Exception catching (custom domain exceptions) and response feedback.
 */
@WebServlet(name = "AppointmentServlet", urlPatterns = {"/appointment", "/book-slot"})
public class AppointmentServlet extends HttpServlet {
    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private final VaccineDAO vaccineDAO = new VaccineDAO();
    private final CentreDAO centreDAO = new CentreDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        Object userObj = session != null ? session.getAttribute("user") : null;

        int patientId = 1; // Default fallback to Arya Prakash for guest direct bookings
        if (userObj instanceof Patient) {
            patientId = ((Patient) userObj).getPatientId();
        }

        try {
            String vaccineInput = req.getParameter("vaccine");
            String centreInput = req.getParameter("centre");
            String dateStr = req.getParameter("appointmentDate");

            // Resolve Vaccine ID (by ID or name)
            int vaccineId = 1;
            try {
                vaccineId = Integer.parseInt(vaccineInput);
            } catch (Exception e) {
                Vaccine v = vaccineDAO.getVaccineByName(vaccineInput);
                if (v != null) vaccineId = v.getVaccineId();
            }

            // Resolve Centre ID (by ID or name)
            int centreId = 1;
            try {
                centreId = Integer.parseInt(centreInput);
            } catch (Exception e) {
                Centre c = centreDAO.getCentreByName(centreInput);
                if (c != null) centreId = c.getCentreId();
            }

            LocalDate appointmentDate = LocalDate.now().plusDays(1);
            if (dateStr != null && !dateStr.trim().isEmpty()) {
                appointmentDate = LocalDate.parse(dateStr.trim());
            }

            Appointment appt = new Appointment();
            appt.setPatientId(patientId);
            appt.setVaccineId(vaccineId);
            appt.setCentreId(centreId);
            appt.setAppointmentDate(appointmentDate);

            int bookedId = appointmentDAO.bookAppointment(appt);

            String successMsg = "Appointment #" + bookedId + " booked successfully for Dose " + appt.getDoseNumber() + "!";
            if (userObj != null) {
                resp.sendRedirect(req.getContextPath() + "/dashboard?msg=" + URLEncoder.encode(successMsg, StandardCharsets.UTF_8));
            } else {
                resp.sendRedirect(req.getContextPath() + "/index.jsp?msg=" + URLEncoder.encode(successMsg, StandardCharsets.UTF_8));
            }

        } catch (InsufficientGapException e) {
            String err = "Clinical Interval Warning: " + e.getMessage();
            resp.sendRedirect(req.getContextPath() + "/index.jsp?error=" + URLEncoder.encode(err, StandardCharsets.UTF_8));
        } catch (SlotUnavailableException e) {
            String err = "Inventory Notice: " + e.getMessage();
            resp.sendRedirect(req.getContextPath() + "/index.jsp?error=" + URLEncoder.encode(err, StandardCharsets.UTF_8));
        } catch (VaxiCareException e) {
            String err = "Booking Error: " + e.getMessage();
            resp.sendRedirect(req.getContextPath() + "/index.jsp?error=" + URLEncoder.encode(err, StandardCharsets.UTF_8));
        }
    }
}
