package com.sjsu.currency.converter.strategy.standardizer;

import com.sjsu.currency.converter.models.StandardizedTransaction;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.stereotype.Component;

@Component
public class JsonStandardizedTransactionConverter implements StandardizedTransactionConverter<JSONObject> {
    @Override

    public StandardizedTransaction convertToStandardizedTransaction(JSONObject rawTransaction) {
        StandardizedTransaction standardizedTransaction = new StandardizedTransaction();
        try {
            standardizedTransaction.setFromCurrency(rawTransaction.getString("OriginalCurrency"));
            standardizedTransaction.setToCurrency(rawTransaction.getString("TargetCurrency"));
            standardizedTransaction.setAmount(Double.valueOf(rawTransaction.getString("Amount")));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return standardizedTransaction;
    }
}
