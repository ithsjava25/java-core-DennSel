package com.example;
import java.util.HashMap;
import java.util.Map;

public class Category {
    private String name;

    // Constructor
    private Category(String nameConstructor) {
        this.name = nameConstructor;
    }

    // Cache
    private static final Map<String, Category> categories = new HashMap<>();

    // Factory
    public static Category of(String nameOf){
        if (nameOf == null){
            IO.println("Category name can't be null");
            return null;
        }

        if (nameOf.isBlank()){
            IO.println("Category name can't be blank");
            return null;
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
        input = input.substring(0,1).toUpperCase() + input.substring(1);
        return input;
    }
}