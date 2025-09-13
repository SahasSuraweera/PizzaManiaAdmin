package com.example.pizzamaniaadmin;

import java.io.Serializable;

public class PizzaModel implements Serializable {
    private String id;
    private String name;
    private String size;
    private double price;
    private String description;
    private String imageUrl;

    // Empty constructor required for Firebase
    public PizzaModel() {}

    // Constructor for creating a pizza
    public PizzaModel(String id, String name, String size, double price, String description, String imageUrl) {
        this.id = id;
        this.name = name;
        this.size = size;
        this.price = price;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
