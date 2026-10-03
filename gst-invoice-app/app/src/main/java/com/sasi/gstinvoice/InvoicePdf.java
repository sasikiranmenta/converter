package com.sasi.gstinvoice;

import android.graphics.*;
import android.graphics.pdf.PdfDocument;
import java.util.*;

public final class InvoicePdf {
    public static PdfDocument create(
        String invoiceNo,String date,String buyerName,String buyerAddress,String buyerState,String buyerPan,String buyerGst,String place,
        String description,String hsn,double amt,double cgstPct,double sgstPct,double igstPct,double cgst,double sgst,double igst,double total,
        String seller,String sellerAddr,String mobile,String sellerGst,String bank,String declaration) {

        PdfDocument doc=new PdfDocument();
        PdfDocument.PageInfo info=new PdfDocument.PageInfo.Builder(595,842,1).create();
        PdfDocument.Page page=doc.startPage(info);
        Canvas c=page.getCanvas();

        Paint p=new Paint(Paint.ANTI_ALIAS_FLAG); p.setColor(Color.BLACK);
        p.setTypeface(Typeface.create("sans",Typeface.NORMAL)); p.setTextSize(9);
        Paint bold=new Paint(p); bold.setTypeface(Typeface.create("sans",Typeface.BOLD));
        Paint line=new Paint(p); line.setStyle(Paint.Style.STROKE); line.setStrokeWidth(0.8f);

        final float L=88, R=516, T=58, W=R-L;
        bold.setTextSize(13); center(c,"TAX INVOICE",(L+R)/2,40,bold);

        // Top seller / invoice meta block
        c.drawRect(L,T,R,145,line);
        c.drawLine(295,T,295,145,line); c.drawLine(444,T,444,145,line);
        c.drawLine(L,105,R,105,line);
        bold.setTextSize(15); center(c,seller,(L+295)/2,96,bold);
        p.setTextSize(8.5f); center(c,sellerAddr.replace("\n","  "),(L+295)/2,110,p);
        bold.setTextSize(9); center(c,"GSTIN : "+sellerGst,(L+295)/2,134,bold);
        bold.setTextSize(8.5f); c.drawText("Mob. "+mobile,206,70,bold);
        p.setTextSize(9); c.drawText("Invoice No. : "+invoiceNo,302,84,p); c.drawText("Date : "+date,449,84,p);
        c.drawText("Buyer’s Order No.",302,119,p); c.drawText("Date :",449,119,p);

        // Buyer block
        c.drawRect(L,145,R,254,line);
        drawMulti(c,p, buyer("Name : ",buyerName), L+6,163,16);
        drawMulti(c,p, buyer("Address : ",buyerAddress), L+6,181,16);
        drawMulti(c,p, buyer("State  : ",buyerState), L+6,199,16);
        drawMulti(c,p, buyer("PAN / IT No. : ",buyerPan), L+6,217,16);
        drawMulti(c,p, buyer("GSTIN : ",buyerGst), L+6,235,16);
        drawMulti(c,p, buyer("Place of Supply : ",place), L+6,251,16);

        // Service table
        float y0=254, yHeader=274, yBottom=540;
        c.drawRect(L,y0,R,yBottom,line);
        float x1=120,x2=374,x3=458;
        c.drawLine(x1,y0,x1,yBottom,line); c.drawLine(x2,y0,x2,yBottom,line); c.drawLine(x3,y0,x3,yBottom,line);
        c.drawLine(L,yHeader,R,yHeader,line);
        center(c,"S.No.",(L+x1)/2,268,p); center(c,"DESCRIPTION OF SERVICES",(x1+x2)/2,268,p); center(c,"HSN/SAC",(x2+x3)/2,268,p); center(c,"AMOUNT",(x3+R)/2,268,p);
        bold.setTextSize(9); c.drawText("1.",95,303,bold);
        p.setTextSize(9); wrap(c,description,x1+8,300,x2-8,13,p);
        c.drawText(hsn,x2+10,303,p); right(c,fmt(amt),R-8,303,p);
        p.setTextSize(9); c.drawText("E, & O.E.",x1+8,yBottom-9,p);

        float taxTop=455, row=22;
        c.drawLine(x2,taxTop,R,taxTop,line);
        String[][] tax={{"CGST  "+fmtPct(cgstPct)+" %",fmt(cgst)},{"SGST  "+fmtPct(sgstPct)+" %",fmt(sgst)},{"IGST   "+fmtPct(igstPct)+" %",fmt(igst)},{"TOTAL",fmt(total)}};
        for(int i=0;i<4;i++){ float yy=taxTop+i*row; c.drawLine(x2,yy+row,R,yy+row,line); c.drawLine(458,yy,458,yy+row,line); bold.setTextSize(9); right(c,tax[i][0],451,yy+15,bold); right(c,tax[i][1],R-7,yy+15,p); }

        float amtTop=yBottom, decTop=575, bottom=760;
        c.drawRect(L,amtTop,R,decTop,line);
        bold.setTextSize(8.5f); wrap(c,"Amount Chargeable (Inwords) : "+numberToWords(Math.round(total))+" RUPEES ONLY",L+6,amtTop+19,R-6,13,bold);

        c.drawRect(L,decTop,R,bottom,line); c.drawLine(350,decTop,350,bottom,line);
        bold.setTextSize(9); c.drawText("Declaration :",L+6,decTop+16,bold);
        p.setTextSize(8.5f); wrap(c,declaration,L+8,decTop+35,342,13,p);

        wrap(c,bank,360,decTop+36,R-8,14,p);

        c.drawRect(L,bottom,R,824,line); c.drawLine(350,bottom,350,824,line);
        bold.setTextSize(8.5f); center(c,"For "+seller,433,705,bold);
        p.setTypeface(Typeface.create("sans",Typeface.ITALIC)); center(c,"Customer’s Seal and Signature",219,813,p);
        p.setTypeface(Typeface.DEFAULT);

        doc.finishPage(page); return doc;
    }

    static String buyer(String a,String b){return a+(b==null?"":b);}
    static void center(Canvas c,String s,float x,float y,Paint p){c.drawText(s,x-p.measureText(s)/2,y,p);}
    static void right(Canvas c,String s,float x,float y,Paint p){c.drawText(s,x-p.measureText(s),y,p);}
    static String fmt(double n){return String.format(Locale.US,"%.2f",n);}
    static String fmtPct(double n){return String.format(Locale.US,"%.0f",n);}
    static void wrap(Canvas c,String s,float x,float y,float maxX,float step,Paint p){
        if(s==null)s="";
        String[] paras=s.replace("\r","").split("\n");
        for(String para:paras){
            String line="";
            for(String w:para.split(" ")){
                String test=line.isEmpty()?w:line+" "+w;
                if(x+p.measureText(test)>maxX && !line.isEmpty()){c.drawText(line,x,y,p);y+=step;line=w;} else line=test;
            }
            if(!line.isEmpty()){c.drawText(line,x,y,p);y+=step;}
        }
    }
    static void drawMulti(Canvas c,Paint p,String s,float x,float y,float step){wrap(c,s,x,y,510,step,p);}

    static String numberToWords(long n){
        if(n==0)return "ZERO";
        String[] ones={"","ONE","TWO","THREE","FOUR","FIVE","SIX","SEVEN","EIGHT","NINE","TEN","ELEVEN","TWELVE","THIRTEEN","FOURTEEN","FIFTEEN","SIXTEEN","SEVENTEEN","EIGHTEEN","NINETEEN"};
        String[] tens={"","","TWENTY","THIRTY","FORTY","FIFTY","SIXTY","SEVENTY","EIGHTY","NINETY"};
        StringBuilder sb=new StringBuilder();
        if(n>=10000000){sb.append(numberToWords(n/10000000)).append(" CRORE ");n%=10000000;}
        if(n>=100000){sb.append(numberToWords(n/100000)).append(" LAKH ");n%=100000;}
        if(n>=1000){sb.append(numberToWords(n/1000)).append(" THOUSAND ");n%=1000;}
        if(n>=100){sb.append(numberToWords(n/100)).append(" HUNDRED ");n%=100;}
        if(n>=20){sb.append(tens[(int)n/10]).append(" ");n%=10;}
        if(n>0)sb.append(ones[(int)n]).append(" ");
        return sb.toString().trim();
    }
}
