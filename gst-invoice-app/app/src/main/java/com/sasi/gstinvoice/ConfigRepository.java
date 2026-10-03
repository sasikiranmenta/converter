package com.sasi.gstinvoice;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public final class ConfigRepository {
    private static final String PREFS="invoice_store";
    private static final String KEY_CONFIG="config_json";
    private final Context context;
    private final SharedPreferences prefs;

    public ConfigRepository(Context context){this.context=context.getApplicationContext();this.prefs=this.context.getSharedPreferences(PREFS,Context.MODE_PRIVATE);}
    public InvoiceConfig load(){
        String saved=prefs.getString(KEY_CONFIG,null);
        if(saved!=null&&!saved.trim().isEmpty()) try{return InvoiceConfig.fromJson(new JSONObject(saved));}catch(Exception ignored){}
        InvoiceConfig bundled=loadBundled(); save(bundled); return bundled;
    }
    public void save(InvoiceConfig config){prefs.edit().putString(KEY_CONFIG,config.toJson().toString()).apply();}
    public InvoiceConfig restoreBundled(){InvoiceConfig bundled=loadBundled();save(bundled);return bundled;}
    private InvoiceConfig loadBundled(){
        try(InputStream in=context.getAssets().open("invoice_config.json");BufferedReader br=new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))){
            StringBuilder sb=new StringBuilder();String line;while((line=br.readLine())!=null)sb.append(line).append('\n');return InvoiceConfig.fromJson(new JSONObject(sb.toString()));
        }catch(Exception e){return new InvoiceConfig();}
    }
}