package com.tealiumdemo.pages;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class HomePage {

    private final WebDriverWait wait;

    // Locators
    private final By accountDropdown = By.xpath("//a[contains(@class, 'skip-account')]");
    private final By registerLink = By.linkText("Register");
    private final By logoutLink = By.linkText("Log Out");

    public HomePage(org.openqa.selenium.WebDriver driver) {
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public void clickAccountDropdown() {
        wait.until(ExpectedConditions.elementToBeClickable(accountDropdown)).click();
    }

    public void clickRegister() {
        wait.until(ExpectedConditions.elementToBeClickable(registerLink)).click();
    }

    public void clickLogout() {
        wait.until(ExpectedConditions.elementToBeClickable(logoutLink)).click();
    }
}
