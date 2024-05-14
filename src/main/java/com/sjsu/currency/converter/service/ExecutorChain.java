package com.sjsu.currency.converter.service;

import com.sjsu.currency.converter.models.StandardizedTransaction;

public interface ExecutorChain {

    void setNext(ExecutorChain next);
    StandardizedTransaction execute(StandardizedTransaction transaction);
}
