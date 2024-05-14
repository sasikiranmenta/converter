package com.sjsu.currency.converter.service;

import com.google.common.collect.Table;
import com.sjsu.currency.converter.exception.ConversionChainException;
import com.sjsu.currency.converter.models.StandardizedTransaction;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("convert")
@RequiredArgsConstructor
public class ConvertCurrencyExecutor implements ExecutorChain {
    @Setter
    private ExecutorChain next;
    private final Table<String, String, Double> conversionRates;

    @Override
    public StandardizedTransaction execute(StandardizedTransaction transaction) {
        Double conversionRate;
        if(conversionRates.contains(transaction.getFromCurrency(), transaction.getToCurrency())) {
            conversionRate = conversionRates.get(transaction.getFromCurrency(), transaction.getToCurrency());
        } else if(conversionRates.contains(transaction.getToCurrency(), transaction.getFromCurrency())) {
            conversionRate = 1d / conversionRates.get(transaction.getToCurrency(), transaction.getFromCurrency());
        } else {
            throw new ConversionChainException("No Conversion Rate Provided");
        }
        transaction.setConvertedAmount(transaction.getAmount() * conversionRate);
        return next.execute(transaction);
    }
}
