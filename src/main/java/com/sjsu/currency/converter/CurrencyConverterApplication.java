package com.sjsu.currency.converter;

import com.sjsu.currency.converter.orchestrator.ConversionOrchestrator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication

public class CurrencyConverterApplication {

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(CurrencyConverterApplication.class, args);
        String fileType = extractFileType(args[0]);
        ConversionOrchestrator orchestrator = context.getBean(fileType, ConversionOrchestrator.class);
        orchestrator.orchestrateConversion(args[0], args[1]);
    }

    private static String extractFileType(String file) {
        String[] str = file.split("\\.");
        return str[str.length - 1];
    }

}
