package com.example;

import org.junit.jupiter.api.*;

import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * A collection of tests for the business domain classes.
 */
class BasicTest {

    /**
     * Test suite for the {@link Category} value object.
     */
    @Nested
    @DisplayName("A Category")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class CategoryTests {

        @Test
        @Order(1)
        @DisplayName("✅ should not have any public constructors")
        void should_notHavePublicConstructors() {
            Constructor<?>[] constructors = Category.class.getConstructors();
            assertThat(constructors)
                    .as("Category should only be instantiated via its factory method, not public constructors.")
                    .isEmpty();
        }

        @Test
        @Order(2)
        @DisplayName("✅ should be created using the 'of' factory method")
        void should_beCreated_when_usingFactoryMethod() {
            Category category = Category.of("Electronics");
            assertThat(category.getName())
                    .as("The category name should match the value provided to the factory method.")
                    .isEqualTo("Electronics");
        }

        @Test
        @Order(3)
        @DisplayName("✅ should return the same instance for the same name (flyweight pattern)")
        void should_returnSameInstance_when_nameIsIdentical() {
            Category category1 = Category.of("Dairy");
            Category category2 = Category.of("Dairy");
            assertThat(category1)
                    .as("Categories with the same name should be the exact same instance to save memory.")
                    .isSameAs(category2);
        }

        @Test
        @Order(4)
        @DisplayName("✅ should capitalize the first letter of its name automatically")
        void should_capitalizeName_when_createdWithLowercase() {
            Category category = Category.of("fruit");
            assertThat(category.getName())
                    .as("The category's name should be formatted with an initial capital letter.")
                    .isEqualTo("Fruit");
        }

        @Test
        @Order(5)
        @DisplayName("❌ should throw IllegalArgumentException if the name is null")
        void should_throwException_when_nameIsNull() {
            assertThatThrownBy(() -> Category.of(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Category name can't be null");
        }

        @Test
        @Order(6)
        @DisplayName("❌ should throw IllegalArgumentException if the name is empty")
        void should_throwException_when_nameIsEmpty() {
            assertThatThrownBy(() -> Category.of(""))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Category name can't be blank");
        }

        @Test
        @Order(7)
        @DisplayName("❌ should throw IllegalArgumentException if the name is blank (whitespace)")
        void should_throwException_when_nameIsBlank() {
            assertThatThrownBy(() -> Category.of("   "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Category name can't be blank");
        }
    }

    /**
     * Test suite for the {@link Warehouse} singleton, covering its lifecycle,
     * product management, and interactions with the product class hierarchy.
     */
    @Nested
    @DisplayName("A Warehouse")
    class WarehouseTests {

        private Warehouse warehouse;

        @BeforeEach
        void setUp() {
            warehouse = Warehouse.getInstance("TestWarehouse");
            warehouse.clearProducts(); // Ensures test isolation
        }

        @Test
        @DisplayName("✅ should be empty immediately after setup")
        void should_beEmpty_when_newlySetUp() {
            assertThat(warehouse.isEmpty())
                    .as("Warehouse should be empty after setUp clears it.")
                    .isTrue();
        }

        // --- Singleton and Factory Pattern Tests ---

        @Nested
        @DisplayName("Factory and Singleton Behavior")
        class FactoryTests {

            @Test
            @DisplayName("✅ should not have any public constructors")
            void should_notHavePublicConstructors() {
                Constructor<?>[] constructors = Warehouse.class.getConstructors();
                assertThat(constructors)
                        .as("Warehouse should only be accessed via its getInstance() factory method.")
                        .isEmpty();
            }

            @Test
            @DisplayName("✅ should be created by calling the 'getInstance' factory method")
            void should_beCreated_when_usingFactoryMethod() {
                Warehouse defaultWarehouse = Warehouse.getInstance();
                assertThat(defaultWarehouse).isNotNull();
            }

            @Test
            @DisplayName("✅ should return the same instance for the same name")
            void should_returnSameInstance_when_nameIsIdentical() {
                Warehouse warehouse1 = Warehouse.getInstance("GlobalStore");
                Warehouse warehouse2 = Warehouse.getInstance("GlobalStore");
                assertThat(warehouse1)
                        .as("Warehouses with the same name should be the same singleton instance.")
                        .isSameAs(warehouse2);
            }
        }

        @Nested
        @DisplayName("Product Management")
        class ProductManagementTests {

            @Test
            @DisplayName("✅ should be empty when new")
            void should_beEmpty_when_new() {
                assertThat(warehouse.isEmpty())
                        .as("A new warehouse instance should have no products.")
                        .isTrue();
            }

            @Test
            @DisplayName("✅ should return an empty product list when new")
            void should_returnEmptyProductList_when_new() {
                assertThat(warehouse.getProducts())
                        .as("A new warehouse should return an empty list, not null.")
                        .isEmpty();
            }

            @Test
            @DisplayName("✅ should store various product types (Food, Electronics)")
            void should_storeHeterogeneousProducts() {
                // Arrange
                Product milk = new FoodProduct(UUID.randomUUID(), "Milk", Category.of("Dairy"), new BigDecimal("15.50"), LocalDate.now().plusDays(7), new BigDecimal("1.0"));
                Product laptop = new ElectronicsProduct(UUID.randomUUID(), "Laptop", Category.of("Electronics"), new BigDecimal("12999"), 24, new BigDecimal("2.2"));

                // Act
                warehouse.addProduct(milk);
                warehouse.addProduct(laptop);

                // Assert
                assertThat(warehouse.getProducts())
                        .as("Warehouse should correctly store different subtypes of Product.")
                        .hasSize(2)
                        .containsExactlyInAnyOrder(milk, laptop);
            }



            @Test
            @DisplayName("❌ should throw an exception when adding a product with a duplicate ID")
            void should_throwException_when_addingProductWithDuplicateId() {
                // Arrange
                UUID sharedId = UUID.randomUUID();
                Product milk = new FoodProduct(sharedId, "Milk", Category.of("Dairy"), BigDecimal.ONE, LocalDate.now(), BigDecimal.ONE);
                Product cheese = new FoodProduct(sharedId, "Cheese", Category.of("Dairy"), BigDecimal.TEN, LocalDate.now(), BigDecimal.TEN);
                warehouse.addProduct(milk);

                // Act & Assert
                assertThatThrownBy(() -> warehouse.addProduct(cheese))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("Product with that id already exists, use updateProduct for updates.");
            }

            @Test
            @DisplayName("✅ should update the price of an existing product")
            void should_updateExistingProductPrice() {
                // Arrange
                Product milk = new FoodProduct(UUID.randomUUID(), "Milk", Category.of("Dairy"), new BigDecimal("15.50"), LocalDate.now().plusDays(7), new BigDecimal("1.0"));
                warehouse.addProduct(milk);
                BigDecimal newPrice = new BigDecimal("17.00");

                // Act
                warehouse.updateProductPrice(milk.uuid(), newPrice);

                // Assert
                assertThat(warehouse.getProductById(milk.uuid()))
                        .as("The product's price should be updated to the new value.")
                        .isPresent()
                        .hasValueSatisfying(product ->
                                assertThat(product.price()).isEqualByComparingTo(newPrice)
                        );
            }

            @Test
            @DisplayName("✅ should group products correctly by their category")
            void should_groupProductsByCategories() {
                // Arrange
                Product milk = new FoodProduct(UUID.randomUUID(), "Milk", Category.of("Dairy"), BigDecimal.ONE, LocalDate.now(), BigDecimal.ONE);
                Product apple = new FoodProduct(UUID.randomUUID(), "Apple", Category.of("Fruit"), BigDecimal.ONE, LocalDate.now(), BigDecimal.ONE);
                Product laptop = new ElectronicsProduct(UUID.randomUUID(), "Laptop", Category.of("Electronics"), BigDecimal.TEN, 24, BigDecimal.TEN);
                warehouse.addProduct(milk);
                warehouse.addProduct(apple);
                warehouse.addProduct(laptop);

                Map<Category, List<Product>> expectedMap = Map.of(
                        Category.of("Dairy"), List.of(milk),
                        Category.of("Fruit"), List.of(apple),
                        Category.of("Electronics"), List.of(laptop)
                );

                // Act & Assert
                assertThat(warehouse.getProductsGroupedByCategories())
                        .as("The returned map should have categories as keys and lists of products as values.")
                        .isEqualTo(expectedMap);
            }

            @Test
            @DisplayName("🔒 should return an unmodifiable list of products to protect internal state")
            void should_returnUnmodifiableProductList() {
                // Arrange
                Product milk = new FoodProduct(UUID.randomUUID(), "Milk", Category.of("Dairy"), BigDecimal.ONE, LocalDate.now(), BigDecimal.ONE);
                warehouse.addProduct(milk);
                List<Product> products = warehouse.getProducts();

                // Act & Assert
                assertThatThrownBy(products::clear)
                        .as("The list returned by getProducts() should be immutable to prevent external modification.")
                        .isInstanceOf(UnsupportedOperationException.class);
            }

            @Test
            @DisplayName("✅ should correctly remove an existing product")
            void should_removeExistingProduct() {
                // Arrange
                Product milk = new FoodProduct(UUID.randomUUID(), "Milk", Category.of("Dairy"), BigDecimal.TEN, LocalDate.now(), BigDecimal.ONE);
                warehouse.addProduct(milk);
                assertThat(warehouse.getProducts()).hasSize(1);

                // Act
                warehouse.remove(milk.uuid());

                // Assert
                assertThat(warehouse.isEmpty())
                        .as("Warehouse should be empty after the only product is removed.")
                        .isTrue();
                assertThat(warehouse.getProductById(milk.uuid()))
                        .as("The removed product should no longer be found.")
                        .isEmpty();
            }

            @Test
            @DisplayName("❌ should throw an exception when adding a null product")
            void should_throwException_when_addingNullProduct() {
                assertThatThrownBy(() -> warehouse.addProduct(null))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("Product cannot be null.");
            }

            @Test
            @DisplayName("✅ should return an empty map when grouping by category if empty")
            void should_returnEmptyMap_when_groupingCategoriesOnEmptyWarehouse() {
                assertThat(warehouse.getProductsGroupedByCategories())
                        .as("Grouping products by category in an empty warehouse should yield an empty map, not null.")
                        .isNotNull()
                        .isEmpty();
            }
        }

        @Nested
        @DisplayName("Interacting with Non-Existent Products")
        class NonExistentProductTests {

            @Test
            @DisplayName("❓ should return an empty Optional when getting a product by a non-existent ID")
            void should_returnEmptyOptional_when_gettingByNonExistentId() {
                // Arrange: Warehouse is empty
                UUID nonExistentId = UUID.randomUUID();

                // Act & Assert
                assertThat(warehouse.getProductById(nonExistentId))
                        .as("Searching for a non-existent product should result in an empty Optional.")
                        .isEmpty();
            }

            @Test
            @DisplayName("❌ should throw an exception when updating a non-existent product")
            void should_throwException_when_updatingNonExistentProduct() {
                // Arrange
                UUID nonExistentId = UUID.randomUUID();
                BigDecimal newPrice = new BigDecimal("99.99");

                // Act & Assert
                assertThatThrownBy(() -> warehouse.updateProductPrice(nonExistentId, newPrice))
                        .as("Attempting to update a product that does not exist should fail clearly.")
                        .isInstanceOf(NoSuchElementException.class)
                        .hasMessageContaining("Product not found with id:");
            }
        }

        @Nested
        @DisplayName("Polymorphism and Interfaces")
        class InterfaceAndPolymorphismTests {
            @Test
            @DisplayName("✅ should allow polymorphic behavior on productDetails() method")
            void should_demonstratePolymorphism_when_callingProductDetails() {
                // Arrange
                Product milk = new FoodProduct(UUID.randomUUID(), "Milk", Category.of("Dairy"), new BigDecimal("15.50"), LocalDate.of(2025, 12, 24), new BigDecimal("1.0"));
                Product laptop = new ElectronicsProduct(UUID.randomUUID(), "Laptop", Category.of("Electronics"), new BigDecimal("12999"), 24, new BigDecimal("2.2"));

                // Act & Assert
                assertThat(milk.productDetails()).isEqualTo("Food: Milk, Expires: 2025-12-24");
                assertThat(laptop.productDetails()).isEqualTo("Electronics: Laptop, Warranty: 24 months");
            }

            @Test
            @DisplayName("✅ should find all expired products using the Perishable interface")
            void should_findExpiredProducts_when_checkingPerishables() {
                // Arrange
                Product freshMilk = new FoodProduct(UUID.randomUUID(), "Fresh Milk", Category.of("Dairy"), new BigDecimal("15"), LocalDate.now().plusDays(5), new BigDecimal("1.0"));
                Product oldMilk = new FoodProduct(UUID.randomUUID(), "Old Milk", Category.of("Dairy"), new BigDecimal("10"), LocalDate.now().minusDays(2), new BigDecimal("1.0"));
                Product laptop = new ElectronicsProduct(UUID.randomUUID(), "Laptop", Category.of("Electronics"), new BigDecimal("9999"), 24, new BigDecimal("2.5")); // Not perishable
                warehouse.addProduct(freshMilk);
                warehouse.addProduct(oldMilk);
                warehouse.addProduct(laptop);

                // Act
                List<Perishable> expiredItems = warehouse.expiredProducts();

                // Assert
                assertThat(expiredItems)
                        .as("Only products that have passed their expiration date should be returned.")
                        .hasSize(1)
                        .containsExactly((Perishable) oldMilk);
            }

            @Test
            @DisplayName("✅ should calculate total shipping cost using the Shippable interface")
            void should_calculateTotalShippingCost_when_summingShippableItems() {
                // Arrange: Shipping cost logic is described in comments
                Product milk = new FoodProduct(UUID.randomUUID(), "Milk", Category.of("Dairy"), new BigDecimal("15"), LocalDate.now().plusDays(5), new BigDecimal("1.2")); // Shipping: 1.2 * 50 = 60
                Product heavyLaptop = new ElectronicsProduct(UUID.randomUUID(), "Heavy Laptop", Category.of("Electronics"), new BigDecimal("15000"), 24, new BigDecimal("6.0")); // Shipping: 79 + 49 = 128
                warehouse.addProduct(milk);
                warehouse.addProduct(heavyLaptop);

                // Act
                BigDecimal totalShippingCost = warehouse.shippableProducts().stream()
                        .map(Shippable::calculateShippingCost)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                // Assert
                assertThat(totalShippingCost)
                        .as("Total shipping cost should be the sum of costs for all shippable items.")
                        .isEqualByComparingTo("188.0"); // Expected: 60 + 128
            }
        }
    }

    /**
     * Test suite for validating the invariants of a {@link FoodProduct}.
     */
    @Nested
    @DisplayName("A Food Product")
    class FoodProductTests {

        @Test
        @DisplayName("❌ should throw an exception if created with a negative price")
        void should_throwException_when_createdWithNegativePrice() {
            assertThatThrownBy(() -> new FoodProduct(
                    UUID.randomUUID(),
                    "Expired Milk",
                    Category.of("Dairy"),
                    new BigDecimal("-10.00"), // Invalid price
                    LocalDate.now(),
                    BigDecimal.ONE))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Price cannot be negative.");
        }

        @Test
        @DisplayName("❌ should throw an exception if created with a negative weight")
        void should_throwException_when_createdWithNegativeWeight() {
            assertThatThrownBy(() -> new FoodProduct(
                    UUID.randomUUID(),
                    "Anti-Gravity Milk",
                    Category.of("Dairy"),
                    BigDecimal.TEN,
                    LocalDate.now(),
                    new BigDecimal("-1.0"))) // Invalid weight
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Weight cannot be negative.");
        }
    }

    /**
     * Test suite for validating the invariants of an {@link ElectronicsProduct}.
     */
    @Nested
    @DisplayName("An Electronics Product")
    class ElectronicsProductTests {

        @Test
        @DisplayName("❌ should throw an exception if created with a negative warranty")
        void should_throwException_when_createdWithNegativeWarranty() {
            assertThatThrownBy(() -> new ElectronicsProduct(
                    UUID.randomUUID(),
                    "Time Machine",
                    Category.of("Gadgets"),
                    BigDecimal.valueOf(9999),
                    -12, // Invalid warranty
                    BigDecimal.TEN))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Warranty months cannot be negative.");
        }
    }
}