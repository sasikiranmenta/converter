package com.sjsu.currency.converter.service;

import com.sjsu.currency.converter.exception.ConversionChainException;
import com.sjsu.currency.converter.models.StandardizedTransaction;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component("fromValidator")
@RequiredArgsConstructor
public class FromCurrencyValidatorExecutor implements ExecutorChain {

    private final Set<String> conversionCurrencySet;
    @Setter
    private ExecutorChain next;

    @Override
    public StandardizedTransaction execute(StandardizedTransaction transaction) {
        if (!conversionCurrencySet.contains(transaction.getFromCurrency())) {
            throw new ConversionChainException("Invalid original currency code");
        }
        return next.execute(transaction);
    }
}
