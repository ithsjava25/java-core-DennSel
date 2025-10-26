package com.example;

import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * Advanced test suite for extra credit - requires implementation of WarehouseAnalyzer methods
 * These tests verify complex business logic, performance considerations, and edge cases
 */
@DisplayName("Advanced Warehouse Features (Extra Credit)")
class EdgeCaseTest {

    private Warehouse warehouse;
    private WarehouseAnalyzer analyzer;

    /**
     * Test setup: obtain a named singleton Warehouse instance, clear all products for test isolation,
     * and initialize a WarehouseAnalyzer used throughout the advanced feature tests.
     */
    @BeforeEach
    void setUp() {
        warehouse = Warehouse.getInstance("AdvancedTestWarehouse");
        warehouse.clearProducts();
        analyzer = new WarehouseAnalyzer(warehouse);
    }

    @Nested
    @DisplayName("Advanced Search and Filtering")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class AdvancedSearchTests {

        @Test
        @Order(1)
        @DisplayName("🔍 should handle complex price range queries with boundary conditions")
        /**
         * Verifies price range filtering is inclusive of boundaries.
         * Arrange: add products priced below min, at min, in range, at max, and above max.
         * Act: call analyzer.findProductsInPriceRange(10.00, 100.00).
         * Assert: only names MinPrice, InRange, and MaxPrice are returned (3 items).
         */
        void should_filterByPriceRange_withBoundaryConditions() {
            // Arrange - Products at exact boundaries
            Product exactMin = new FoodProduct(UUID.randomUUID(), "MinPrice", Category.of("Test"),
                    new BigDecimal("10.00"), LocalDate.now().plusDays(1), BigDecimal.ONE);
            Product belowMin = new FoodProduct(UUID.randomUUID(), "BelowMin", Category.of("Test"),
                    new BigDecimal("9.99"), LocalDate.now().plusDays(1), BigDecimal.ONE);
            Product exactMax = new FoodProduct(UUID.randomUUID(), "MaxPrice", Category.of("Test"),
                    new BigDecimal("100.00"), LocalDate.now().plusDays(1), BigDecimal.ONE);
            Product aboveMax = new FoodProduct(UUID.randomUUID(), "AboveMax", Category.of("Test"),
                    new BigDecimal("100.01"), LocalDate.now().plusDays(1), BigDecimal.ONE);
            Product inRange = new FoodProduct(UUID.randomUUID(), "InRange", Category.of("Test"),
                    new BigDecimal("50.00"), LocalDate.now().plusDays(1), BigDecimal.ONE);

            warehouse.addProduct(exactMin);
            warehouse.addProduct(belowMin);
            warehouse.addProduct(exactMax);
            warehouse.addProduct(aboveMax);
            warehouse.addProduct(inRange);

            // Act
            List<Product> filtered = analyzer.findProductsInPriceRange(
                    new BigDecimal("10.00"),
                    new BigDecimal("100.00")
            );

            // Assert
            assertThat(filtered)
                    .as("Should include products at exact boundaries but exclude those outside")
                    .hasSize(3)
                    .extracting(Product::name)
                    .containsExactlyInAnyOrder("MinPrice", "MaxPrice", "InRange");
        }

        @Test
        @Order(2)
        @DisplayName("🔍 should find products expiring within N days using date arithmetic")
        /**
         * Finds perishable products that expire within the given number of days starting today,
         * excluding items already expired and non-perishables. Uses LocalDate arithmetic.
         * Expect: Today, Tomorrow, In3Days (3 results), based on analyzer.findProductsExpiringWithinDays(3).
         */
        void should_findProductsExpiringWithinDays() {
            // Arrange - Various expiration scenarios
            LocalDate today = LocalDate.now();
            Product expiringToday = new FoodProduct(UUID.randomUUID(), "Today", Category.of("Dairy"),
                    BigDecimal.TEN, today, BigDecimal.ONE);
            Product expiringTomorrow = new FoodProduct(UUID.randomUUID(), "Tomorrow", Category.of("Dairy"),
                    BigDecimal.TEN, today.plusDays(1), BigDecimal.ONE);
            Product expiringIn3Days = new FoodProduct(UUID.randomUUID(), "In3Days", Category.of("Dairy"),
                    BigDecimal.TEN, today.plusDays(3), BigDecimal.ONE);
            Product expiringIn8Days = new FoodProduct(UUID.randomUUID(), "In8Days", Category.of("Dairy"),
                    BigDecimal.TEN, today.plusDays(8), BigDecimal.ONE);
            Product alreadyExpired = new FoodProduct(UUID.randomUUID(), "Expired", Category.of("Dairy"),
                    BigDecimal.TEN, today.minusDays(1), BigDecimal.ONE);
            Product nonPerishable = new ElectronicsProduct(UUID.randomUUID(), "Laptop", Category.of("Electronics"),
                    BigDecimal.TEN, 12, BigDecimal.ONE);

            warehouse.addProduct(expiringToday);
            warehouse.addProduct(expiringTomorrow);
            warehouse.addProduct(expiringIn3Days);
            warehouse.addProduct(expiringIn8Days);
            warehouse.addProduct(alreadyExpired);
            warehouse.addProduct(nonPerishable);

            // Act
            List<Perishable> expiringWithin3Days = analyzer.findProductsExpiringWithinDays(3);

            // Assert
            assertThat(expiringWithin3Days)
                    .as("Should find products expiring within the specified window (not already expired)")
                    .hasSize(3)
                    .extracting(p -> ((Product) p).name())
                    .containsExactlyInAnyOrder("Today", "Tomorrow", "In3Days");
        }

        @Test
        @Order(3)
        @DisplayName("🔍 should perform case-insensitive partial name search with special characters")
        /**
         * Performs a case-insensitive substring search over product names, handling spaces and symbols.
         * Arrange: mix of names containing "milk" in various cases plus a non-matching electronics item.
         * Act: analyzer.searchProductsByName("milk").
         * Assert: returns 4 matching products regardless of case or extra characters.
         */
        void should_searchByPartialName_caseInsensitive() {
            // Arrange
            warehouse.addProduct(new FoodProduct(UUID.randomUUID(), "Organic Milk 2%", Category.of("Dairy"),
                    BigDecimal.TEN, LocalDate.now().plusDays(5), BigDecimal.ONE));
            warehouse.addProduct(new FoodProduct(UUID.randomUUID(), "MILK Chocolate", Category.of("Sweets"),
                    BigDecimal.TEN, LocalDate.now().plusDays(5), BigDecimal.ONE));
            warehouse.addProduct(new FoodProduct(UUID.randomUUID(), "Almond Milk", Category.of("Dairy"),
                    BigDecimal.TEN, LocalDate.now().plusDays(5), BigDecimal.ONE));
            warehouse.addProduct(new FoodProduct(UUID.randomUUID(), "Milkshake Mix", Category.of("Dairy"),
                    BigDecimal.TEN, LocalDate.now().plusDays(5), BigDecimal.ONE));
            warehouse.addProduct(new ElectronicsProduct(UUID.randomUUID(), "Gaming Mouse", Category.of("Electronics"),
                    BigDecimal.TEN, 12, BigDecimal.ONE));

            // Act
            List<Product> milkProducts = analyzer.searchProductsByName("milk");

            // Assert
            assertThat(milkProducts)
                    .as("Should find all products containing 'milk' regardless of case")
                    .hasSize(4)
                    .extracting(Product::name)
                    .containsExactlyInAnyOrder("Organic Milk 2%", "MILK Chocolate", "Almond Milk", "Milkshake Mix");
        }
    }

    @Nested
    @DisplayName("Advanced Analytics and Calculations")
    class AdvancedAnalyticsTests {

        @Test
        @DisplayName("📊 should calculate weighted average price by category")
        /**
         * Calculates a weighted average price per category where weight is the product weight.
         * Arrange: three dairy items with different prices and weights.
         * Act: analyzer.calculateWeightedAveragePriceByCategory().
         * Assert: Dairy category has weighted average 11.43.
         */
        void should_calculateWeightedAveragePrice_byCategory() {
            // Arrange - Products with different weights in same category
            Category dairy = Category.of("Dairy");
            warehouse.addProduct(new FoodProduct(UUID.randomUUID(), "Milk", dairy,
                    new BigDecimal("10.00"), LocalDate.now().plusDays(5), new BigDecimal("2.0"))); // Weight: 2kg
            warehouse.addProduct(new FoodProduct(UUID.randomUUID(), "Cheese", dairy,
                    new BigDecimal("30.00"), LocalDate.now().plusDays(10), new BigDecimal("0.5"))); // Weight: 0.5kg
            warehouse.addProduct(new FoodProduct(UUID.randomUUID(), "Yogurt", dairy,
                    new BigDecimal("5.00"), LocalDate.now().plusDays(3), new BigDecimal("1.0"))); // Weight: 1kg

            // Act - Calculate weighted average: (10*2 + 30*0.5 + 5*1) / (2 + 0.5 + 1) = 40/3.5 = 11.43
            Map<Category, BigDecimal> weightedAverages = analyzer.calculateWeightedAveragePriceByCategory();

            // Assert
            assertThat(weightedAverages.get(dairy))
                    .as("Weighted average should consider product weights: (10*2 + 30*0.5 + 5*1) / 3.5 = 11.43")
                    .isEqualByComparingTo(new BigDecimal("11.43"));
        }

        @Test
        @DisplayName("📊 should identify products with abnormal pricing (outliers)")
        /**
         * Detects price outliers using mean and standard deviation.
         * Arrange: mostly normal-priced items around 15, plus very cheap and very expensive outliers.
         * Act: analyzer.findPriceOutliers(2.0).
         * Assert: returns exactly the two outliers ("Expensive" and "Cheap").
         */
        void should_identifyPriceOutliers_usingStatistics() {
            // Arrange - Most products around 10-20, with outliers
            IntStream.rangeClosed(1, 10).forEach(i ->
                    warehouse.addProduct(new FoodProduct(UUID.randomUUID(), "Normal" + i, Category.of("Test"),
                            new BigDecimal("15.00").add(new BigDecimal(i % 3)), LocalDate.now().plusDays(5), BigDecimal.ONE))
            );
            Product outlierHigh = new FoodProduct(UUID.randomUUID(), "Expensive", Category.of("Test"),
                    new BigDecimal("500.00"), LocalDate.now().plusDays(5), BigDecimal.ONE);
            Product outlierLow = new FoodProduct(UUID.randomUUID(), "Cheap", Category.of("Test"),
                    new BigDecimal("0.01"), LocalDate.now().plusDays(5), BigDecimal.ONE);

            warehouse.addProduct(outlierHigh);
            warehouse.addProduct(outlierLow);

            // Act - Find outliers (products with price > 2 standard deviations from mean)
            List<Product> outliers = analyzer.findPriceOutliers(2.0); // 2 standard deviations

            // Assert
            assertThat(outliers)
                    .as("Should identify statistical outliers beyond 2 standard deviations")
                    .hasSize(2)
                    .extracting(Product::name)
                    .containsExactlyInAnyOrder("Expensive", "Cheap");
        }

        @Test
        @DisplayName("💰 should optimize shipping by grouping products efficiently")
        /**
         * Groups shippable products into bins not exceeding a maximum total weight to optimize shipping.
         * Arrange: mix of light and heavy items across categories.
         * Act: analyzer.optimizeShippingGroups(10.0).
         * Assert: each group total weight <= 10.0 and all 5 items are included across groups.
         */
        void should_optimizeShipping_byGroupingProducts() {
            // Arrange - Products that could be grouped for shipping optimization
            warehouse.addProduct(new FoodProduct(UUID.randomUUID(), "Light1", Category.of("Food"),
                    BigDecimal.TEN, LocalDate.now().plusDays(5), new BigDecimal("0.5")));
            warehouse.addProduct(new FoodProduct(UUID.randomUUID(), "Light2", Category.of("Food"),
                    BigDecimal.TEN, LocalDate.now().plusDays(5), new BigDecimal("0.3")));
            warehouse.addProduct(new FoodProduct(UUID.randomUUID(), "Heavy1", Category.of("Food"),
                    BigDecimal.TEN, LocalDate.now().plusDays(5), new BigDecimal("8.0")));
            warehouse.addProduct(new ElectronicsProduct(UUID.randomUUID(), "Laptop1", Category.of("Electronics"),
                    BigDecimal.TEN, 12, new BigDecimal("2.5")));
            warehouse.addProduct(new ElectronicsProduct(UUID.randomUUID(), "Laptop2", Category.of("Electronics"),
                    BigDecimal.TEN, 12, new BigDecimal("2.8")));

            // Act - Group products to minimize total shipping cost
            List<ShippingGroup> optimizedGroups = analyzer.optimizeShippingGroups(new BigDecimal("10.0"));

            // Assert
            assertThat(optimizedGroups)
                    .as("Should create optimal shipping groups")
                    .hasSizeGreaterThanOrEqualTo(2)
                    .allSatisfy(group -> {
                        assertThat(group.getTotalWeight())
                                .as("Each group should not exceed max weight of 10.0")
                                .isLessThanOrEqualTo(10.0);
                    });

            // Verify all products are included
            long totalProducts = optimizedGroups.stream()
                    .mapToLong(g -> g.getProducts().size())
                    .sum();
            assertThat(totalProducts)
                    .as("All shippable products should be included in groups")
                    .isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("Complex Business Rules")
    class BusinessRulesTests {

        @Test
        @DisplayName("📋 should apply discount rules based on expiration proximity")
        /**
         * Applies tiered discounts based on how soon a perishable item expires.
         * Arrange: items expiring today, tomorrow, in 3 days, and in 7 days.
         * Act: analyzer.calculateExpirationBasedDiscounts().
         * Assert: prices become 50%, 70%, 85%, and 100% of original respectively.
         */
        void should_applyDiscounts_basedOnExpiration() {
            // Arrange
            LocalDate today = LocalDate.now();
            Product expiresToday = new FoodProduct(UUID.randomUUID(), "ExpiresToday", Category.of("Food"),
                    new BigDecimal("100.00"), today, BigDecimal.ONE);
            Product expiresTomorrow = new FoodProduct(UUID.randomUUID(), "ExpiresTomorrow", Category.of("Food"),
                    new BigDecimal("100.00"), today.plusDays(1), BigDecimal.ONE);
            Product expiresIn3Days = new FoodProduct(UUID.randomUUID(), "ExpiresIn3Days", Category.of("Food"),
                    new BigDecimal("100.00"), today.plusDays(3), BigDecimal.ONE);
            Product expiresIn7Days = new FoodProduct(UUID.randomUUID(), "ExpiresIn7Days", Category.of("Food"),
                    new BigDecimal("100.00"), today.plusDays(7), BigDecimal.ONE);

            warehouse.addProduct(expiresToday);
            warehouse.addProduct(expiresTomorrow);
            warehouse.addProduct(expiresIn3Days);
            warehouse.addProduct(expiresIn7Days);

            // Act
            Map<Product, BigDecimal> discountedPrices = analyzer.calculateExpirationBasedDiscounts();

            // Assert - Verify discount rules: 50% if expires today, 30% if tomorrow, 15% if within 3 days
            assertThat(discountedPrices.get(expiresToday))
                    .as("Product expiring today should have 50% discount")
                    .isEqualByComparingTo(new BigDecimal("50.00"));
            assertThat(discountedPrices.get(expiresTomorrow))
                    .as("Product expiring tomorrow should have 30% discount")
                    .isEqualByComparingTo(new BigDecimal("70.00"));
            assertThat(discountedPrices.get(expiresIn3Days))
                    .as("Product expiring in 3 days should have 15% discount")
                    .isEqualByComparingTo(new BigDecimal("85.00"));
            assertThat(discountedPrices.get(expiresIn7Days))
                    .as("Product expiring in 7 days should have no discount")
                    .isEqualByComparingTo(new BigDecimal("100.00"));
        }

        @Test
        @DisplayName("📦 should validate inventory constraints and business rules")
        /**
         * Validates high-value item percentage and category diversity business rules across inventory.
         * Arrange: 15 expensive electronics and 5 food items (total 20).
         * Act: analyzer.validateInventoryConstraints().
         * Assert: ~75% high-value, warning for >70%, diversity count 2, and minimum diversity satisfied.
         */
        void should_validateInventoryConstraints() {
            // Arrange - Setup products that might violate business rules
            Category electronics = Category.of("Electronics");
            Category food = Category.of("Food");

            // Add many electronics (expensive items)
            IntStream.range(0, 15).forEach(i ->
                    warehouse.addProduct(new ElectronicsProduct(UUID.randomUUID(), "Laptop" + i,
                            electronics, new BigDecimal("2000"), 12, BigDecimal.ONE))
            );

            // Add some food items
            IntStream.range(0, 5).forEach(i ->
                    warehouse.addProduct(new FoodProduct(UUID.randomUUID(), "Food" + i,
                            food, new BigDecimal("10"), LocalDate.now().plusDays(1), BigDecimal.ONE))
            );

            // Act
            InventoryValidation validation = analyzer.validateInventoryConstraints();

            // Assert
            assertThat(validation.getHighValuePercentage())
                    .as("High-value items percentage should be calculated correctly")
                    .isCloseTo(75.0, within(1.0)); // 15 out of 20 products

            assertThat(validation.isHighValueWarning())
                    .as("Should warn when high-value items exceed 70%")
                    .isTrue();

            assertThat(validation.getCategoryDiversity())
                    .as("Should track category diversity")
                    .isEqualTo(2);

            assertThat(validation.hasMinimumDiversity())
                    .as("Should have minimum category diversity (at least 2)")
                    .isTrue();
        }

        @Test
        @DisplayName("📊 should generate comprehensive inventory statistics")
        /**
         * Produces aggregate inventory metrics including counts, sums, averages, and extremes.
         * Arrange: 4 diverse products with one expired.
         * Act: analyzer.getInventoryStatistics().
         * Assert: totalProducts=4, totalValue=1590.50, averagePrice=397.63, expiredCount=1,
         * categoryCount=2, most expensive is "Laptop" and cheapest is "Milk".
         */
        void should_generateInventoryStatistics() {
            // Arrange - Diverse product mix
            warehouse.addProduct(new FoodProduct(UUID.randomUUID(), "Milk", Category.of("Dairy"),
                    new BigDecimal("15.50"), LocalDate.now().plusDays(2), new BigDecimal("1.0")));
            warehouse.addProduct(new FoodProduct(UUID.randomUUID(), "Cheese", Category.of("Dairy"),
                    new BigDecimal("25.00"), LocalDate.now().minusDays(1), new BigDecimal("0.5"))); // Expired
            warehouse.addProduct(new ElectronicsProduct(UUID.randomUUID(), "Laptop", Category.of("Electronics"),
                    new BigDecimal("1500.00"), 24, new BigDecimal("2.5")));
            warehouse.addProduct(new ElectronicsProduct(UUID.randomUUID(), "Mouse", Category.of("Electronics"),
                    new BigDecimal("50.00"), 12, new BigDecimal("0.1")));

            // Act
            InventoryStatistics stats = analyzer.getInventoryStatistics();

            // Assert
            assertThat(stats.getTotalProducts()).isEqualTo(4);
            assertThat(stats.getTotalValue())
                    .as("Should sum all product prices")
                    .isEqualByComparingTo(new BigDecimal("1590.50"));
            assertThat(stats.getAveragePrice())
                    .as("Should calculate average price")
                    .isEqualByComparingTo(new BigDecimal("397.63")); // 1590.50 / 4
            assertThat(stats.getExpiredCount()).isEqualTo(1);
            assertThat(stats.getCategoryCount()).isEqualTo(2);
            assertThat(stats.getMostExpensiveProduct().name()).isEqualTo("Laptop");
            assertThat(stats.getCheapestProduct().name()).isEqualTo("Milk");
        }
    }
}