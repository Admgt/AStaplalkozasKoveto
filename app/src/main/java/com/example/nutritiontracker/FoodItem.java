package com.example.nutritiontracker;

import java.io.Serializable;

public class FoodItem implements Serializable {
    private String id;
    private String name;
    private int calories;
    private double protein, carbs, fats;

    public FoodItem() {
        this.name = "";
        this.calories = 0;
        this.protein = 0.0;
        this.carbs = 0.0;
        this.fats = 0.0;
    }

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
    public void setName(String name) {
        this.name = name;
    }
    public void setCalories(int calories) {
        this.calories = calories;
    }
    public void setProtein(double protein) {
        this.protein = protein;
    }
    public void setCarbs(double carbs) {
        this.carbs = carbs;
    }
    public void setFats(double fats) {
        this.fats = fats;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
