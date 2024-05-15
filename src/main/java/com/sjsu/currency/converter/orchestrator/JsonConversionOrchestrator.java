package com.sjsu.currency.converter.orchestrator;

import com.sjsu.currency.converter.fileparser.FileParser;
import com.sjsu.currency.converter.models.StandardizedTransaction;
import com.sjsu.currency.converter.service.ExecutorChain;
import com.sjsu.currency.converter.strategy.standardizer.StandardizedTransactionConverter;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component("json")
public class JsonConversionOrchestrator implements ConversionOrchestrator {

    private final StandardizedTransactionConverter<JSONObject> standardizedTransactionConverter;
    private final FileParser<JSONObject> fileParser;
    private final ExecutorChain conversionChain;

    public JsonConversionOrchestrator(StandardizedTransactionConverter<JSONObject> standardizedTransactionConverter, FileParser<JSONObject> fileParser, ExecutorChain conversionChain) {
        this.standardizedTransactionConverter = standardizedTransactionConverter;
        this.fileParser = fileParser;
        this.conversionChain = conversionChain;
    }

    @Override
    public String getFileHandlerType() {
        return "json";
    }

    @Override

    public void orchestrateConversion(String inputFile, String outputFile) {
        try {
            JSONObject jsonObject = fileParser.readFile(inputFile);
            List<StandardizedTransaction> standardizedTransactions = convertToStandardizedList(jsonObject);
            standardizedTransactions.forEach(transaction -> {
                        try {
                            conversionChain.execute(transaction);
                        } catch (Exception e) {
                            transaction.setException(e);
                        }
                    }
            );
            fileParser.writeToFile(standardizedTransactions, outputFile);
        } catch (Exception e) {
            throw new RuntimeException(e);

        }
    }


    private List<StandardizedTransaction> convertToStandardizedList(JSONObject jsonObject) {
        try {
            List<StandardizedTransaction> list = new ArrayList<>();
            JSONArray jsonArray = jsonObject.getJSONArray("transactions");
            int bound = jsonArray.length();
            for (int i = 0; i < bound; i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                StandardizedTransaction standardizedTransaction = standardizedTransactionConverter.convertToStandardizedTransaction(obj);
                list.add(standardizedTransaction);
            }
            return list;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
