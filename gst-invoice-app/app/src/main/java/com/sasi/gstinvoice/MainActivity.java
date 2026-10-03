package com.sasi.gstinvoice;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends Activity {
    private ConfigRepository repo;
    private InvoiceConfig config;
    private final Map<String, EditText> inputs = new LinkedHashMap<>();
    private int editingPage = -1;
    private TextView totalPreview;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        repo = new ConfigRepository(this);
        config = repo.load();
        showHome();
    }

    private int dp(int v) { return Math.round(v * getResources().getDisplayMetrics().density); }

    private GradientDrawable bg(int color, float radius) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp((int) radius));
        g.setStroke(dp(1), Color.rgb(220, 220, 220));
        return g;
    }

    private TextView heading(String text, int size) {
        TextView t = new TextView(this);
        t.setText(text);
        t.setTextSize(size);
        t.setTextColor(Color.rgb(25, 25, 28));
        t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        t.setPadding(dp(4), dp(5), dp(4), dp(5));
        return t;
    }

    private TextView caption(String text) {
        TextView t = new TextView(this);
        t.setText(text);
        t.setTextSize(13);
        t.setTextColor(Color.rgb(105, 105, 110));
        t.setPadding(dp(4), dp(2), dp(4), dp(8));
        return t;
    }

    private Button action(String text, boolean primary) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextSize(14);
        b.setAllCaps(false);
        b.setTextColor(primary ? Color.WHITE : Color.rgb(35,35,40));
        b.setPadding(dp(12), dp(4), dp(12), dp(4));
        b.setBackground(bg(primary ? Color.rgb(35, 91, 135) : Color.WHITE, 10));
        b.setMinHeight(dp(48));
        return b;
    }

    private LinearLayout card() {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(dp(16), dp(14), dp(16), dp(14));
        c.setBackground(bg(Color.WHITE, 12));
        return c;
    }

    private LinearLayout screenRoot(ScrollView scroll) {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16), dp(12), dp(16), dp(28));
        scroll.addView(root);
        return root;
    }

    private LinearLayout.LayoutParams match() { return new LinearLayout.LayoutParams(-1, -2); }
    private LinearLayout.LayoutParams spaced(int bottom) { LinearLayout.LayoutParams p = match(); p.bottomMargin = dp(bottom); return p; }
    private LinearLayout.LayoutParams weight(float w, int rightDp) { LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, -2, w); p.rightMargin = dp(rightDp); return p; }

    private void showHome() {
        ScrollView sv = new ScrollView(this);
        LinearLayout root = screenRoot(sv);
        root.addView(heading("GST Invoice", 27));
        root.addView(caption("Offline invoice generator · bundled invoice configuration · PDF export"), spaced(8));

        LinearLayout intro = card();
        intro.addView(heading("Invoice pages", 19));
        intro.addView(caption(config.pages.size() + " saved page configuration" + (config.pages.size() == 1 ? "" : "s") + ". Choose a page to edit or generate."));
        root.addView(intro, spaced(14));

        for (int i = 0; i < config.pages.size(); i++) addPageCard(root, i);

        Button add = action("＋  Add new page", true);
        add.setOnClickListener(v -> addNewPage());
        root.addView(add, spaced(10));

        Button settings = action("⚙  Business & app settings", false);
        settings.setOnClickListener(v -> showSettings());
        root.addView(settings, spaced(10));

        TextView foot = caption("Everything is stored locally on the phone. Internet access is not required.");
        foot.setGravity(Gravity.CENTER);
        root.addView(foot);
        setContentView(sv);
    }

    private void addPageCard(LinearLayout root, int index) {
        InvoiceConfig.Page p = config.pages.get(index);
        LinearLayout c = card();

        TextView top = heading("Page " + (index + 1) + "  ·  Invoice " + safe(p.invoiceNo), 17);
        c.addView(top);
        c.addView(caption(safe(p.pageName)));
        TextView customer = new TextView(this);
        customer.setText(safe(p.buyerName));
        customer.setTextSize(14);
        customer.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        customer.setTextColor(Color.rgb(55,55,60));
        customer.setPadding(dp(4),0,dp(4),dp(5));
        c.addView(customer);

        TextView amount = new TextView(this);
        amount.setText("Total  ₹ " + InvoiceConfig.money(p.total()));
        amount.setTextSize(14);
        amount.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        amount.setTextColor(Color.rgb(40, 90, 70));
        amount.setPadding(dp(4), dp(2), dp(4), dp(10));
        c.addView(amount);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        Button edit = action("Edit", false);
        edit.setOnClickListener(v -> showForm(index));
        Button generate = action("Generate PDF", true);
        generate.setOnClickListener(v -> generate(index, false));
        Button more = action("More", false);
        more.setOnClickListener(v -> showPageActions(index));
        row.addView(edit, weight(1, 6));
        row.addView(generate, weight(1, 6));
        row.addView(more, weight(1, 0));
        c.addView(row);
        root.addView(c, spaced(12));
    }

    private void showPageActions(final int index) {
        String[] choices = {"Duplicate page", "Rename page", "Delete page"};
        new android.app.AlertDialog.Builder(this)
            .setTitle("Page " + (index + 1))
            .setItems(choices, (d, which) -> {
                if (which == 0) duplicatePage(index);
                else if (which == 1) renamePage(index);
                else deletePage(index);
            }).show();
    }

    private void addNewPage() {
        int next = 1;
        for (InvoiceConfig.Page p : config.pages) {
            try { next = Math.max(next, Integer.parseInt(p.invoiceNo.replaceAll("[^0-9]", "")) + 1); }
            catch (Exception ignored) {}
        }
        InvoiceConfig.Page p = InvoiceConfig.blankPage("New Invoice", String.valueOf(next), config);
        config.pages.add(p);
        repo.save(config);
        showForm(config.pages.size() - 1);
    }

    private void duplicatePage(int index) {
        InvoiceConfig.Page p = config.pages.get(index).copy();
        p.pageName = p.pageName + " Copy";
        int next = index + 1;
        try { next = Integer.parseInt(p.invoiceNo.replaceAll("[^0-9]", "")) + 1; } catch (Exception ignored) {}
        p.invoiceNo = String.valueOf(next);
        config.pages.add(p);
        repo.save(config);
        showHome();
    }

    private void renamePage(final int index) {
        EditText e = new EditText(this);
        e.setText(config.pages.get(index).pageName);
        e.setSingleLine(true);
        e.setPadding(dp(10),dp(10),dp(10),dp(10));
        new android.app.AlertDialog.Builder(this)
            .setTitle("Page name")
            .setView(e)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Save", (d,w) -> {
                String s=e.getText().toString().trim();
                if(!s.isEmpty()) config.pages.get(index).pageName=s;
                repo.save(config);
                showHome();
            }).show();
    }

    private void deletePage(final int index) {
        if (config.pages.size() <= 1) {
            Toast.makeText(this, "Keep at least one page configuration.", Toast.LENGTH_SHORT).show();
            return;
        }
        new android.app.AlertDialog.Builder(this)
            .setTitle("Delete page?")
            .setMessage("Page " + (index + 1) + " and its invoice values will be removed from this device.")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Delete", (d,w) -> {
                config.pages.remove(index);
                repo.save(config);
                showHome();
            }).show();
    }

    private void showForm(int pageIndex) {
        editingPage = pageIndex;
        InvoiceConfig.Page p = config.pages.get(pageIndex);
        inputs.clear();
        totalPreview = null;

        ScrollView sv = new ScrollView(this);
        LinearLayout root = screenRoot(sv);
        root.addView(heading("Edit invoice", 25));
        root.addView(caption("Page " + (pageIndex + 1) + " · Invoice " + safe(p.invoiceNo)), spaced(8));

        addSection(root, "Invoice details", "Number and date shown in the top-right block");
        field(root, "page_name", "Page name", p.pageName, false);
        field(root, "invoice_no", "Invoice No.", p.invoiceNo, false);
        field(root, "invoice_date", "Invoice Date", p.invoiceDate, false);
        field(root, "buyer_order_no", "Buyer’s Order No.", p.buyerOrderNo, false);
        field(root, "buyer_order_date", "Buyer’s Order Date", p.buyerOrderDate, false);

        addSection(root, "Customer", "All fields from the buyer block in the scanned invoice");
        field(root, "buyer_name", "Name", p.buyerName, false);
        field(root, "buyer_address", "Address", p.buyerAddress, true);
        field(root, "buyer_state", "State", p.buyerState, false);
        field(root, "buyer_pan", "PAN / IT No.", p.buyerPan, false);
        field(root, "buyer_gstin", "GSTIN", p.buyerGstin, false);
        field(root, "place_supply", "Place of Supply", p.placeOfSupply, false);

        addSection(root, "Service & tax", "Tax amounts are calculated automatically from taxable amount and rates");
        field(root, "description", "Description of Services", p.description, true);
        field(root, "hsn_sac", "HSN / SAC", p.hsnSac, false);
        field(root, "taxable_amount", "Taxable Amount", InvoiceConfig.money(p.taxableAmount), false, InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        field(root, "cgst_rate", "CGST %", number(p.cgstRate), false, InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        field(root, "sgst_rate", "SGST %", number(p.sgstRate), false, InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        field(root, "igst_rate", "IGST %", number(p.igstRate), false, InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

        totalPreview = heading("Total  ₹ " + InvoiceConfig.money(p.total()), 18);
        totalPreview.setPadding(dp(10), dp(12), dp(10), dp(12));
        totalPreview.setBackground(bg(Color.rgb(243, 248, 246), 10));
        root.addView(totalPreview, spaced(12));
        addTaxWatchers();

        addSection(root, "Bank & signature", "These values can vary per invoice page, as they do in the scanned PDF");
        field(root, "bank_name", "Bank Name", p.bankName, false);
        field(root, "account_no", "Account No.", p.accountNo, false, InputType.TYPE_CLASS_NUMBER);
        field(root, "branch_ifsc", "Branch & IFS Code", p.branchIfsc, true);
        field(root, "signatory_name", "Authorized Signatory", p.signatoryName, false);

        Button save = action("Save page", true);
        save.setOnClickListener(v -> {
            persistPage();
            repo.save(config);
            Toast.makeText(this,"Page saved",Toast.LENGTH_SHORT).show();
            showHome();
        });
        root.addView(save, spaced(8));

        Button gen = action("Save & Generate PDF", true);
        gen.setOnClickListener(v -> {
            persistPage();
            repo.save(config);
            generate(pageIndex, false);
        });
        root.addView(gen, spaced(8));

        Button back = action("Back", false);
        back.setOnClickListener(v -> showHome());
        root.addView(back);

        setContentView(sv);
    }

    private void addSection(LinearLayout root, String title, String subtitle) {
        LinearLayout c = card();
        c.setPadding(dp(14), dp(10), dp(14), dp(8));
        c.addView(heading(title, 16));
        c.addView(caption(subtitle));
        root.addView(c, spaced(6));
    }

    private void field(LinearLayout root, String key, String label, String value, boolean multi) {
        field(root,key,label,value,multi,InputType.TYPE_CLASS_TEXT);
    }

    private void field(LinearLayout root, String key, String label, String value, boolean multi, int type) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(4),dp(2),dp(4),dp(8));
        TextView l = new TextView(this);
        l.setText(label);
        l.setTextSize(13);
        l.setTextColor(Color.rgb(70,70,75));
        box.addView(l);

        EditText e = new EditText(this);
        e.setText(value == null ? "" : value);
        e.setTextSize(16);
        e.setTextColor(Color.rgb(25,25,28));
        e.setHint(label);
        e.setPadding(dp(12),dp(8),dp(12),dp(8));
        e.setBackground(bg(Color.rgb(250,250,251), 9));
        e.setInputType(type | (multi ? InputType.TYPE_TEXT_FLAG_MULTI_LINE : InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS));
        e.setSingleLine(!multi);
        inputs.put(key,e);
        box.addView(e);
        root.addView(box);
    }

    private void addTaxWatchers() {
        TextWatcher w = new TextWatcher() {
            public void beforeTextChanged(CharSequence s,int st,int c,int a){}
            public void onTextChanged(CharSequence s,int st,int b,int c){ updateTotal(); }
            public void afterTextChanged(Editable e){}
        };
        if(inputs.get("taxable_amount")!=null) inputs.get("taxable_amount").addTextChangedListener(w);
        if(inputs.get("cgst_rate")!=null) inputs.get("cgst_rate").addTextChangedListener(w);
        if(inputs.get("sgst_rate")!=null) inputs.get("sgst_rate").addTextChangedListener(w);
        if(inputs.get("igst_rate")!=null) inputs.get("igst_rate").addTextChangedListener(w);
    }

    private void updateTotal() {
        if(totalPreview==null) return;
        double a=numInput("taxable_amount"), c=numInput("cgst_rate"), s=numInput("sgst_rate"), i=numInput("igst_rate");
        double total=a*(1+(c+s+i)/100d);
        totalPreview.setText("Total  ₹ " + InvoiceConfig.money(total));
    }

    private double numInput(String key) {
        try { return Double.parseDouble(inputs.get(key).getText().toString().replace(",","").trim()); }
        catch(Exception e) { return 0; }
    }

    private void persistPage() {
        InvoiceConfig.Page p = config.pages.get(editingPage);
        p.pageName=value("page_name"); p.invoiceNo=value("invoice_no"); p.invoiceDate=value("invoice_date");
        p.buyerOrderNo=value("buyer_order_no"); p.buyerOrderDate=value("buyer_order_date");
        p.buyerName=value("buyer_name"); p.buyerAddress=value("buyer_address"); p.buyerState=value("buyer_state");
        p.buyerPan=value("buyer_pan"); p.buyerGstin=value("buyer_gstin"); p.placeOfSupply=value("place_supply");
        p.description=value("description"); p.hsnSac=value("hsn_sac");
        p.taxableAmount=numInput("taxable_amount"); p.cgstRate=numInput("cgst_rate"); p.sgstRate=numInput("sgst_rate"); p.igstRate=numInput("igst_rate");
        p.bankName=value("bank_name"); p.accountNo=value("account_no"); p.branchIfsc=value("branch_ifsc"); p.signatoryName=value("signatory_name");
    }

    private String value(String k){
        EditText e=inputs.get(k);
        return e==null ? "" : e.getText().toString().trim();
    }

    private void showSettings() {
        ScrollView sv = new ScrollView(this);
        LinearLayout root = screenRoot(sv);
        root.addView(heading("Business & app settings",25));
        root.addView(caption("Seller details are printed in the invoice header. The declaration is printed in the footer."), spaced(10));

        LinearLayout business=card();
        business.addView(heading("Seller details",17));
        EditText n = settingInput(business,"Business Name",config.sellerName);
        EditText a = settingInput(business,"Address",config.sellerAddress);
        EditText phone = settingInput(business,"Mobile",config.sellerPhone);
        EditText gst = settingInput(business,"GSTIN",config.sellerGstin);
        EditText dec = settingInput(business,"Declaration",config.declaration);
        root.addView(business, spaced(12));

        Button save=action("Save business settings",true);
        save.setOnClickListener(v->{
            config.sellerName=n.getText().toString().trim();
            config.sellerAddress=a.getText().toString().trim();
            config.sellerPhone=phone.getText().toString().trim();
            config.sellerGstin=gst.getText().toString().trim();
            config.declaration=dec.getText().toString().trim();
            repo.save(config);
            Toast.makeText(this,"Business settings saved",Toast.LENGTH_SHORT).show();
            showHome();
        });
        root.addView(save, spaced(10));

        Button restore=action("Restore the 7 bundled sample pages",false);
        restore.setOnClickListener(v->confirmRestore());
        root.addView(restore, spaced(10));

        Button back=action("Back",false);
        back.setOnClickListener(v->showHome());
        root.addView(back);

        setContentView(sv);
    }

    private EditText settingInput(LinearLayout root,String label,String val){
        TextView l=new TextView(this);
        l.setText(label); l.setTextSize(13); l.setTextColor(Color.rgb(75,75,80)); root.addView(l);
        EditText e=new EditText(this);
        e.setText(val); e.setTextSize(16); e.setPadding(dp(10),dp(7),dp(10),dp(7));
        e.setBackground(bg(Color.rgb(250,250,251),9));
        boolean multi=label.equals("Address")||label.equals("Declaration");
        e.setMinLines(multi?2:1); e.setSingleLine(!multi);
        root.addView(e,spaced(8));
        return e;
    }

    private void confirmRestore(){
        new android.app.AlertDialog.Builder(this)
            .setTitle("Restore bundled samples?")
            .setMessage("This replaces the page configurations stored on this phone with the seven invoice pages from the bundled config file.")
            .setNegativeButton("Cancel",null)
            .setPositiveButton("Restore",(d,w)->{config=repo.restoreBundled();showHome();})
            .show();
    }

    private void generate(int index, boolean share) {
        InvoiceConfig.Page p = config.pages.get(index);
        try {
            PdfDocument doc = InvoicePdf.create(config,p);
            String fileName = "GST_Invoice_" + safe(p.invoiceNo) + "_" +
                new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date()) + ".pdf";
            Uri uri = savePdf(doc,fileName);
            doc.close();
            Toast.makeText(this,"Saved to Downloads/GST Invoice",Toast.LENGTH_LONG).show();
            if (share) sharePdf(uri); else viewPdf(uri);
        } catch(Exception e) {
            Toast.makeText(this,"Could not create PDF: "+e.getMessage(),Toast.LENGTH_LONG).show();
        }
    }

    private Uri savePdf(PdfDocument doc,String fileName) throws Exception {
        if(Build.VERSION.SDK_INT>=29){
            ContentValues cv=new ContentValues();
            cv.put(MediaStore.Downloads.DISPLAY_NAME,fileName);
            cv.put(MediaStore.Downloads.MIME_TYPE,"application/pdf");
            cv.put(MediaStore.Downloads.RELATIVE_PATH,Environment.DIRECTORY_DOWNLOADS+"/GST Invoice");
            Uri uri=getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI,cv);
            if(uri==null)throw new IOException("Could not create Downloads file");
            try(OutputStream out=getContentResolver().openOutputStream(uri)){
                if(out==null)throw new IOException("Could not open output stream");
                doc.writeTo(out);
            }
            return uri;
        }

        File dir=new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),"GST Invoice");
        if(!dir.exists()&&!dir.mkdirs())throw new IOException("Cannot create Downloads directory");
        File f=new File(dir,fileName);
        try(FileOutputStream out=new FileOutputStream(f)){doc.writeTo(out);}
        return Uri.fromFile(f);
    }

    private void viewPdf(Uri uri){
        Intent i=new Intent(Intent.ACTION_VIEW);
        i.setDataAndType(uri,"application/pdf");
        i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try{startActivity(i);}
        catch(Exception ignored){Toast.makeText(this,"PDF saved. Install a PDF viewer to open it here.",Toast.LENGTH_LONG).show();}
    }

    private void sharePdf(Uri uri){
        Intent i=new Intent(Intent.ACTION_SEND);
        i.setType("application/pdf");
        i.putExtra(Intent.EXTRA_STREAM,uri);
        i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try{startActivity(Intent.createChooser(i,"Share invoice"));}catch(Exception ignored){viewPdf(uri);}
    }

    private static String safe(String s){ return s==null||s.trim().isEmpty()?"-":s.trim(); }
    private static String number(double v){ return String.format(Locale.US,"%.0f",v); }
}