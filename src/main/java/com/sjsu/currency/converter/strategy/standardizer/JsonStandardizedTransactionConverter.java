package com.sjsu.currency.converter.strategy.standardizer;

import com.sjsu.currency.converter.models.StandardizedTransaction;
import lombok.SneakyThrows;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.stereotype.Component;

@Component
public class JsonStandardizedTransactionConverter implements StandardizedTransactionConverter<JSONObject> {
    @Override
    @SneakyThrows
    public StandardizedTransaction convertToStandardizedTransaction(JSONObject rawTransaction) {
        StandardizedTransaction standardizedTransaction = new StandardizedTransaction();
        standardizedTransaction.setFromCurrency(rawTransaction.getString("OriginalCurrency"));
        standardizedTransaction.setToCurrency(rawTransaction.getString("TargetCurrency"));
        standardizedTransaction.setAmount(Double.valueOf(rawTransaction.getString("Amount")));
        return standardizedTransaction;
    }
}
