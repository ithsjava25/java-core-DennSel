package com.example;
import java.util.HashMap;
import java.util.Map;

public class Category {
    private String name;

    // Constructor
    private Category(String name) {
        this.name = name;
    }

    // Create 1 static cache map when initialized, then points to the HashMap reference.
    private static final Map<String, Category> categories = new HashMap<>();

    // Factory
    public static Category of(String nameOf){
        if (nameOf == null){
            throw new IllegalArgumentException("Category name can't be null");
        }

        if (nameOf.isBlank()){
            throw new IllegalArgumentException("Category name can't be blank");
        }

        // Normalize to capital letter
        String normalized = normalize(nameOf);

        // Check the cache, create new or return cached instance
        if (categories.containsKey(normalized)){
            return categories.get(normalized);
        }
        else {
            Category category = new Category(normalized);
            categories.put(normalized, category);
            return category;
        }
    }

    // Method to normalize to capital letter
    private static String normalize(String input) {
        input = input.trim().toLowerCase();
        if (input.isEmpty()) throw new IllegalArgumentException("Category name must not be blank");
        input = input.substring(0,1).toUpperCase() + input.substring(1);
        return input;
    }

    public String getName(){
        return this.name;
    }
}