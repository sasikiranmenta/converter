package com.sjsu.currency.converter.orchestrator;

public interface ConversionOrchestrator {
    String getFileHandlerType();
    public void orchestrateConversion(String inputFile, String outputFile);
}
