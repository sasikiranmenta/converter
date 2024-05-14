package com.sjsu.currency.converter.strategy.standardizer;

import com.sjsu.currency.converter.models.StandardizedTransaction;

public interface StandardizedTransactionConverter<InputType> {
    StandardizedTransaction convertToStandardizedTransaction(InputType rawTransaction);
}