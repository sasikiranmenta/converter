package com.sjsu.currency.converter.models;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class StandardizedTransaction {
    private String fromCurrency;
    private String toCurrency;
    private double amount;
    private double convertedAmount;
    private Exception exception;

    public String getFromCurrency() {
        return this.fromCurrency;
    }

    public String getToCurrency() {
        return this.toCurrency;
    }

    public double getAmount() {
        return this.amount;
    }

    public double getConvertedAmount() {
        return this.convertedAmount;
    }

    public Exception getException() {
        return this.exception;
    }

    public void setFromCurrency(String fromCurrency) {
        this.fromCurrency = fromCurrency;
    }

    public void setToCurrency(String toCurrency) {
        this.toCurrency = toCurrency;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setConvertedAmount(double convertedAmount) {
        this.convertedAmount = convertedAmount;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }
}
