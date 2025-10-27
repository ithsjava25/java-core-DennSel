package com.example;

import java.math.BigDecimal;
import java.util.UUID;

public class ElectronicsProduct extends Product implements Shippable {
    private final int warrantyMonths;
    private final BigDecimal weight; // kg

    public ElectronicsProduct(UUID uuid, String nameInput, Category categoryInput, BigDecimal priceInput, int warrantyMonths, BigDecimal weightInput) {
        super(uuid, nameInput, categoryInput, ensurePositive(priceInput, "Price"));

        this.warrantyMonths = ensurePositiveInt(warrantyMonths);
        this.weight = ensurePositive(weightInput, "Weight");
    }

    // Make sure price, weight is positive
    private static BigDecimal ensurePositive(BigDecimal value, String valueType) {
        // Uses built in constant for 0
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(valueType + " cannot be negative.");
        }
        return value;
    }

    // Make sure warranty is positive
    private static int ensurePositiveInt(int value) {
        if (value < 0) {
            throw new IllegalArgumentException("Warranty months cannot be negative.");
        }
        return value;
    }

    @Override
    public String productDetails() {
        return "Electronics: " + name() + ", Warranty: " + warrantyMonths + " months";
    }

    @Override
    public BigDecimal calculateShippingCost() {
        BigDecimal baseShipping = BigDecimal.valueOf(79.0);
        BigDecimal heavyShipping = baseShipping.add(BigDecimal.valueOf(49.0));
        BigDecimal maxWeight = BigDecimal.valueOf(5.0);
        // See if weight is larger than 0
        if (weight.compareTo(maxWeight) > 0) {
            return heavyShipping;
        }
        return baseShipping;
    }

    @Override
    public Double weight() {
        return Double.parseDouble(String.valueOf(weight));
    }
}
