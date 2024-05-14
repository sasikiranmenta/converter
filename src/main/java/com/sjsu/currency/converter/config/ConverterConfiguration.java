package com.sjsu.currency.converter.config;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.sjsu.currency.converter.orchestrator.ConversionOrchestrator;
import com.sjsu.currency.converter.service.ExecutorChain;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration
public class ConverterConfiguration {

    @Bean
    public Map<String, ConversionOrchestrator> orchestratorFactory(List<ConversionOrchestrator> orchestratorList) {
        return orchestratorList.stream()
                .collect(Collectors.toMap(ConversionOrchestrator::getFileHandlerType, Function.identity()));
    }

    @Bean
    @Primary
    public ExecutorChain executorChainConfig(@Autowired Map<String, ExecutorChain> executorChainFactory, @Value("${converter.chain.config}")
    List<String> executorOrderList) {
        ExecutorChain prev = null;
        for (int i = executorOrderList.size() - 1; i >= 0; i--) {
            ExecutorChain chain = executorChainFactory.get(executorOrderList.get(i));
            chain.setNext(prev);
            prev = chain;
        }
        return executorChainFactory.get(executorOrderList.get(0));
    }

    @Bean
    public Set<String> countrySet(Table<String, String, Double> conversionTable) {
        Set<String> country = new HashSet<>();
        for (Table.Cell<String, String, ?> cell : conversionTable.cellSet()) {
            country.add(cell.getRowKey());
            country.add(cell.getColumnKey());
        }
        return country;
    }

    @Bean
    public Table<String, String, Double> conversionRates() {
        Table<String, String, Double> conversionRates = HashBasedTable.create();
        return conversionRates;
    }
}
