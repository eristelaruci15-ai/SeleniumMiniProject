package com.tealiumdemo.tests;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class Test2_SignIn {
    
    private WebDriver driver;
    private WebDriverWait wait;
    
    // CRITICAL: Use the EXACT email and password from Test 1 output
    // Look at your Test 1 console output and copy the exact email and password
    private final String email = "Test@gail.com";  // REPLACE WITH YOUR TEST 1 EMAIL
    private final String password = "1234567";                // REPLACE WITH YOUR TEST 1 PASSWORD
    
    @BeforeMethod
    public void setup() {
        System.out.println("========== SETUP TEST 2 ==========");
        System.out.println("Using credentials:");
        System.out.println("Email: " + email);
        System.out.println("Password: " + password);
        
        // Setup ChromeDriver
        System.setProperty("webdriver.chrome.driver", "chromedriver.exe");
        
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        options.addArguments("--remote-allow-origins=*");
        
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        
        // Navigate to website
        driver.get("https://ecommerce.tealiumdemo.com/");
        System.out.println("Opened: " + driver.getTitle());
    }
    
    @Test
    public void signInTest() {
        System.out.println("\n========== TEST 2: SIGN IN ==========");
        
        try {
            // Step 2: Click on Account then Sign in
            System.out.println("\nSTEP 2: Click Account -> Log In");
            
            // Find and click Account link
            WebElement accountLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[contains(text(), 'Account')]")));
            accountLink.click();
            System.out.println("✓ Clicked Account");
            
            // Wait for dropdown and click Log In
            WebElement loginLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.linkText("Log In")));
            loginLink.click();
            System.out.println("✓ Clicked Log In");
            
            // Step 3: Login with credentials from Test 1
            System.out.println("\nSTEP 3: Login with Test 1 credentials");
            
            // Wait for login form
            wait.until(ExpectedConditions.presenceOfElementLocated(By.id("email")));
            
            // Enter email
            driver.findElement(By.id("email")).clear();
            driver.findElement(By.id("email")).sendKeys(email);
            System.out.println("  Entered email");
            
            // Enter password
            driver.findElement(By.id("pass")).clear();
            driver.findElement(By.id("pass")).sendKeys(password);
            System.out.println("  Entered password");
            
            // Click login button
            WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.id("send2")));
            loginButton.click();
            System.out.println("✓ Clicked Login button");
            
            // Wait for login to process
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                System.out.println("Sleep interrupted: " + ie.getMessage());
            }
            
            // Step 4: Check username is displayed
            System.out.println("\nSTEP 4: Check username display");
            
            // DEBUG: Show current page info
            System.out.println("Current URL: " + driver.getCurrentUrl());
            System.out.println("Page title: " + driver.getTitle());
            
            // Check for login success - multiple ways
            boolean loginSuccess = false;
            String successMessage;
            
            // Method 1: Check for welcome message
            try {
                WebElement welcomeElement = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//div[contains(@class, 'welcome')] | " +
                            "//p[contains(@class, 'welcome')] | " +
                            "//span[contains(text(), 'Welcome')]")));
                successMessage = welcomeElement.getText();
                System.out.println("✓ Welcome message found: " + successMessage);
                loginSuccess = true;
            } catch (org.openqa.selenium.TimeoutException | NoSuchElementException e) {
                System.out.println("No welcome message found");
            }
            
            // Method 2: Check if we're on account page
            if (!loginSuccess) {
                String currentUrl = driver.getCurrentUrl().toLowerCase();
                String pageTitle = driver.getTitle().toLowerCase();
                
                if (currentUrl.contains("account") || pageTitle.contains("account")) {
                    successMessage = "On account page";
                    System.out.println("✓ " + successMessage);
                    loginSuccess = true;
                }
            }
            
            // Method 3: Check page source for success indicators
            if (!loginSuccess) {
                String pageSource = driver.getPageSource().toLowerCase();
                if (pageSource.contains("logout") || pageSource.contains("log out")) {
                    successMessage = "Logout link found - user is logged in";
                    System.out.println("✓ " + successMessage);
                    loginSuccess = true;
                } else if (pageSource.contains("my dashboard") || pageSource.contains("dashboard")) {
                    successMessage = "Dashboard found - user is logged in";
                    System.out.println("✓ " + successMessage);
                    loginSuccess = true;
                }
            }
            
            // If login seems to have failed, check for error messages
            if (!loginSuccess) {
                System.out.println("\n⚠ Checking for login errors...");
                try {
                    WebElement errorElement = driver.findElement(
                        By.xpath("//li[@class='error-msg'] | " +
                                "//div[contains(@class, 'error')] | " +
                                "//span[contains(text(), 'error')]"));
                    String errorText = errorElement.getText();
                    System.out.println("❌ Login error: " + errorText);
                    
                    // Check for specific errors
                    if (errorText.toLowerCase().contains("invalid")) {
                        System.out.println("  → Credentials are invalid");
                    } else if (errorText.toLowerCase().contains("account") && 
                               errorText.toLowerCase().contains("exist")) {
                        System.out.println("  → Account doesn't exist. Did Test 1 create it successfully?");
                    }
                } catch (NoSuchElementException e) {
                    System.out.println("No clear error message found");
                }
                
                // Show what's on the page
                System.out.println("\nPage source snippet (first 500 chars):");
                System.out.println(driver.getPageSource().substring(0, Math.min(500, driver.getPageSource().length())));
            }
            
            // SOFT ASSERTION: Don't fail immediately, let us see what happened
            if (!loginSuccess) {
                System.out.println("\n❌ LOGIN APPEARS TO HAVE FAILED");
                System.out.println("Browser will remain open for debugging...");
                
                // Keep browser open for manual inspection
                try {
                    Thread.sleep(10000); // 10 seconds to look
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    System.out.println("Sleep interrupted: " + ie.getMessage());
                }
                
                // Now fail the test
                Assert.fail("Login failed. No success indicators found.");
            }
            
            System.out.println("✓ Login successful!");
            
            // Step 5: Click on Account and Log Out
            System.out.println("\nSTEP 5: Log out");
            
            // Find Account again
            WebElement accountLink2 = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[contains(text(), 'Account')]")));
            accountLink2.click();
            
            // Click Log Out
            WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.linkText("Log Out")));
            logoutLink.click();
            
            // Verify logout
            wait.until(ExpectedConditions.presenceOfElementLocated(By.linkText("Log In")));
            System.out.println("✓ Logged out");
            
            System.out.println("\n✅ TEST 2 PASSED!");
            
        } catch (RuntimeException e) {
            System.out.println("\n❌ TEST 2 FAILED WITH EXCEPTION:");
            System.out.println("Error: " + e.getMessage());
            System.out.println("Cause: " + e);
            
            // Keep browser open for debugging
            System.out.println("\nBrowser will remain open for 15 seconds...");
            try {
                Thread.sleep(15000);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                System.out.println("Sleep interrupted: " + ie.getMessage());
            }
            
            throw new RuntimeException("Test 2 failed", e);
        }
    }
    
    @AfterMethod
    public void tearDown() throws Exception {
        System.out.println("\n========== CLEANUP ==========");
        System.out.println("Press ENTER in this console to close browser...");
        
        try {
            // Wait for user to press Enter
            System.in.read();
        } catch (java.io.IOException e) {
            // If input fails, wait 10 seconds
            System.out.println("Input error, waiting 10 seconds... " + e.getMessage());
            try {
                Thread.sleep(10000);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                System.out.println("Sleep interrupted: " + ie.getMessage());
            }
        }
        
        if (driver != null) {
            driver.quit();
            System.out.println("Browser closed");
        }
    }
}