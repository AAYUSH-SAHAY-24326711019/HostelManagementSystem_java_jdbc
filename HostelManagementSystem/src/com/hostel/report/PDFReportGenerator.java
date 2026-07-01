package com.hostel.report;

import com.hostel.dao.*;
import com.hostel.model.*;
import com.hostel.util.AppConstants;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * All PDF report generation is centralized here.
 * Uses PDFBox 1.8.16 API (old style PDPageContentStream constructor).
 */
public class PDFReportGenerator {

    private static final float MARGIN = 40;
    private static final float PAGE_WIDTH = 595;   // A4
    private static final float PAGE_HEIGHT = 842;
    private static final float LINE_HEIGHT = 18f;
    private static final SimpleDateFormat SDF = new SimpleDateFormat("dd-MMM-yyyy");
    private static final SimpleDateFormat TSDF = new SimpleDateFormat("dd-MMM-yyyy HH:mm");

    private final String hostelName;

    public PDFReportGenerator() {
        hostelName = new HostelConfigDAO().getHostelName();
    }

    // ===== 1. Girl's Payment History =====================================
    public String generatePaymentReport(int girlId) throws IOException {
        GirlDAO gDao = new GirlDAO();
        BillDAO bDao = new BillDAO();
        Girl girl = gDao.getById(girlId);
        List<Payment> payments = bDao.getPaymentsByGirlId(girlId);
        double total = payments.stream().mapToDouble(Payment::getAmountPaid).sum();

        PDDocument doc = new PDDocument();
        PDPage page = new PDPage(PDPage.PAGE_SIZE_A4);
        doc.addPage(page);
        PDPageContentStream cs = new PDPageContentStream(doc, page);
        float y = PAGE_HEIGHT - MARGIN;

        y = drawHeader(cs, y, "Payment History Report");
        y = drawKeyVal(cs, y, "Student", girl.getName());
        y = drawKeyVal(cs, y, "Aadhar", girl.getAadharNumber());
        y = drawKeyVal(cs, y, "Generated", TSDF.format(new Date()));
        y -= 10;
        y = drawTableHeader(cs, y, new String[]{"Date", "Bill Type", "Mode", "Amount (Rs)", "Receipt"}, new float[]{90, 130, 90, 100, 110});
        for (Payment p : payments) {
            String[] row = {
                SDF.format(p.getPaymentDate()),
                p.getBillType() != null ? p.getBillType() : "-",
                p.getPaymentMode(),
                String.format("%.2f", p.getAmountPaid()),
                p.getReceiptNo() != null ? p.getReceiptNo() : "-"
            };
            y = drawTableRow(cs, doc, page, y, row, new float[]{90, 130, 90, 100, 110}, false);
            if (y < 60) { cs.close(); page = new PDPage(PDPage.PAGE_SIZE_A4); doc.addPage(page); cs = new PDPageContentStream(doc, page); y = PAGE_HEIGHT - MARGIN; }
        }
        y -= 10;
        y = drawKeyVal(cs, y, "Total Paid", String.format("Rs. %.2f", total));
        cs.close();
        return saveDoc(doc, "payment_" + girlId + "_" + System.currentTimeMillis() + ".pdf");
    }

    // ===== 2. Girl's Current Plan ========================================
    public String generatePlanReport(int girlId) throws IOException {
        GirlDAO gDao = new GirlDAO();
        FeeStructureDAO fsDao = new FeeStructureDAO();
        Girl girl = gDao.getById(girlId);
        FeeStructure fs = fsDao.getByGirlId(girlId);

        PDDocument doc = new PDDocument();
        PDPage page = new PDPage(PDPage.PAGE_SIZE_A4);
        doc.addPage(page);
        PDPageContentStream cs = new PDPageContentStream(doc, page);
        float y = PAGE_HEIGHT - MARGIN;

        y = drawHeader(cs, y, "Stay Plan Details");
        y = drawKeyVal(cs, y, "Student", girl.getName());
        y = drawKeyVal(cs, y, "Room", girl.getRoomNumber() != null ? girl.getRoomNumber() : "-");
        y = drawKeyVal(cs, y, "Plan", girl.getPlanName() != null ? girl.getPlanName() : "-");
        y = drawKeyVal(cs, y, "Kitchen Plan", girl.getKitchenPlanName() != null ? girl.getKitchenPlanName() : "-");
        y = drawKeyVal(cs, y, "Admission Date", girl.getAdmissionDate() != null ? SDF.format(girl.getAdmissionDate()) : "-");
        y -= 10;
        if (fs != null) {
            y = drawSectionTitle(cs, y, "Monthly Fee Breakdown");
            y = drawKeyVal(cs, y, "Monthly Stay Bill", String.format("Rs. %.2f", fs.getMonthlyStayBill()));
            y = drawKeyVal(cs, y, "Emergency Deposit", String.format("Rs. %.2f", fs.getEmergencyDeposit()));
            y = drawKeyVal(cs, y, "Electricity Deposit", String.format("Rs. %.2f", fs.getElectricityDeposit()));
            y = drawKeyVal(cs, y, "WiFi Deposit", String.format("Rs. %.2f", fs.getWifiDeposit()));
            y = drawKeyVal(cs, y, "Plan Extra Charge", String.format("Rs. %.2f", fs.getPlanExtraCharge()));
            y = drawKeyVal(cs, y, "Kitchen Charge", String.format("Rs. %.2f", fs.getKitchenCharge()));
            double total = fs.getMonthlyStayBill() + fs.getPlanExtraCharge() + fs.getKitchenCharge();
            y = drawKeyVal(cs, y, "Total Monthly", String.format("Rs. %.2f", total));
        }
        cs.close();
        return saveDoc(doc, "plan_" + girlId + "_" + System.currentTimeMillis() + ".pdf");
    }

    // ===== 3. Active Girls List ==========================================
    public String generateActiveGirlsReport() throws IOException {
        List<Girl> girls = new GirlDAO().getAllActive();
        return generateGirlListReport(girls, "List of Currently Staying Girls", "active_girls");
    }

    // ===== 4. Left Girls List ============================================
    public String generateLeftGirlsReport() throws IOException {
        List<Girl> girls = new GirlDAO().getAllLeft();
        return generateGirlListReport(girls, "List of Girls Who Have Left", "left_girls");
    }

    // ===== 5. Girls Due Next Month =======================================
    public String generateDueNextMonthReport() throws IOException {
        List<Girl> girls = new GirlDAO().getGirlsDueNextMonth();
        return generateGirlListReport(girls, "Girls With Fees Due Next Month", "due_next_month");
    }

    // ===== 6. Girls Paid This Month ======================================
    public String generatePaidThisMonthReport() throws IOException {
        List<Girl> girls = new GirlDAO().getGirlsPaidThisMonth();
        return generateGirlListReport(girls, "Girls Who Paid Fees This Month", "paid_this_month");
    }

    // ===== 7. All Dues Report ============================================
    public String generateDuesReport() throws IOException {
        List<Due> dues = new BillDAO().getAllPendingDues();
        PDDocument doc = new PDDocument();
        PDPage page = new PDPage(PDPage.PAGE_SIZE_A4);
        doc.addPage(page);
        PDPageContentStream cs = new PDPageContentStream(doc, page);
        float y = PAGE_HEIGHT - MARGIN;
        y = drawHeader(cs, y, "Pending Dues Report");
        y = drawKeyVal(cs, y, "Generated", TSDF.format(new Date()));
        y -= 10;
        y = drawTableHeader(cs, y, new String[]{"Girl Name", "Bill Type", "Amount Due (Rs)", "Due Date"}, new float[]{160, 130, 130, 100});
        for (Due d : dues) {
            y = drawTableRow(cs, doc, page, y, new String[]{
                d.getGirlName(), d.getBillType(),
                String.format("%.2f", d.getAmountDue()),
                d.getDueDate() != null ? SDF.format(d.getDueDate()) : "-"
            }, new float[]{160, 130, 130, 100}, false);
            if (y < 60) { cs.close(); page = new PDPage(PDPage.PAGE_SIZE_A4); doc.addPage(page); cs = new PDPageContentStream(doc, page); y = PAGE_HEIGHT - MARGIN; }
        }
        cs.close();
        return saveDoc(doc, "dues_" + System.currentTimeMillis() + ".pdf");
    }

    // ===== 8. Fines Report ===============================================
    public String generateFinesReport(int girlId) throws IOException {
        List<Fine> fines = (girlId > 0) ? new FineDAO().getFinesByGirlId(girlId) : new FineDAO().getAllFines();
        PDDocument doc = new PDDocument();
        PDPage page = new PDPage(PDPage.PAGE_SIZE_A4);
        doc.addPage(page);
        PDPageContentStream cs = new PDPageContentStream(doc, page);
        float y = PAGE_HEIGHT - MARGIN;
        y = drawHeader(cs, y, girlId > 0 ? "Fine Report (Student)" : "All Fines Report");
        y -= 10;
        y = drawTableHeader(cs, y, new String[]{"Girl Name", "Reason", "Amount (Rs)", "Date", "Status"}, new float[]{120, 150, 90, 90, 70});
        for (Fine f : fines) {
            y = drawTableRow(cs, doc, page, y, new String[]{
                f.getGirlName(), f.getReason(),
                String.format("%.2f", f.getAmount()),
                f.getFineDate() != null ? SDF.format(f.getFineDate()) : "-",
                f.getStatus()
            }, new float[]{120, 150, 90, 90, 70}, false);
            if (y < 60) { cs.close(); page = new PDPage(PDPage.PAGE_SIZE_A4); doc.addPage(page); cs = new PDPageContentStream(doc, page); y = PAGE_HEIGHT - MARGIN; }
        }
        cs.close();
        return saveDoc(doc, "fines_" + System.currentTimeMillis() + ".pdf");
    }

    // ===== 9. Canteen Refund Report ======================================
    public String generateRefundReport() throws IOException {
        List<Object[]> refunds = new CanteenDAO().getPendingRefunds();
        PDDocument doc = new PDDocument();
        PDPage page = new PDPage(PDPage.PAGE_SIZE_A4);
        doc.addPage(page);
        PDPageContentStream cs = new PDPageContentStream(doc, page);
        float y = PAGE_HEIGHT - MARGIN;
        y = drawHeader(cs, y, "Pending Canteen Refunds");
        y -= 10;
        y = drawTableHeader(cs, y, new String[]{"Girl Name", "Service Date", "Refund Amount (Rs)"}, new float[]{200, 150, 160});
        for (Object[] row : refunds) {
            y = drawTableRow(cs, doc, page, y, new String[]{
                (String) row[0],
                row[4] != null ? SDF.format(row[4]) : "-",
                String.format("%.2f", (Double) row[3])
            }, new float[]{200, 150, 160}, false);
            if (y < 60) { cs.close(); page = new PDPage(PDPage.PAGE_SIZE_A4); doc.addPage(page); cs = new PDPageContentStream(doc, page); y = PAGE_HEIGHT - MARGIN; }
        }
        cs.close();
        return saveDoc(doc, "refunds_" + System.currentTimeMillis() + ".pdf");
    }

    // ===== Shared helper: girl list table ================================
    private String generateGirlListReport(List<Girl> girls, String title, String prefix) throws IOException {
        PDDocument doc = new PDDocument();
        PDPage page = new PDPage(PDPage.PAGE_SIZE_A4);
        doc.addPage(page);
        PDPageContentStream cs = new PDPageContentStream(doc, page);
        float y = PAGE_HEIGHT - MARGIN;
        y = drawHeader(cs, y, title);
        y = drawKeyVal(cs, y, "Total", String.valueOf(girls.size()));
        y = drawKeyVal(cs, y, "Generated", TSDF.format(new Date()));
        y -= 10;
        y = drawTableHeader(cs, y, new String[]{"Name", "Mobile", "Room", "Plan", "Admission"}, new float[]{130, 100, 60, 110, 90});
        for (Girl g : girls) {
            y = drawTableRow(cs, doc, page, y, new String[]{
                g.getName(), g.getMobile(),
                g.getRoomNumber() != null ? g.getRoomNumber() : "-",
                g.getPlanName() != null ? g.getPlanName() : "-",
                g.getAdmissionDate() != null ? SDF.format(g.getAdmissionDate()) : "-"
            }, new float[]{130, 100, 60, 110, 90}, false);
            if (y < 60) { cs.close(); page = new PDPage(PDPage.PAGE_SIZE_A4); doc.addPage(page); cs = new PDPageContentStream(doc, page); y = PAGE_HEIGHT - MARGIN; }
        }
        cs.close();
        return saveDoc(doc, prefix + "_" + System.currentTimeMillis() + ".pdf");
    }

    // ===== Drawing helpers =============================================
    private float drawHeader(PDPageContentStream cs, float y, String title) throws IOException {
        // Hostel name
        cs.beginText(); cs.setFont(PDType1Font.HELVETICA_BOLD, 16);
        cs.moveTextPositionByAmount(MARGIN, y);
        cs.drawString(hostelName); cs.endText();
        y -= 22;
        // Title
        cs.beginText(); cs.setFont(PDType1Font.HELVETICA_BOLD, 13);
        cs.moveTextPositionByAmount(MARGIN, y);
        cs.drawString(title); cs.endText();
        y -= 18;
        // Divider
        cs.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y);
        y -= 12;
        return y;
    }

    private float drawSectionTitle(PDPageContentStream cs, float y, String title) throws IOException {
        y -= 6;
        cs.beginText(); cs.setFont(PDType1Font.HELVETICA_BOLD, 12);
        cs.moveTextPositionByAmount(MARGIN, y);
        cs.drawString(title); cs.endText();
        y -= 16;
        return y;
    }

    private float drawKeyVal(PDPageContentStream cs, float y, String key, String value) throws IOException {
        cs.beginText(); cs.setFont(PDType1Font.HELVETICA_BOLD, 11);
        cs.moveTextPositionByAmount(MARGIN, y);
        cs.drawString(key + ": "); cs.endText();
        cs.beginText(); cs.setFont(PDType1Font.HELVETICA, 11);
        cs.moveTextPositionByAmount(MARGIN + 150, y);
        cs.drawString(value != null ? value : ""); cs.endText();
        y -= LINE_HEIGHT;
        return y;
    }

    private float drawTableHeader(PDPageContentStream cs, float y, String[] cols, float[] widths) throws IOException {
        float x = MARGIN;
        cs.setNonStrokingColor(new java.awt.Color(0xC7, 0xC7, 0xD9));
        cs.fillRect(MARGIN, y - 4, PAGE_WIDTH - 2 * MARGIN, LINE_HEIGHT);
        cs.setNonStrokingColor(java.awt.Color.BLACK);
        cs.beginText(); cs.setFont(PDType1Font.HELVETICA_BOLD, 10);
        for (int i = 0; i < cols.length; i++) {
            cs.moveTextPositionByAmount(i == 0 ? x : widths[i - 1], i == 0 ? y : 0);
            cs.drawString(cols[i]);
        }
        cs.endText();
        y -= LINE_HEIGHT;
        return y;
    }

    private float drawTableRow(PDPageContentStream cs, PDDocument doc, PDPage page,
                               float y, String[] cells, float[] widths, boolean shaded) throws IOException {
        float x = MARGIN;
        cs.beginText(); cs.setFont(PDType1Font.HELVETICA, 10);
        for (int i = 0; i < cells.length; i++) {
            String cell = cells[i] != null ? cells[i] : "";
            if (cell.length() > 22) cell = cell.substring(0, 19) + "...";
            cs.moveTextPositionByAmount(i == 0 ? x : widths[i - 1], i == 0 ? y : 0);
            cs.drawString(cell);
        }
        cs.endText();
        y -= LINE_HEIGHT;
        return y;
    }

    private String saveDoc(PDDocument doc, String filename) throws IOException {
        File dir = new File(AppConstants.REPORTS_DIR);
        if (!dir.exists()) dir.mkdirs();
        File out = new File(dir, filename);
        try {
            doc.save(out.getAbsolutePath());
        } catch (org.apache.pdfbox.exceptions.COSVisitorException e) {
            throw new IOException("PDF save failed: " + e.getMessage(), e);
        }
        doc.close();
        return out.getAbsolutePath();
    }
}
