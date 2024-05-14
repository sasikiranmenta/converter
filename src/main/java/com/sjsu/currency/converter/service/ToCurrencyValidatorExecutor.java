package com.sjsu.currency.converter.service;

import com.sjsu.currency.converter.exception.ConversionChainException;
import com.sjsu.currency.converter.models.StandardizedTransaction;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component("toValidator")
@RequiredArgsConstructor
public class ToCurrencyValidatorExecutor implements ExecutorChain {

    @Setter
    private ExecutorChain next;
    private final Set<String> conversionCurrencySet;

    @Override
    public StandardizedTransaction execute(StandardizedTransaction transaction) {
        if(!conversionCurrencySet.contains(transaction.getToCurrency())) {
            throw new ConversionChainException("Invalid target currency code");
        }
        return next.execute(transaction);
    }
}
