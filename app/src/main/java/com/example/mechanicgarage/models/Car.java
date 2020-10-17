package com.example.mechanicgarage.models;

import com.example.mechanicgarage.utils.StringUtils;

public class Car {
    private String ownerId;
    private String make;
    private String model;
    private int year;
    private String engine;
    private String carVIN;


    public Car() {
    }

    public Car(String ownerId, String make, String model, int year, String engine, String carVIN) {
        this.ownerId = ownerId;
        this.make = make;
        this.model = model;
        this.year = year;
        this.engine = engine;
        this.carVIN = carVIN;
    }

    public String getOwnerId() {
        return StringUtils.getValueOrEmpty(ownerId);
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getMake() {
        return StringUtils.getValueOrEmpty(make);
    }

    public void setMake(String make) {
        this.make = make;
    }

    public String getModel() {
        return StringUtils.getValueOrEmpty(model);
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getEngine() {
        return StringUtils.getValueOrEmpty(engine);
    }

    public void setEngine(String engine) {
        this.engine = engine;
    }

    public String getCarVIN() {
        return StringUtils.getValueOrEmpty(carVIN);
    }

    public void setCarVIN(String carVIN) {
        this.carVIN = carVIN;
    }

    @Override
    public String toString() {
        return "Car{" +
                "ownerId='" + ownerId + '\'' +
                ", make='" + make + '\'' +
                ", model='" + model + '\'' +
                ", year=" + year +
                ", engine='" + engine + '\'' +
                ", carVIN='" + carVIN + '\'' +
                '}';
    }
}
