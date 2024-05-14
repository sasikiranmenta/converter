package com.sjsu.currency.converter.fileparser;

import com.sjsu.currency.converter.models.StandardizedTransaction;

import java.util.List;

public interface FileParser<Target> {
    Target readFile(String filepath);

    void writeToFile(List<StandardizedTransaction> transactionList, String outputFile);
}
