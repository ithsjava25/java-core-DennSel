package com.example;
import java.math.BigDecimal;
import java.util.*;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

public class Warehouse {
    // List to save what products are in the warehouse
    private final Set<Product> products;
    // Set to make sure no duplicates
    private final Set<UUID> changedProducts;
    private final String name;

    // Cache to save warehouses(singletons) by key: name
    private static final Map<String, Warehouse> instances = new HashMap<>();

    // Constructor
    private Warehouse() {
        // HashSet with faster add, remove, contains (I've read) and no order needed
        this.name = "Default";
        this.products = new HashSet<>();
        this.changedProducts = new HashSet<>();
    }

    // Constructor
    private Warehouse(String name) {
        this.name = name;
        // HashSet faster add, remove, contains (I've read) and no order needed
        this.products = new HashSet<>();
        this.changedProducts = new HashSet<>();
    }


    /******************************************
    ***************   METHODS   ***************
    *******************************************/

    public static Warehouse getInstance() {
        return getInstance("default");
    }

    public static Warehouse getInstance(String name) {
        if(!instances.containsKey(name)) {
            instances.put(name, new Warehouse(name));
        }
        return instances.get(name);
    }

    // Get all products
    public List<Product> getProducts() {
        return Collections.unmodifiableList(new ArrayList<>(products));
    }

    // Get a single product by id
    public Optional<Product> getProductById(UUID uuid) {
        return products.stream()
                // Find matching ID
                .filter(item -> item.uuid().equals(uuid))
                // Return Optional
                .findFirst();
    }

    public void updateProductPrice(UUID uuid, BigDecimal newPrice) {
        Product item = products.stream()
                .filter(p -> p.uuid().equals(uuid))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Product not found with id: " + uuid));

        item.setPrice(newPrice);
        changedProducts.add(uuid);
    }

    public Set<UUID> getChangedProducts() {
        return Collections.unmodifiableSet(changedProducts);
    }

    public void addProduct(Product item) {
        if (item == null) {
            throw new IllegalArgumentException("Product cannot mvn clean install -Ube null.");
        }
        else if (products.stream().anyMatch(p -> p.uuid().equals(item.uuid()))) {
            throw new IllegalArgumentException("Product with that id already exists, use updateProduct for updates.");
        }
        products.add(item);
    }

    public void clearProducts() {
        products.clear();
        changedProducts.clear();
        if (instances != null){
            instances.clear();
        }

    }

    public boolean isEmpty() {
        return products.isEmpty();
    }

    public List<Perishable> expiredProducts() {
        return products.stream()
                .filter(item -> item instanceof Perishable) // Find every item implementing Perishable
                .map(item -> (Perishable) item) // Turn into Perishable instead of Product
                .filter(Perishable::isExpired) // Find every item that has expired
                .collect(toList()); // Finally return the list
    }

    public List<Shippable> shippableProducts() {
        return products.stream()
                .filter(item -> item instanceof Shippable) // Find every item implementing Shippable
                .map(item -> (Shippable) item) // Turn Product into Shippable
                .collect(toList()); // Finally returns the list
    }

    public void remove(UUID uuid) {
        products.removeIf(item -> item.uuid().equals(uuid));
        changedProducts.remove(uuid);
    }

    public Map<Category, List<Product>> getProductsGroupedByCategories() {
        return products.stream()
                .collect(groupingBy(Product::category));
    }
}

