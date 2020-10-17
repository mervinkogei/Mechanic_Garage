package com.example.mechanicgarage.models;

public class User {
    private String email;

    private String name;
    private String phone;
    private String uid;
    private Car car;


    public User() {
    }

    public User(String email, String name, String phone, String uid, Car car) {
        this.email = email;
        this.name = name;
        this.phone = phone;
        this.uid = uid;
        this.car = car;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Car getCar() {
        return car;
    }

    public void setCar(Car car) {
        this.car = car;
    }
}
