package com.pharmadesk.backend.sales.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.pharmadesk.backend.pharmacy.exception.ResourceNotFoundException;
import com.pharmadesk.backend.sales.model.PharmacyBill;
import com.pharmadesk.backend.sales.model.PharmacyBillItem;
import com.pharmadesk.backend.sales.repository.PharmacyBillRepository;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

@Service
public class BillPdfService {

    private final PharmacyBillRepository billRepository;

    public BillPdfService(PharmacyBillRepository billRepository) {
        this.billRepository = billRepository;
    }

    public byte[] generateBillPdf(Long billId) {
        PharmacyBill bill = billRepository.findById(billId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found"));

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter writer = PdfWriter.getInstance(document, baos);
            document.open();

            // Fonts
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);

            // Hospital Header
            Paragraph header = new Paragraph("PharmaDesk Hospital Pharmacy", titleFont);
            header.setAlignment(Element.ALIGN_CENTER);
            document.add(header);

            Paragraph address = new Paragraph("123 Health Avenue, Medical City\nPhone: (555) 123-4567 | GSTIN: 22AAAAA0000A1Z5", normalFont);
            address.setAlignment(Element.ALIGN_CENTER);
            document.add(address);
            
            document.add(new Paragraph("\n"));

            // Barcode
            PdfContentByte cb = writer.getDirectContent();
            Barcode128 barcode = new Barcode128();
            barcode.setCode(bill.getBillNumber());
            Image barcodeImage = barcode.createImageWithBarcode(cb, null, null);
            barcodeImage.setAlignment(Element.ALIGN_RIGHT);
            barcodeImage.scalePercent(150);
            document.add(barcodeImage);

            // Bill Info Table
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.setSpacingBefore(10f);

            PdfPCell cell1 = new PdfPCell(new Phrase("Bill No: " + bill.getBillNumber(), boldFont));
            cell1.setBorder(Rectangle.NO_BORDER);
            infoTable.addCell(cell1);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
            PdfPCell cell2 = new PdfPCell(new Phrase("Date: " + bill.getBillingDate().format(formatter), boldFont));
            cell2.setBorder(Rectangle.NO_BORDER);
            cell2.setHorizontalAlignment(Element.ALIGN_RIGHT);
            infoTable.addCell(cell2);

            PdfPCell cell3 = new PdfPCell(new Phrase("Patient: " + (bill.getPatientName() != null ? bill.getPatientName() : "Walk-in"), normalFont));
            cell3.setBorder(Rectangle.NO_BORDER);
            infoTable.addCell(cell3);

            PdfPCell cell4 = new PdfPCell(new Phrase("Doctor: " + (bill.getDoctorName() != null ? bill.getDoctorName() : "Self"), normalFont));
            cell4.setBorder(Rectangle.NO_BORDER);
            cell4.setHorizontalAlignment(Element.ALIGN_RIGHT);
            infoTable.addCell(cell4);

            document.add(infoTable);
            document.add(new Paragraph("\n"));

            // Items Table
            PdfPTable itemTable = new PdfPTable(6);
            itemTable.setWidthPercentage(100);
            itemTable.setWidths(new float[]{1, 4, 1.5f, 1.5f, 1.5f, 2});

            String[] headers = {"S.No", "Description", "Batch", "Qty", "Price", "Amount"};
            for (String h : headers) {
                PdfPCell c = new PdfPCell(new Phrase(h, headerFont));
                c.setBackgroundColor(new java.awt.Color(230, 230, 230));
                c.setPadding(5);
                itemTable.addCell(c);
            }

            int sno = 1;
            for (PharmacyBillItem item : bill.getItems()) {
                itemTable.addCell(new Phrase(String.valueOf(sno++), normalFont));
                String medName = item.getStock() != null ? item.getStock().getMedicine().getName() : "Unknown";
                itemTable.addCell(new Phrase(medName, normalFont));
                String batch = item.getStock() != null ? item.getStock().getBatchNumber() : "-";
                itemTable.addCell(new Phrase(batch, normalFont));
                itemTable.addCell(new Phrase(String.valueOf(item.getQuantity()), normalFont));
                itemTable.addCell(new Phrase(String.format("%.2f", item.getUnitPrice()), normalFont));
                itemTable.addCell(new Phrase(String.format("%.2f", item.getNetAmount()), normalFont));
            }
            document.add(itemTable);
            
            document.add(new Paragraph("\n"));

            // Totals
            PdfPTable totalTable = new PdfPTable(2);
            totalTable.setWidthPercentage(100);
            totalTable.setWidths(new float[]{8, 2});

            addTotalRow(totalTable, "Sub Total:", bill.getSubTotal(), normalFont);
            addTotalRow(totalTable, "Discount:", bill.getDiscountAmount(), normalFont);
            addTotalRow(totalTable, "Tax Amount:", bill.getTaxAmount(), normalFont);
            
            PdfPCell labelCell = new PdfPCell(new Phrase("Net Amount:", boldFont));
            labelCell.setBorder(Rectangle.NO_BORDER);
            labelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalTable.addCell(labelCell);

            PdfPCell valCell = new PdfPCell(new Phrase(String.format("%.2f", bill.getNetAmount()), boldFont));
            valCell.setBorder(Rectangle.NO_BORDER);
            valCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalTable.addCell(valCell);

            document.add(totalTable);

            // Footer
            document.add(new Paragraph("\n\n\n"));
            Paragraph signature = new Paragraph("Authorized Signatory", normalFont);
            signature.setAlignment(Element.ALIGN_RIGHT);
            document.add(signature);

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF: " + e.getMessage(), e);
        }
    }

    private void addTotalRow(PdfPTable table, String label, BigDecimal value, Font font) {
        if (value == null) value = BigDecimal.ZERO;
        PdfPCell c1 = new PdfPCell(new Phrase(label, font));
        c1.setBorder(Rectangle.NO_BORDER);
        c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(c1);

        PdfPCell c2 = new PdfPCell(new Phrase(String.format("%.2f", value), font));
        c2.setBorder(Rectangle.NO_BORDER);
        c2.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(c2);
    }
}
