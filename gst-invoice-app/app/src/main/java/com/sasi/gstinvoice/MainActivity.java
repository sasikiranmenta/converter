package com.sasi.gstinvoice;

import android.app.*;
import android.content.*;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.*;
import android.provider.MediaStore;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.text.*;
import java.util.*;

public class MainActivity extends Activity {
    private LinearLayout form;
    private SharedPreferences sp;
    private final Map<String, EditText> fields = new LinkedHashMap<>();

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        sp = getSharedPreferences("config", MODE_PRIVATE);
        buildUi();
    }

    private void buildUi() {
        ScrollView sv = new ScrollView(this);
        form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(16), dp(12), dp(16), dp(24));
        sv.addView(form);

        TextView title = new TextView(this);
        title.setText("GST INVOICE");
        title.setTextSize(22);
        title.setTypeface(null, 1);
        title.setGravity(Gravity.CENTER);
        form.addView(title, lp());

        Button settings = new Button(this);
        settings.setText("Settings / Configure");
        settings.setOnClickListener(v -> showSettings());
        form.addView(settings, lp());

        addField("invoice_no", "Invoice No.", "29");
        addField("date", "Date", new SimpleDateFormat("dd.MM.yy", Locale.US).format(new Date()));
        addField("buyer_name", "Customer Name", "");
        addField("buyer_address", "Customer Address", "");
        addField("buyer_state", "State", "A.P - 524 001");
        addField("buyer_pan", "PAN / IT No.", "");
        addField("buyer_gstin", "Customer GSTIN", "");
        addField("place_supply", "Place of Supply", "");
        addField("description", "Description of Services", "");
        addField("hsn_sac", "HSN / SAC", "997212");
        addField("amount", "Taxable Amount", "130000");
        addField("cgst_pct", "CGST %", "9");
        addField("sgst_pct", "SGST %", "9");
        addField("igst_pct", "IGST %", "0");

        Button generate = new Button(this);
        generate.setText("GENERATE PDF");
        generate.setOnClickListener(v -> generatePdf());
        form.addView(generate, lp());

        setContentView(sv);
    }

    private void addField(String key, String label, String def) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        TextView l = new TextView(this);
        l.setText(label);
        l.setTextSize(13);
        EditText e = new EditText(this);
        e.setText(def);
        e.setTextSize(16);
        e.setSingleLine(false);
        e.setBackgroundResource(com.sasi.gstinvoice.R.drawable.edit_bg);
        fields.put(key, e);
        box.addView(l, lp());
        box.addView(e, lp());
        form.addView(box, lp());
    }

    private void showSettings() {
        LinearLayout l = new LinearLayout(this);
        l.setPadding(dp(18), dp(10), dp(18), 0);
        l.setOrientation(LinearLayout.VERTICAL);

        EditText seller = settingEdit(l, "Seller / Business Name", sp.getString("seller","M.V. SURESH BABU"));
        EditText sellerAddr = settingEdit(l, "Seller Address", sp.getString("sellerAddr","13/242, MUNGAMURUVARI STREET, NELLORE\n- 524 001 (A.P.)"));
        EditText mob = settingEdit(l, "Mobile", sp.getString("mobile","99630 14114"));
        EditText gst = settingEdit(l, "Seller GSTIN", sp.getString("sellerGst","37ADLPM9062G1ZS"));
        EditText bank = settingEdit(l, "Bank Details", sp.getString("bank","Bank Name : CENTRAL BANK OF INDIA\nAccount No.:3455826404\nBranch & IFS Code:\nNELLORE\nMAIN BRANCH\nand CBIN0280839"));
        EditText decl = settingEdit(l, "Declaration", sp.getString("decl","We declare that this invoice shows the actual price of the Services described and that all particulars are true and correct.\nSubject to Nellore Jurisdiction."));
        ScrollView sc = new ScrollView(this); sc.addView(l);

        AlertDialog d = new AlertDialog.Builder(this)
            .setTitle("Invoice Configuration")
            .setView(sc)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Save", null).create();
        d.setOnShowListener(x -> d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            sp.edit().putString("seller",seller.getText().toString())
              .putString("sellerAddr",sellerAddr.getText().toString())
              .putString("mobile",mob.getText().toString())
              .putString("sellerGst",gst.getText().toString())
              .putString("bank",bank.getText().toString())
              .putString("decl",decl.getText().toString())
              .apply();
            Toast.makeText(this,"Configuration saved",Toast.LENGTH_SHORT).show();
            d.dismiss();
        }));
        d.show();
    }

    private EditText settingEdit(LinearLayout l, String label, String value) {
        TextView t = new TextView(this); t.setText(label); t.setTextSize(13); l.addView(t);
        EditText e = new EditText(this); e.setText(value); e.setMinLines(1); l.addView(e, lp());
        return e;
    }

    private void generatePdf() {
        try {
            double amt = num("amount");
            double cgstPct = num("cgst_pct"), sgstPct = num("sgst_pct"), igstPct = num("igst_pct");
            double cgst = amt*cgstPct/100.0, sgst = amt*sgstPct/100.0, igst = amt*igstPct/100.0, total = amt+cgst+sgst+igst;

            PdfDocument doc = InvoicePdf.create(
                val("invoice_no"), val("date"), val("buyer_name"), val("buyer_address"),
                val("buyer_state"), val("buyer_pan"), val("buyer_gstin"), val("place_supply"),
                val("description"), val("hsn_sac"), amt, cgstPct, sgstPct, igstPct,
                cgst, sgst, igst, total,
                sp.getString("seller","M.V. SURESH BABU"),
                sp.getString("sellerAddr","13/242, MUNGAMURUVARI STREET, NELLORE\n- 524 001 (A.P.)"),
                sp.getString("mobile","99630 14114"),
                sp.getString("sellerGst","37ADLPM9062G1ZS"),
                sp.getString("bank","Bank Name : CENTRAL BANK OF INDIA\nAccount No.:3455826404\nBranch & IFS Code:\nNELLORE\nMAIN BRANCH\nand CBIN0280839"),
                sp.getString("decl","We declare that this invoice shows the actual price of the Services described and that all particulars are true and correct.\nSubject to Nellore Jurisdiction.")
            );

            String fileName = "Invoice_" + val("invoice_no") + "_" + System.currentTimeMillis() + ".pdf";
            if (Build.VERSION.SDK_INT >= 29) {
                ContentResolver cr = getContentResolver();
                ContentValues values = new ContentValues();
                values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
                values.put(MediaStore.Downloads.MIME_TYPE, "application/pdf");
                values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
                Uri uri = cr.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                if (uri == null) throw new IOException("Could not create Downloads file");
                try (OutputStream os = cr.openOutputStream(uri)) {
                    if (os == null) throw new IOException("Could not open Downloads file");
                    doc.writeTo(os);
                }
                doc.close();
                Toast.makeText(this, "PDF saved in Downloads/"+fileName, Toast.LENGTH_LONG).show();
                viewPdf(uri);
            } else {
                File downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                if (!downloads.exists() && !downloads.mkdirs()) throw new IOException("Cannot create Downloads");
                File out = new File(downloads, fileName);
                try (FileOutputStream fos = new FileOutputStream(out)) { doc.writeTo(fos); }
                doc.close();
                Toast.makeText(this, "PDF saved in Downloads/"+fileName, Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "PDF generation failed: "+e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void viewPdf(Uri uri) {
        Intent view = new Intent(Intent.ACTION_VIEW);
        view.setDataAndType(uri, "application/pdf");
        view.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try { startActivity(view); } catch (Exception ignored) {}
    }

    private String val(String k){ return fields.get(k).getText().toString().trim(); }
    private double num(String k){ try{return Double.parseDouble(val(k).replace(",",""));}catch(Exception e){return 0;} }
    private LinearLayout.LayoutParams lp(){ return new LinearLayout.LayoutParams(-1, -2); }
    private int dp(int n){return Math.round(n*getResources().getDisplayMetrics().density);}
}
