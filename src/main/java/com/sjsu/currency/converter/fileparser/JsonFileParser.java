package com.sjsu.currency.converter.fileparser;

import com.sjsu.currency.converter.models.StandardizedTransaction;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.jettison.json.JSONArray;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;

@Component
@Slf4j
public class JsonFileParser implements FileParser<JSONArray> {
    @Override
    public JSONArray readFile(String filepath) {
        //todo
        return null;
    }

    @Override
    public void writeToFile(List<StandardizedTransaction> transactionList, String outputFile) {
        //todo
    }
}
