package com.tealiumdemo.pages;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class RegisterPage {
    
    private final WebDriver driver;
    private final WebDriverWait wait;
    
    // Locators
    private final By pageTitle = By.xpath("//h1[contains(text(), 'Create an Account')]");
    private final By firstNameInput = By.id("firstname");
    private final By lastNameInput = By.id("lastname");
    private final By emailInput = By.id("email_address");
    private final By passwordInput = By.id("password");
    private final By confirmPasswordInput = By.id("confirmation");
    private final By registerButton = By.xpath("//button[@title='Register']");
    private final By successMessage = By.xpath("//li[@class='success-msg']//span");
    
    public RegisterPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }
    
    public String getPageTitle() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(pageTitle)).getText();
    }
    
    public void registerUser(String firstName, String lastName, String email, String password) {
        // Fill form
        wait.until(ExpectedConditions.visibilityOfElementLocated(firstNameInput)).sendKeys(firstName);
        driver.findElement(lastNameInput).sendKeys(lastName);
        driver.findElement(emailInput).sendKeys(email);
        driver.findElement(passwordInput).sendKeys(password);
        driver.findElement(confirmPasswordInput).sendKeys(password);
        
        // Click register
        driver.findElement(registerButton).click();
    }
    
    public String getSuccessMessage() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(successMessage)).getText();
    }
}