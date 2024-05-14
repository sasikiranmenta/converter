package com.sjsu.currency.converter.service;

import com.sjsu.currency.converter.models.StandardizedTransaction;
import org.springframework.stereotype.Component;

@Component("terminal")
public class TerminalExecutor implements ExecutorChain {
    @Override
    public void setNext(ExecutorChain next) {
        //do nothing as this is terminal operation
    }

    @Override
    public StandardizedTransaction execute(StandardizedTransaction transaction) {
        return transaction;
    }
}
