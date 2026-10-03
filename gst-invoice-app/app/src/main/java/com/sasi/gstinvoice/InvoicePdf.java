package com.sasi.gstinvoice;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import java.util.Locale;

public final class InvoicePdf {
    private InvoicePdf() {}

    // Geometry is based on the supplied scanned invoice (A4 page, 595 x 842 pt).
    // The invoice itself occupies the upper ~745 pt of the page.
    private static final float L = 63f;
    private static final float R = 554f;
    private static final float T = 42f;
    private static final float SELLER_RIGHT = 292f;
    private static final float META_RIGHT = 445f;
    private static final float HEADER_MID = 90f;
    private static final float HEADER_BOTTOM = 131f;
    private static final float BUYER_BOTTOM = 245f;
    private static final float TABLE_HEADER_BOTTOM = 267f;
    private static final float TABLE_BOTTOM = 550f;
    private static final float AMOUNT_BOTTOM = 590f;
    private static final float DECLARATION_SPLIT = 329f;
    private static final float FOOTER_BOTTOM = 664f;
    private static final float SIGNATURE_BOTTOM = 745f;

    private static final float X_SNO = 102f;
    private static final float X_DESC = 375f;
    private static final float X_HSN = 458f;

    public static PdfDocument create(InvoiceConfig config, InvoiceConfig.Page page) {
        PdfDocument doc = new PdfDocument();
        PdfDocument.PageInfo info = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page pdfPage = doc.startPage(info);
        renderPage(pdfPage.getCanvas(), config, page);
        doc.finishPage(pdfPage);
        return doc;
    }

    /** Draws directly onto a PdfDocument canvas so all invoice text remains vector text. */
    public static void renderPage(Canvas c, InvoiceConfig config, InvoiceConfig.Page page) {
        c.drawColor(Color.WHITE);

        Paint normal = paint(8.8f, false, false);
        Paint bold = paint(9.0f, true, false);
        Paint italic = paint(8.6f, false, true);
        Paint normal = paint(8.8f, false, false);
        Paint bold = paint(9.0f, true, false);
        Paint italic = paint(8.6f, false, true);
        Paint line = new Paint(Paint.ANTI_ALIAS_FLAG);
        line.setColor(Color.BLACK);
        line.setStyle(Paint.Style.STROKE);
        line.setStrokeWidth(0.75f);

        bold.setTextSize(13.2f);
        bold.setUnderlineText(true);
        center(c, "TAX INVOICE", (L + R) / 2f, 35f, bold);
        bold.setUnderlineText(false);

        // Main invoice frame and header divisions.
        c.drawRect(L, T, R, SIGNATURE_BOTTOM, line);
        c.drawLine(SELLER_RIGHT, T, SELLER_RIGHT, HEADER_BOTTOM, line);
        c.drawLine(META_RIGHT, T, META_RIGHT, HEADER_BOTTOM, line);
        c.drawLine(L, HEADER_MID, R, HEADER_MID, line);
        c.drawLine(L, HEADER_BOTTOM, R, HEADER_BOTTOM, line);
        c.drawLine(L, BUYER_BOTTOM, R, BUYER_BOTTOM, line);

        // Seller block - positions follow the scanned bill.
        bold.setTextSize(9.0f);
        center(c, "Mob. " + safe(config.sellerPhone), (L + SELLER_RIGHT) / 2f, 52f, bold);

        bold.setTextSize(14.5f);
        center(c, config.sellerName, (L + SELLER_RIGHT) / 2f, 81f, bold);

        normal.setTextSize(8.7f);
        center(c, "13/242, MUNGAMURUVARI STREET, NELLORE", (L + SELLER_RIGHT) / 2f, 97f, normal);
        center(c, "- 524 001 (A.P.)", (L + SELLER_RIGHT) / 2f, 110f, normal);

        bold.setTextSize(8.8f);
        center(c, "GSTIN : " + safe(config.sellerGstin), (L + SELLER_RIGHT) / 2f, 123f, bold);

        // Invoice metadata block.
        normal.setTextSize(9.0f);
        left(c, "Invoice No. : " + safe(page.invoiceNo), SELLER_RIGHT + 6f, 67f, normal);
        left(c, "Date : " + safe(page.invoiceDate), META_RIGHT + 7f, 67f, normal);
        left(c, "Buyer’s Order No.", SELLER_RIGHT + 6f, 109f, normal);
        left(c, "Date : " + safe(page.buyerOrderDate), META_RIGHT + 7f, 109f, normal);

        // Buyer block.
        float y = 143f;
        y = labelLine(c, "Name : ", page.buyerName, L + 7f, y, R - 7f, bold);
        y = addressLines(c, page.buyerAddress, L + 7f, y, R - 7f, normal);
        y = labelLine(c, "State : ", page.buyerState, L + 7f, y, R - 7f, normal);
        y = labelLine(c, "PAN / IT No. : ", page.buyerPan, L + 7f, y, R - 7f, normal);
        y = labelLine(c, "GSTIN : ", page.buyerGstin, L + 7f, y, R - 7f, bold);
        labelLine(c, "Place of Supply : ", page.placeOfSupply, L + 7f, y, R - 7f, normal);

        // Service table.
        c.drawLine(L, TABLE_HEADER_BOTTOM, R, TABLE_HEADER_BOTTOM, line);
        c.drawLine(X_SNO, BUYER_BOTTOM, X_SNO, TABLE_BOTTOM, line);
        c.drawLine(X_DESC, BUYER_BOTTOM, X_DESC, TABLE_BOTTOM, line);
        c.drawLine(X_HSN, BUYER_BOTTOM, X_HSN, TABLE_BOTTOM, line);

        center(c, "S.No.", (L + X_SNO) / 2f, 261f, normal);
        center(c, "DESCRIPTION OF SERVICES", (X_SNO + X_DESC) / 2f, 261f, normal);
        center(c, "HSN/SAC", (X_DESC + X_HSN) / 2f, 261f, normal);
        center(c, "AMOUNT", (X_HSN + R) / 2f, 261f, normal);

        bold.setTextSize(9.0f);
        left(c, "1.", L + 10f, 290f, bold);
        normal.setTextSize(8.8f);
        wrap(c, page.description, X_SNO + 8f, 289f, X_DESC - 8f, 14f, normal);
        left(c, safe(page.hsnSac), X_DESC + 8f, 290f, normal);
        right(c, InvoiceConfig.money(page.taxableAmount), R - 8f, 290f, normal);

        italic.setTextSize(8.5f);
        left(c, "E. & O.E.", X_SNO + 8f, TABLE_BOTTOM - 8f, italic);

        // Tax block in the lower part of the amount columns.
        final float taxTop = 457f;
        final float taxRow = 24f;
        c.drawLine(X_DESC, taxTop, R, taxTop, line);
        drawTaxRow(c, line, normal, "CGST  " + pct(page.cgstRate) + " %", page.cgst(), taxTop, taxRow);
        drawTaxRow(c, line, normal, "SGST  " + pct(page.sgstRate) + " %", page.sgst(), taxTop + taxRow, taxRow);
        drawTaxRow(c, line, normal, "IGST  " + pct(page.igstRate) + " %", page.igst(), taxTop + 2 * taxRow, taxRow);
        drawTaxRow(c, line, bold, "TOTAL", page.total(), taxTop + 3 * taxRow, taxRow);

        // Amount-in-words strip.
        c.drawLine(L, TABLE_BOTTOM, R, TABLE_BOTTOM, line);
        c.drawLine(L, AMOUNT_BOTTOM, R, AMOUNT_BOTTOM, line);
        bold.setTextSize(8.6f);
        left(c, "Amount Chargeable (Inwords) :", L + 7f, TABLE_BOTTOM + 24f, bold);
        String words = numberToWords(Math.round(page.total())) + " RUPEES ONLY";
        float labelWidth = bold.measureText("Amount Chargeable (Inwords) : ");
        wrap(c, words, L + 7f + labelWidth, TABLE_BOTTOM + 24f, R - 7f, 12f, bold);

        // Declaration / bank block.
        c.drawLine(L, AMOUNT_BOTTOM, R, AMOUNT_BOTTOM, line);
        c.drawLine(DECLARATION_SPLIT, AMOUNT_BOTTOM, DECLARATION_SPLIT, FOOTER_BOTTOM, line);
        c.drawLine(L, FOOTER_BOTTOM, R, FOOTER_BOTTOM, line);

        bold.setTextSize(8.8f);
        left(c, "Declaration :", L + 7f, AMOUNT_BOTTOM + 16f, bold);
        normal.setTextSize(8.1f);
        left(c, "✧", L + 7f, AMOUNT_BOTTOM + 35f, normal);
        wrap(c, "We declare that this invoice shows the actual price of the Services described and that all particulars are true and correct.",
                L + 22f, AMOUNT_BOTTOM + 35f, DECLARATION_SPLIT - 8f, 12f, normal);
        left(c, "✧", L + 7f, AMOUNT_BOTTOM + 69f, normal);
        wrap(c, "Subject to Nellore Jurisdiction.", L + 22f, AMOUNT_BOTTOM + 69f, DECLARATION_SPLIT - 8f, 12f, normal);

        normal.setTextSize(8.6f);
        left(c, "Bank Name : " + safe(page.bankName), DECLARATION_SPLIT + 7f, AMOUNT_BOTTOM + 25f, normal);
        left(c, "Account No.: " + safe(page.accountNo), DECLARATION_SPLIT + 7f, AMOUNT_BOTTOM + 43f, normal);
        wrap(c, "Branch & IFS Code : " + safe(page.branchIfsc),
                DECLARATION_SPLIT + 7f, AMOUNT_BOTTOM + 61f, R - 8f, 12f, normal);

        // Signature area.
        c.drawLine(DECLARATION_SPLIT, FOOTER_BOTTOM, DECLARATION_SPLIT, SIGNATURE_BOTTOM, line);
        bold.setTextSize(8.8f);
        center(c, "For " + safe(page.signatoryName), (DECLARATION_SPLIT + R) / 2f, 704f, bold);
        italic.setTextSize(8.7f);
        center(c, "Customer’s Seal and Signature", (L + DECLARATION_SPLIT) / 2f, 728f, italic);
        normal.setTextSize(8.7f);
        center(c, "Authorised Signatory", (DECLARATION_SPLIT + R) / 2f, 738f, normal);

    }

    private static float labelLine(Canvas c, String label, String value, float x, float y, float maxX, Paint p) {
        c.drawText(label + safe(value), x, y, p);
        return y + 14.5f;
    }

    private static float addressLines(Canvas c, String address, float x, float y, float maxX, Paint p) {
        if (address == null || address.trim().isEmpty()) return y + 14.5f;
        String[] lines = address.replace("\r", "").split("\n");
        if (lines.length == 1) {
            wrap(c, "Address : " + lines[0], x, y, maxX, 14.5f, p);
            return y + 29f;
        }
        c.drawText("Address : " + lines[0], x, y, p);
        y += 14.5f;
        for (int i = 1; i < lines.length; i++) {
            c.drawText(lines[i], x + 48f, y, p);
            y += 14.5f;
        }
        return y;
    }

    private static void drawTaxRow(Canvas c, Paint line, Paint text, String label, double value, float top, float row) {
        c.drawLine(X_DESC, top + row, R, top + row, line);
        c.drawLine(X_HSN, top, X_HSN, top + row, line);
        right(c, label, X_HSN - 7f, top + 16f, text);
        if (label.startsWith("IGST") && value == 0) return;
        right(c, InvoiceConfig.money(value), R - 7f, top + 16f, text);
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

    private static String safe(String s) { return s == null ? "" : s.trim(); }
}
