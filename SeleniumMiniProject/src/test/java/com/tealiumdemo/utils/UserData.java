package com.tealiumdemo.utils;

import java.util.UUID;

public class UserData {
    
    // User properties
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    
    // Constructor with random data
    public UserData() {
        this.firstName = "Test";
        this.lastName = "User";
        this.email = generateRandomEmail();
        this.password = "Test@1234";
    }
    
    // Constructor with custom data
    public UserData(String firstName, String lastName, String email, String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
    }
    
    // Generate random email
    private String generateRandomEmail() {
        String randomString = UUID.randomUUID().toString().substring(0, 8);
        return "testuser_" + randomString + "@test.com";
    }
    
    // Getters
    public String getFirstName() {
        return firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public String getEmail() {
        return email;
    }
    
    public String getPassword() {
        return password;
    }
    
    // Setters (optional)
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    // Print user info
    public void printUserInfo() {
        System.out.println("=== USER DATA ===");
        System.out.println("First Name: " + firstName);
        System.out.println("Last Name: " + lastName);
        System.out.println("Email: " + email);
        System.out.println("Password: " + password);
        System.out.println("=================");
    }
    
    // Static method to create a test user
    public static UserData createTestUser() {
        return new UserData();
    }
    
    // Static method to create a user with custom email prefix
    public static UserData createTestUser(String emailPrefix) {
        String randomString = UUID.randomUUID().toString().substring(0, 8);
        String email = emailPrefix + "_" + randomString + "@test.com";
        return new UserData("Test", "User", email, "Test@1234");
    }
}