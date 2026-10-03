package com.sasi.gstinvoice;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import java.util.Locale;

public final class InvoicePdf {
    private InvoicePdf() {}

    public static PdfDocument create(InvoiceConfig config, InvoiceConfig.Page page) {
        PdfDocument doc = new PdfDocument();
        PdfDocument.PageInfo info = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page pdfPage = doc.startPage(info);
        Canvas c = pdfPage.getCanvas();
        c.drawColor(Color.WHITE);

        Paint normal = paint(8.7f, false, false);
        Paint bold = paint(8.9f, true, false);
        Paint italic = paint(8.4f, false, true);
        Paint line = new Paint(Paint.ANTI_ALIAS_FLAG);
        line.setColor(Color.BLACK);
        line.setStyle(Paint.Style.STROKE);
        line.setStrokeWidth(0.75f);

        final float L = 88, R = 516, T = 58;
        final float headerBottom = 145, buyerBottom = 254, tableBottom = 543, amountBottom = 575, declarationTop = 575, footerBottom = 760, signatureBottom = 824;
        final float sellerRight = 295, metaRight = 444;

        bold.setTextSize(13.2f);
        bold.setUnderlineText(true);
        center(c, "TAX INVOICE", (L + R) / 2f, 40, bold);
        bold.setUnderlineText(false);

        c.drawRect(L, T, R, signatureBottom, line);
        c.drawLine(sellerRight, T, sellerRight, headerBottom, line);
        c.drawLine(metaRight, T, metaRight, headerBottom, line);
        c.drawLine(L, 105, R, 105, line);
        c.drawLine(L, headerBottom, R, headerBottom, line);
        c.drawLine(L, buyerBottom, R, buyerBottom, line);

        bold.setTextSize(14.5f);
        center(c, config.sellerName, (L + sellerRight) / 2f, 96, bold);
        normal.setTextSize(8.2f);
        center(c, config.sellerAddress.replace("\n", "  "), (L + sellerRight) / 2f, 111, normal);
        bold.setTextSize(8.7f);
        center(c, "GSTIN : " + config.sellerGstin, (L + sellerRight) / 2f, 133, bold);
        bold.setTextSize(8.5f);
        center(c, config.sellerPhone, (L + sellerRight) / 2f + 22, 70, bold);

        normal.setTextSize(8.8f);
        left(c, "Invoice No. : " + page.invoiceNo, 302, 84, normal);
        left(c, "Date : " + page.invoiceDate, 449, 84, normal);
        left(c, "Buyer’s Order No.", 302, 119, normal);
        left(c, "Date : " + page.buyerOrderDate, 449, 119, normal);

        float y = 160;
        y = labelLine(c, "Name : ", page.buyerName, L + 6, y, 505, normal);
        y = addressLines(c, page.buyerAddress, L + 6, y, 505, normal);
        y = labelLine(c, "State : ", page.buyerState, L + 6, y, 505, normal);
        y = labelLine(c, "PAN / IT No. : ", page.buyerPan, L + 6, y, 505, normal);
        y = labelLine(c, "GSTIN : ", page.buyerGstin, L + 6, y, 505, bold);
        labelLine(c, "Place of Supply : ", page.placeOfSupply, L + 6, y, 505, normal);

        float x1 = 120, x2 = 374, x3 = 458;
        c.drawLine(L, 274, R, 274, line);
        c.drawLine(x1, buyerBottom, x1, tableBottom, line);
        c.drawLine(x2, buyerBottom, x2, tableBottom, line);
        c.drawLine(x3, buyerBottom, x3, tableBottom, line);
        center(c, "S.No.", (L + x1) / 2f, 268, normal);
        center(c, "DESCRIPTION OF SERVICES", (x1 + x2) / 2f, 268, normal);
        center(c, "HSN/SAC", (x2 + x3) / 2f, 268, normal);
        center(c, "AMOUNT", (x3 + R) / 2f, 268, normal);

        bold.setTextSize(8.8f);
        left(c, "1.", 95, 303, bold);
        normal.setTextSize(8.6f);
        wrap(c, page.description, x1 + 8, 300, x2 - 8, 12, normal);
        left(c, page.hsnSac, x2 + 10, 303, normal);
        right(c, InvoiceConfig.money(page.taxableAmount), R - 8, 303, normal);

        italic.setTextSize(8.2f);
        left(c, "E. & O.E.", x1 + 8, tableBottom - 9, italic);

        float taxTop = 455, row = 22;
        c.drawLine(x2, taxTop, R, taxTop, line);
        drawTaxRow(c, line, normal, "CGST  " + pct(page.cgstRate) + " %", page.cgst(), taxTop, row, x2, x3, R);
        drawTaxRow(c, line, normal, "SGST  " + pct(page.sgstRate) + " %", page.sgst(), taxTop + row, row, x2, x3, R);
        drawTaxRow(c, line, normal, "IGST   " + pct(page.igstRate) + " %", page.igst(), taxTop + 2 * row, row, x2, x3, R);
        drawTaxRow(c, line, bold, "TOTAL", page.total(), taxTop + 3 * row, row, x2, x3, R);

        c.drawLine(L, tableBottom, R, tableBottom, line);
        c.drawLine(L, amountBottom, R, amountBottom, line);
        bold.setTextSize(8.3f);
        wrap(c, "Amount Chargeable (Inwords) : " + numberToWords(Math.round(page.total())) + " RUPEES ONLY", L + 6, tableBottom + 18, R - 6, 12, bold);

        c.drawLine(L, declarationTop, R, declarationTop, line);
        c.drawLine(350, declarationTop, 350, footerBottom, line);
        c.drawLine(L, footerBottom, R, footerBottom, line);
        bold.setTextSize(8.8f);
        left(c, "Declaration :", L + 6, declarationTop + 16, bold);
        normal.setTextSize(8.1f);
        wrap(c, config.declaration, L + 8, declarationTop + 35, 342, 12, normal);

        normal.setTextSize(8.3f);
        left(c, "Bank Name : " + page.bankName, 360, declarationTop + 36, normal);
        left(c, "Account No.: " + page.accountNo, 360, declarationTop + 52, normal);
        wrap(c, "Branch & IFS Code : " + page.branchIfsc, 360, declarationTop + 68, R - 8, 12, normal);

        c.drawLine(350, footerBottom, 350, signatureBottom, line);
        bold.setTextSize(8.8f);
        center(c, "For " + page.signatoryName, 433, 705, bold);
        italic.setTextSize(8.7f);
        center(c, "Customer’s Seal and Signature", 219, 813, italic);
        normal.setTextSize(8.7f);
        center(c, "Authorised Signatory", 433, 816, normal);

        doc.finishPage(pdfPage);
        return doc;
    }

    private static float labelLine(Canvas c, String label, String value, float x, float y, float maxX, Paint p) {
        c.drawText(label + (value == null ? "" : value), x, y, p);
        return y + 15;
    }

    private static float addressLines(Canvas c, String address, float x, float y, float maxX, Paint p) {
        if (address == null || address.trim().isEmpty()) return y + 15;
        String[] lines = address.replace("\r", "").split("\n");
        if (lines.length == 1) {
            wrap(c, "Address : " + lines[0], x, y, maxX, 15, p);
            return y + 30;
        }
        c.drawText("Address : " + lines[0], x, y, p);
        y += 15;
        for (int i = 1; i < lines.length; i++) { c.drawText(lines[i], x + 48, y, p); y += 15; }
        return y;
    }

    private static void drawTaxRow(Canvas c, Paint line, Paint text, String label, double value, float top, float row, float x2, float x3, float r) {
        c.drawLine(x2, top + row, r, top + row, line);
        c.drawLine(x3, top, x3, top + row, line);
        right(c, label, x3 - 7, top + 15, text);
        if (label.startsWith("IGST") && value == 0) return;
        right(c, InvoiceConfig.money(value), r - 7, top + 15, text);
    }

    private static Paint paint(float size, boolean isBold, boolean italic) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(Color.BLACK);
        p.setTextSize(size);
        p.setTypeface(Typeface.create("sans", isBold ? Typeface.BOLD : italic ? Typeface.ITALIC : Typeface.NORMAL));
        return p;
    }

    private static String pct(double n) { return String.format(Locale.US, "%.0f", n); }
    private static void left(Canvas c, String s, float x, float y, Paint p) { c.drawText(s == null ? "" : s, x, y, p); }
    private static void right(Canvas c, String s, float x, float y, Paint p) { String v = s == null ? "" : s; c.drawText(v, x - p.measureText(v), y, p); }
    private static void center(Canvas c, String s, float x, float y, Paint p) { String v = s == null ? "" : s; c.drawText(v, x - p.measureText(v) / 2f, y, p); }

    private static void wrap(Canvas c, String s, float x, float y, float maxX, float step, Paint p) {
        if (s == null) return;
        for (String para : s.replace("\r", "").split("\n", -1)) {
            String current = "";
            if (para.trim().isEmpty()) { y += step; continue; }
            for (String word : para.split(" ")) {
                String test = current.isEmpty() ? word : current + " " + word;
                if (!current.isEmpty() && x + p.measureText(test) > maxX) {
                    c.drawText(current, x, y, p);
                    y += step;
                    current = word;
                } else current = test;
            }
            if (!current.isEmpty()) { c.drawText(current, x, y, p); y += step; }
        }
    }

    private static String numberToWords(long n) {
        if (n == 0) return "ZERO";
        return indian(n).trim().toUpperCase(Locale.US);
    }

    private static String indian(long n) {
        if (n >= 10000000) return indian(n / 10000000) + " CRORE" + tail(n % 10000000);
        if (n >= 100000) return indian(n / 100000) + " LAKH" + tail(n % 100000);
        if (n >= 1000) return indian(n / 1000) + " THOUSAND" + tail(n % 1000);
        if (n >= 100) return one(n / 100) + " HUNDRED" + tail(n % 100);
        if (n >= 20) return tens(n);
        return one(n);
    }

    private static String tail(long n) { return n == 0 ? "" : " " + indian(n); }

    private static String one(long n) {
        String[] a = {"ZERO","ONE","TWO","THREE","FOUR","FIVE","SIX","SEVEN","EIGHT","NINE","TEN","ELEVEN","TWELVE","THIRTEEN","FOURTEEN","FIFTEEN","SIXTEEN","SEVENTEEN","EIGHTEEN","NINETEEN"};
        return a[(int)n];
    }

    private static String tens(long n) {
        String[] a = {"","","TWENTY","THIRTY","FORTY","FIFTY","SIXTY","SEVENTY","EIGHTY","NINETY"};
        return a[(int)(n / 10)] + (n % 10 == 0 ? "" : " " + one(n % 10));
    }
}