package com.example;
import java.math.BigDecimal;

interface Shippable {
    BigDecimal calculateShippingCost();
    Double weight();
}
