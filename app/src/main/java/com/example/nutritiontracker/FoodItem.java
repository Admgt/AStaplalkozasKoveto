package com.example.nutritiontracker;

public class FoodItem {
    private String name;
    private int calories;
    private double protein, carbs, fats;

    public FoodItem() {}

    public FoodItem(String name, int calories, double protein, double carbs, double fats) {
        this.name = name;
        this.calories = calories;
        this.protein = protein;
        this.carbs = carbs;
        this.fats = fats;
    }

    public String getName() { return name; }
    public int getCalories() { return calories; }
    public double getProtein() { return protein; }
    public double getCarbs() { return carbs; }
    public double getFats() { return fats; }
}
