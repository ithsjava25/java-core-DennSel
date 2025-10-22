package com.example;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class FoodProduct extends Product implements Perishable, Shippable {
    private final LocalDate expirationDate;
    private final BigDecimal weight; // kg

    public FoodProduct(UUID uuid, String nameInput, Category categoryInput, BigDecimal priceInput, LocalDate expirationDateInput, BigDecimal weightInput) {
        super(uuid, nameInput, categoryInput, ensurePositive(priceInput, "Price"));

        this.expirationDate = expirationDateInput;
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

    @Override
    public String productDetails() {
        return "Food: " + name() + ", Expires: " + expirationDate;
    }

    @Override
    public BigDecimal calculateShippingCost() {
        // Multiply weight with 50
        return weight.multiply(BigDecimal.valueOf(50));
    }

    @Override
    public Double weight() {
        return Double.parseDouble(String.valueOf(weight));
    }

    @Override
    public LocalDate expirationDate() {
        return expirationDate;
    }
}
