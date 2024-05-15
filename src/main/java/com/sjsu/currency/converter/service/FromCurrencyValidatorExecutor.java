package com.sjsu.currency.converter.service;

import com.sjsu.currency.converter.exception.ConversionChainException;
import com.sjsu.currency.converter.models.StandardizedTransaction;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component("fromValidator")
public class FromCurrencyValidatorExecutor implements ExecutorChain {

    private final Set<String> conversionCurrencySet;
    private ExecutorChain next;

    public FromCurrencyValidatorExecutor(Set<String> conversionCurrencySet) {
        this.conversionCurrencySet = conversionCurrencySet;
    }

    @Override
    public StandardizedTransaction execute(StandardizedTransaction transaction) {
        if (!conversionCurrencySet.contains(transaction.getFromCurrency())) {
            throw new ConversionChainException("Invalid original currency code");
        }
        return next.execute(transaction);
    }

    public void setNext(ExecutorChain next) {
        this.next = next;
    }
}
