package com.sjsu.currency.converter.orchestrator;

import com.sjsu.currency.converter.fileparser.FileParser;
import com.sjsu.currency.converter.models.StandardizedTransaction;
import com.sjsu.currency.converter.service.ExecutorChain;
import com.sjsu.currency.converter.strategy.standardizer.StandardizedTransactionConverter;
import lombok.SneakyThrows;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component("json")
public class JsonConversionOrchestrator implements ConversionOrchestrator {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(JsonConversionOrchestrator.class);
    private final StandardizedTransactionConverter<JSONObject> standardizedTransactionConverter;
    private final FileParser<JSONArray> fileParser;
    private final ExecutorChain conversionChain;

    public JsonConversionOrchestrator(StandardizedTransactionConverter<JSONObject> standardizedTransactionConverter, FileParser<JSONArray> fileParser, ExecutorChain conversionChain) {
        this.standardizedTransactionConverter = standardizedTransactionConverter;
        this.fileParser = fileParser;
        this.conversionChain = conversionChain;
    }

    @Override
    public String getFileHandlerType() {
        return "json";
    }

    @Override
    @SneakyThrows
    public void orchestrateConversion(String inputFile, String outputFile) {
        JSONArray jsonArray = fileParser.readFile(inputFile);
        List<StandardizedTransaction> standardizedTransactions = convertToStandardizedList(jsonArray);
        standardizedTransactions.forEach(conversionChain::execute);
        fileParser.writeToFile(standardizedTransactions, outputFile);
    }

    @SneakyThrows
    private List<StandardizedTransaction> convertToStandardizedList(JSONArray jsonArray) {
        List<StandardizedTransaction> list = new ArrayList<>();
        int bound = jsonArray.length();
        for (int i = 0; i < bound; i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            StandardizedTransaction standardizedTransaction = standardizedTransactionConverter.convertToStandardizedTransaction(jsonObject);
            list.add(standardizedTransaction);
        }
        return list;
    }
}
