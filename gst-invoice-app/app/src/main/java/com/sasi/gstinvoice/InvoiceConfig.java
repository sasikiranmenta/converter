package com.sasi.gstinvoice;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Locale;

public final class InvoiceConfig {
    public String templateId = "gst_invoice_v1";
    public String templateName = "GST Tax Invoice";
    public String sellerName = "";
    public String sellerAddress = "";
    public String sellerGstin = "";
    public String sellerPhone = "";
    public String declaration = "We declare that this invoice shows the actual price of the Services described and that all particulars are true and correct.\nSubject to Nellore Jurisdiction.";
    public ArrayList<Page> pages = new ArrayList<>();

    public static final class Page {
        public int sourcePage = 0;
        public String pageName = "New Invoice";
        public String invoiceNo = "";
        public String invoiceDate = "";
        public String buyerOrderNo = "";
        public String buyerOrderDate = "";
        public String buyerName = "";
        public String buyerAddress = "";
        public String buyerState = "";
        public String buyerPan = "";
        public String buyerGstin = "";
        public String placeOfSupply = "";
        public String description = "";
        public String hsnSac = "997212";
        public double taxableAmount = 0;
        public double cgstRate = 9;
        public double sgstRate = 9;
        public double igstRate = 0;
        public String bankName = "";
        public String accountNo = "";
        public String branchIfsc = "";
        public String signatoryName = "";

        public double cgst() { return taxableAmount * cgstRate / 100d; }
        public double sgst() { return taxableAmount * sgstRate / 100d; }
        public double igst() { return taxableAmount * igstRate / 100d; }
        public double total() { return taxableAmount + cgst() + sgst() + igst(); }

        public Page copy() {
            Page p = new Page();
            p.sourcePage=sourcePage; p.pageName=pageName; p.invoiceNo=invoiceNo; p.invoiceDate=invoiceDate;
            p.buyerOrderNo=buyerOrderNo; p.buyerOrderDate=buyerOrderDate; p.buyerName=buyerName; p.buyerAddress=buyerAddress;
            p.buyerState=buyerState; p.buyerPan=buyerPan; p.buyerGstin=buyerGstin; p.placeOfSupply=placeOfSupply;
            p.description=description; p.hsnSac=hsnSac; p.taxableAmount=taxableAmount; p.cgstRate=cgstRate; p.sgstRate=sgstRate; p.igstRate=igstRate;
            p.bankName=bankName; p.accountNo=accountNo; p.branchIfsc=branchIfsc; p.signatoryName=signatoryName;
            return p;
        }
    }

    public JSONObject toJson() {
        JSONObject root = new JSONObject();
        try {
            root.put("version",2); root.put("templateId",templateId); root.put("templateName",templateName);
            JSONObject seller=new JSONObject(); seller.put("name",sellerName); seller.put("address",sellerAddress); seller.put("gstin",sellerGstin); seller.put("phone",sellerPhone); seller.put("declaration",declaration);
            root.put("seller",seller);
            JSONArray arr=new JSONArray(); for(Page p:pages) arr.put(pageToJson(p)); root.put("pages",arr);
        } catch(Exception ignored) {}
        return root;
    }

    public static InvoiceConfig fromJson(JSONObject root) {
        InvoiceConfig c=new InvoiceConfig(); c.templateId=root.optString("templateId","gst_invoice_v1"); c.templateName=root.optString("templateName","GST Tax Invoice");
        JSONObject s=root.optJSONObject("seller"); if(s!=null){c.sellerName=s.optString("name","");c.sellerAddress=s.optString("address","");c.sellerGstin=s.optString("gstin","");c.sellerPhone=s.optString("phone","");c.declaration=s.optString("declaration",c.declaration);}
        JSONArray a=root.optJSONArray("pages"); if(a!=null) for(int i=0;i<a.length();i++){JSONObject o=a.optJSONObject(i);if(o!=null)c.pages.add(pageFromJson(o));}
        return c;
    }

    private static JSONObject pageToJson(Page p)throws Exception{
        JSONObject o=new JSONObject(); o.put("sourcePage",p.sourcePage);o.put("pageName",p.pageName);o.put("invoiceNo",p.invoiceNo);o.put("invoiceDate",p.invoiceDate);
        o.put("buyerOrderNo",p.buyerOrderNo);o.put("buyerOrderDate",p.buyerOrderDate);o.put("buyerName",p.buyerName);o.put("buyerAddress",p.buyerAddress);o.put("buyerState",p.buyerState);
        o.put("buyerPan",p.buyerPan);o.put("buyerGstin",p.buyerGstin);o.put("placeOfSupply",p.placeOfSupply);o.put("description",p.description);o.put("hsnSac",p.hsnSac);
        o.put("taxableAmount",p.taxableAmount);o.put("cgstRate",p.cgstRate);o.put("sgstRate",p.sgstRate);o.put("igstRate",p.igstRate);o.put("bankName",p.bankName);o.put("accountNo",p.accountNo);o.put("branchIfsc",p.branchIfsc);o.put("signatoryName",p.signatoryName);
        return o;
    }

    private static Page pageFromJson(JSONObject o){
        Page p=new Page(); p.sourcePage=o.optInt("sourcePage",0);p.pageName=o.optString("pageName","New Invoice");p.invoiceNo=o.optString("invoiceNo","");
        p.invoiceDate=o.optString("invoiceDate","");p.buyerOrderNo=o.optString("buyerOrderNo","");p.buyerOrderDate=o.optString("buyerOrderDate","");p.buyerName=o.optString("buyerName","");
        p.buyerAddress=o.optString("buyerAddress","");p.buyerState=o.optString("buyerState","");p.buyerPan=o.optString("buyerPan","");p.buyerGstin=o.optString("buyerGstin","");
        p.placeOfSupply=o.optString("placeOfSupply","");p.description=o.optString("description","");p.hsnSac=o.optString("hsnSac","997212");p.taxableAmount=o.optDouble("taxableAmount",0);
        p.cgstRate=o.optDouble("cgstRate",9);p.sgstRate=o.optDouble("sgstRate",9);p.igstRate=o.optDouble("igstRate",0);p.bankName=o.optString("bankName","");p.accountNo=o.optString("accountNo","");
        p.branchIfsc=o.optString("branchIfsc","");p.signatoryName=o.optString("signatoryName",""); return p;
    }

    public static Page blankPage(String name,String nextInvoiceNo,InvoiceConfig base){
        Page p=new Page(); p.pageName=name;p.invoiceNo=nextInvoiceNo;p.invoiceDate="";p.hsnSac="997212";p.cgstRate=9;p.sgstRate=9;p.igstRate=0;p.signatoryName=base.sellerName;
        if(!base.pages.isEmpty()){Page last=base.pages.get(base.pages.size()-1);p.bankName=last.bankName;p.accountNo=last.accountNo;p.branchIfsc=last.branchIfsc;} return p;
    }
    public static String money(double n){return String.format(Locale.US,"%.2f",n);}
}