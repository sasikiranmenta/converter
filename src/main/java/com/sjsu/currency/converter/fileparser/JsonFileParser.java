package com.sjsu.currency.converter.fileparser;

import com.sjsu.currency.converter.models.StandardizedTransaction;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Component
public class JsonFileParser implements FileParser<JSONObject> {
    @Override

    public JSONObject readFile(String filepath) {
        try {
            String jsonTxt = new String(Files.readAllBytes(Paths.get(filepath)), StandardCharsets.UTF_8);
            return new JSONObject(jsonTxt);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void writeToFile(List<StandardizedTransaction> transactionList, String outputFile) {
        JSONArray array = new JSONArray();
        try {
            for (int i = 0; i < transactionList.size(); i++) {
                JSONObject ele = new JSONObject();
                StandardizedTransaction transaction = transactionList.get(i);
                ele.put("Amount", transaction.getAmount());
                ele.put("OriginalCurrency", transaction.getFromCurrency());
                ele.put("TargetCurrency", transaction.getToCurrency());

                if (transaction.getException() == null) {
                    ele.put("ConvertedAmount", formatConvertedAmount(transaction.getConvertedAmount()));
                    ele.put("Status", "Success");
                } else {
                    ele.put("ConvertedAmount", "");
                    ele.put("Status", transaction.getException().getMessage());
                }
                array.put(i, ele);
            }
            JSONObject object = new JSONObject();
            object.put("transactions", array);
            FileWriter writer = new FileWriter(outputFile);
            object.write(writer);
            writer.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String formatConvertedAmount(double value) {
        if (value == (long) value) {
            return String.format("%d", (long) value);
        } else {
            return String.format("%.2f", value);
        }
    }
}
