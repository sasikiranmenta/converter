package com.sjsu.currency.converter.service;

import com.sjsu.currency.converter.exception.ConversionChainException;
import com.sjsu.currency.converter.models.StandardizedTransaction;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component("toValidator")
public class ToCurrencyValidatorExecutor implements ExecutorChain {

    private ExecutorChain next;
    private final Set<String> conversionCurrencySet;

    public ToCurrencyValidatorExecutor(Set<String> conversionCurrencySet) {
        this.conversionCurrencySet = conversionCurrencySet;
    }

    @Override
    public StandardizedTransaction execute(StandardizedTransaction transaction) {
        if (!conversionCurrencySet.contains(transaction.getToCurrency())) {
            throw new ConversionChainException("Invalid target currency code");
        }
        return next.execute(transaction);
    }

    public void setNext(ExecutorChain next) {
        this.next = next;
    }
}
