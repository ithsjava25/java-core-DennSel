package com.example;
import java.math.BigDecimal;

interface Shippable {
    BigDecimal calculateShippingCost();

    // Return 0.0 if no weight override
    default Double weight(){
        return 0.0;
    }
}
