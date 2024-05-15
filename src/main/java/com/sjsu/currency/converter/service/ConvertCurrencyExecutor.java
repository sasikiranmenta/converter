package com.sjsu.currency.converter.service;

import com.google.common.collect.Table;
import com.sjsu.currency.converter.exception.ConversionChainException;
import com.sjsu.currency.converter.models.StandardizedTransaction;
import org.springframework.stereotype.Component;

@Component("convert")
public class ConvertCurrencyExecutor implements ExecutorChain {
    private ExecutorChain next;
    private final Table<String, String, Double> conversionRates;

    public ConvertCurrencyExecutor(Table<String, String, Double> conversionRates) {
        this.conversionRates = conversionRates;
    }

    @Override
    public StandardizedTransaction execute(StandardizedTransaction transaction) {
        Double conversionRate;
        if (conversionRates.contains(transaction.getFromCurrency(), transaction.getToCurrency())) {
            conversionRate = conversionRates.get(transaction.getFromCurrency(), transaction.getToCurrency());
        } else if (conversionRates.contains(transaction.getToCurrency(), transaction.getFromCurrency())) {
            conversionRate = 1d / conversionRates.get(transaction.getToCurrency(), transaction.getFromCurrency());
        } else {
            throw new ConversionChainException("No Conversion Rate Provided");
        }
        transaction.setConvertedAmount(transaction.getAmount() * conversionRate);
        return next.execute(transaction);
    }

    public void setNext(ExecutorChain next) {
        this.next = next;
    }
}
