package com.momcare.report.generation.controller;

import com.lowagie.text.DocumentException;
import com.momcare.report.generation.config.JwtUtil;
import com.momcare.report.generation.entity.DietEntryDto;
import com.momcare.report.generation.entity.MonthlyExpenseDto;
import com.momcare.report.generation.service.ReportService;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired
    private ReportService reportGenerationService;

    @Autowired
    private JwtUtil jwtUtil;

    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or invalid Authorization header");
    }

    private String extractEmail(HttpServletRequest request) {
        String token = extractToken(request);
        try {
            return jwtUtil.extractEmail(token);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or malformed JWT token");
        }
    }

    @GetMapping("/monthly-report/pdf")
    public ResponseEntity<byte[]> getMonthlyReportPdf(
            HttpServletRequest request,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {

        String jwtToken = extractToken(request);
        String userEmail = extractEmail(request);

        LocalDate today = LocalDate.now();
        int reportYear = year != null ? year : today.getYear();
        int reportMonth = month != null ? month : today.getMonthValue();

        MonthlyExpenseDto monthlyExpense = reportGenerationService.getMonthlyExpenseSummary(userEmail, reportYear, reportMonth, jwtToken);
        List<DietEntryDto> dietEntries = reportGenerationService.getMonthlyDietEntries(userEmail, reportYear, reportMonth, jwtToken);

        try {
            byte[] pdfBytes = reportGenerationService.generateMonthlyReportPdf(userEmail, reportYear, reportMonth, monthlyExpense, dietEntries);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "momcare_monthly_report.pdf");
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
            headers.setPragma("public");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (ResponseStatusException e) {
            throw e;
        } catch (IOException | DocumentException e) {
            System.err.println("Error generating PDF report: " + e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error generating report.");
        } catch (Exception e) {
            System.err.println("An unexpected error occurred during report generation: " + e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.");
        }
    }
}