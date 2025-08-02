package com.momcare.report.generation.service;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.momcare.report.generation.entity.DietEntryDto;
import com.momcare.report.generation.entity.MonthlyExpenseDto;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class ReportService {

    private final WebClient.Builder webClientBuilder;

    @Value("${expense.service.name}")
    private String expenseServiceId;

    @Value("${diet.service.name}")
    private String dietServiceId;

    private boolean expenseServiceFallbackUsed = false;
    private boolean dietServiceFallbackUsed = false;

    public ReportService(@LoadBalanced WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    // ----------------- Expense Service with Circuit Breaker -----------------
    @CircuitBreaker(name = "expenseService", fallbackMethod = "expenseFallback")
    public MonthlyExpenseDto getMonthlyExpenseSummary(String userEmail, int year, int month, String jwtToken) {
        expenseServiceFallbackUsed = false;
        try {
            return webClientBuilder.build()
                    .get()
                    .uri("http://" + expenseServiceId + "/api/expenses/monthly?email=" + userEmail +
                            "&year=" + year + "&month=" + month)
                    .header("Authorization", "Bearer " + jwtToken)
                    .retrieve()
                    .bodyToMono(MonthlyExpenseDto.class)
                    .block();
        } catch (WebClientResponseException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                return null;
            }
            throw new ResponseStatusException(ex.getStatusCode(), "Error fetching monthly expenses: " + ex.getStatusText());
        }
    }

    public MonthlyExpenseDto expenseFallback(String userEmail, int year, int month, String jwtToken, Throwable t) {
        System.err.println("⚠ Circuit breaker triggered for expense-service: " + t.getMessage());
        expenseServiceFallbackUsed = true;
        return new MonthlyExpenseDto(userEmail, year, month, Map.of(), 0.0); // empty expense data
    }

    // ----------------- Diet Service with Circuit Breaker -----------------
    @CircuitBreaker(name = "dietService", fallbackMethod = "dietFallback")
    public List<DietEntryDto> getMonthlyDietEntries(String userEmail, int year, int month, String jwtToken) {
        dietServiceFallbackUsed = false;
        try {
            return webClientBuilder.build()
                    .get()
                    .uri("http://" + dietServiceId + "/api/diet/monthly?email=" + userEmail +
                            "&year=" + year + "&month=" + month)
                    .header("Authorization", "Bearer " + jwtToken)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<DietEntryDto>>() {})
                    .block();
        } catch (WebClientResponseException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                return List.of();
            }
            throw new ResponseStatusException(ex.getStatusCode(), "Error fetching diet entries: " + ex.getStatusText());
        }
    }

    public List<DietEntryDto> dietFallback(String userEmail, int year, int month, String jwtToken, Throwable t) {
        System.err.println("⚠ Circuit breaker triggered for diet-service: " + t.getMessage());
        dietServiceFallbackUsed = true;
        return List.of(); // empty diet logs
    }

    // ----------------- PDF Generation -----------------
    public byte[] generateMonthlyReportPdf(String userEmail, int year, int month,
                                           MonthlyExpenseDto monthlyExpense, List<DietEntryDto> dietEntries)
            throws IOException, DocumentException {

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter.getInstance(document, out);
            document.open();

            // Title
            Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD, Color.BLACK);
            Paragraph title = new Paragraph("Momcare Monthly Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph("\n"));

            // User & Period Info
            String monthName = LocalDate.of(year, month, 1).getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
            Font subTitleFont = new Font(Font.HELVETICA, 12, Font.NORMAL, Color.BLACK);
            document.add(new Paragraph("Report for: " + userEmail, subTitleFont));
            document.add(new Paragraph("Period: " + monthName + " " + year, subTitleFont));
            document.add(new Paragraph("\n"));

            // Service Health Section
            Font healthFont = new Font(Font.HELVETICA, 10, Font.ITALIC, Color.RED);
            if (expenseServiceFallbackUsed || dietServiceFallbackUsed) {
                document.add(new Paragraph("⚠ Service Health Notice:", new Font(Font.HELVETICA, 12, Font.BOLD, Color.RED)));
                if (expenseServiceFallbackUsed) {
                    document.add(new Paragraph("• Expense Service data could not be retrieved (using fallback)", healthFont));
                }
                if (dietServiceFallbackUsed) {
                    document.add(new Paragraph("• Diet Service data could not be retrieved (using fallback)", healthFont));
                }
                document.add(new Paragraph("\n"));
            }

            // --- Expenses Section ---
            document.add(new Paragraph("1. Monthly Expenses", new Font(Font.HELVETICA, 14, Font.BOLD)));
            document.add(new Paragraph("\n"));
            if (monthlyExpense != null && monthlyExpense.getCategoryAmounts() != null && !monthlyExpense.getCategoryAmounts().isEmpty()) {
                PdfPTable expenseTable = new PdfPTable(2);
                expenseTable.setWidthPercentage(60);
                addTableHeader(expenseTable, "Category", "Amount");
                for (Map.Entry<String, Double> entry : monthlyExpense.getCategoryAmounts().entrySet()) {
                    expenseTable.addCell(entry.getKey());
                    expenseTable.addCell(String.format("%.2f", entry.getValue()));
                }
                document.add(expenseTable);
                document.add(new Paragraph("Total Expenses: " + String.format("%.2f", monthlyExpense.getTotal())));
            } else {
                document.add(new Paragraph("No expenses recorded for this month."));
            }
            document.add(new Paragraph("\n\n"));

            // --- Diet Log Section ---
            document.add(new Paragraph("2. Monthly Diet Logs", new Font(Font.HELVETICA, 14, Font.BOLD)));
            document.add(new Paragraph("\n"));
            if (dietEntries != null && !dietEntries.isEmpty()) {
                for (DietEntryDto dietEntry : dietEntries) {
                    document.add(new Paragraph(
                            "Date: " + dietEntry.getDate().toString() +
                                    " | Water Intake: " +
                                    (dietEntry.getWaterIntakeInLitres() != null ?
                                            String.format("%.2f L", dietEntry.getWaterIntakeInLitres()) : "N/A"),
                            new Font(Font.HELVETICA, 12, Font.BOLD)));

                    PdfPTable mealTable = new PdfPTable(2);
                    mealTable.setWidthPercentage(80);
                    addTableHeader(mealTable, "Meal Type", "Details");

                    for (Map.Entry<String, String> meal : dietEntry.getMeals().entrySet()) {
                        mealTable.addCell(meal.getKey());
                        mealTable.addCell(meal.getValue());
                    }
                    document.add(mealTable);
                }
            } else {
                document.add(new Paragraph("No diet logs recorded for this month."));
            }

            document.close();
            return out.toByteArray();
        }
    }

    // Helper for table headers
    private void addTableHeader(PdfPTable table, String col1, String col2) {
        Font headerFont = new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE);
        PdfPCell cell1 = new PdfPCell(new Phrase(col1, headerFont));
        PdfPCell cell2 = new PdfPCell(new Phrase(col2, headerFont));
        cell1.setBackgroundColor(Color.DARK_GRAY);
        cell2.setBackgroundColor(Color.DARK_GRAY);
        table.addCell(cell1);
        table.addCell(cell2);
    }
}
