package com.tealiumdemo.tests;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class Test3_CheckHoverStyle {
    
    private WebDriver driver;
    private WebDriverWait wait;
    private Actions actions;
    private final String screenshotDir = "screenshots/test3/";
    
    // Login credentials
    private final String email = "test@gail.com";
    private final String password = "1234567";
    
    @BeforeMethod
    public void setup() {
        System.out.println("========== SETUP TEST 3 ==========");
        
        // Create screenshot directory
        try {
            Files.createDirectories(Paths.get(screenshotDir));
        } catch (IOException e) {
            System.out.println("Could not create screenshot directory: " + e.getMessage());
        }
        
        // Setup ChromeDriver
        System.setProperty("webdriver.chrome.driver", "chromedriver.exe");
        
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        options.addArguments("--remote-allow-origins=*");
        // Remove the automation control banner if needed
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
        
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        actions = new Actions(driver);
        
        // Navigate and login
        driver.get("https://ecommerce.tealiumdemo.com/");
        System.out.println("Opened: " + driver.getTitle());
        
        loginUser();
    }
    
    private void loginUser() {
        System.out.println("\n--- Logging in ---");
        
        try {
            // Click Account -> Log In
            WebElement accountLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[contains(text(), 'Account')]")));
            accountLink.click();
            
            WebElement loginLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.linkText("Log In")));
            loginLink.click();
            
            // Enter credentials
            wait.until(ExpectedConditions.presenceOfElementLocated(By.id("email")));
            driver.findElement(By.id("email")).sendKeys(email);
            driver.findElement(By.id("pass")).sendKeys(password);
            
            // Click login
            driver.findElement(By.id("send2")).click();
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                System.out.println("Sleep interrupted: " + ie.getMessage());
            }
            
            System.out.println("✓ Logged in successfully");
            
        } catch (org.openqa.selenium.TimeoutException | NoSuchElementException e) {
            System.out.println("❌ Login failed: " + e.getMessage());
            throw new RuntimeException("Precondition failed: Could not login", e);
        }
    }
    
    @Test
    public void checkHoverStyleTest() {
        System.out.println("\n========== TEST 3: CHECK HOVER STYLE ==========");
        
        try {
            // Step 1: Navigate to Women's page
            System.out.println("\nSTEP 1: Navigate to Women's products page");
            
            // Go directly to Women's page using URL
            driver.get("https://ecommerce.tealiumdemo.com/women");
            System.out.println("Directly navigated to Women's page");
            
            // Wait for page to load
            wait.until(ExpectedConditions.or(
            ExpectedConditions.titleContains("Women"),
            ExpectedConditions.titleContains("WOMEN"),
            ExpectedConditions.presenceOfElementLocated(By.xpath("//h1[contains(text(), 'Women') or contains(text(), 'WOMEN')]")),
            ExpectedConditions.presenceOfElementLocated(By.xpath("//*[contains(text(), '$')]"))
        ));
            
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                System.out.println("Sleep interrupted: " + ie.getMessage());
            }
            takeScreenshot("01_women_page_loaded.png");
            
            // Step 2: Find products - using specific selectors for this site
            System.out.println("\nSTEP 2: Find products on the page");
            
            // Based on your screenshot, products are in list items with class 'item'
            List<WebElement> products = driver.findElements(By.cssSelector("li.item, div.product-item, [class*='product-item']"));
            
            if (products.isEmpty()) {
                // Try alternative selectors
                products = driver.findElements(By.xpath("//*[contains(text(), 'VIEW DETAILS')]/ancestor::li | //*[contains(text(), '$')]/ancestor::li"));
            }
            
            Assert.assertFalse(products.isEmpty(), "No products found on the page");
            System.out.println("Found " + products.size() + " products");
            
            // Select the first product (PARK AVENUE PLEAT FRONT TROUSERS from your screenshot)
            WebElement firstProduct = products.get(0);
            
            // Scroll to the product
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", firstProduct);
            try {
                Thread.sleep(1500);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                System.out.println("Sleep interrupted: " + ie.getMessage());
            }
            
            // Take BEFORE screenshot
            System.out.println("Taking BEFORE hover screenshot...");
            takeScreenshot("02_before_hover.png");
            
            // Step 3: Check what elements exist BEFORE hover
            System.out.println("\nSTEP 3: Check for hover elements BEFORE hover");
            
            // Look for VIEW DETAILS link (should be hidden before hover)
            List<WebElement> viewDetailsBefore = firstProduct.findElements(By.xpath(".//a[contains(text(), 'VIEW DETAILS')]"));
            System.out.println("VIEW DETAILS links found before hover: " + viewDetailsBefore.size());
            
            // Look for Add to Wishlist (should be hidden before hover)
            List<WebElement> wishlistBefore = firstProduct.findElements(By.xpath(".//a[contains(text(), 'Add to Wishlist')]"));
            System.out.println("Add to Wishlist links found before hover: " + wishlistBefore.size());
            
            // Look for Add to Compare (should be hidden before hover)
            List<WebElement> compareBefore = firstProduct.findElements(By.xpath(".//a[contains(text(), 'Add to Compare')]"));
            System.out.println("Add to Compare links found before hover: " + compareBefore.size());
            
            // Step 4: Hover over the product
            System.out.println("\nSTEP 4: Hover over the product");
            
            // Move to the product image or the product container
            WebElement productImage = firstProduct.findElement(By.cssSelector("img, .product-image"));
            actions.moveToElement(productImage).pause(1000).perform();
            System.out.println("Hovered over product image");
            try {
                Thread.sleep(2000); // Give time for hover effects to appear
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                System.out.println("Sleep interrupted: " + ie.getMessage());
            }
            
            // Take DURING hover screenshot
            System.out.println("Taking DURING hover screenshot...");
            takeScreenshot("03_during_hover.png");
            
            // Step 5: Check what elements appear AFTER hover
            System.out.println("\nSTEP 5: Check for hover elements AFTER hover");
            
            // Look for VIEW DETAILS link (should be visible after hover)
            List<WebElement> viewDetailsAfter = firstProduct.findElements(By.xpath(".//a[contains(text(), 'VIEW DETAILS')]"));
            System.out.println("VIEW DETAILS links found after hover: " + viewDetailsAfter.size());
            
            // Check if VIEW DETAILS is now visible
            boolean viewDetailsVisible = false;
            for (WebElement link : viewDetailsAfter) {
                if (link.isDisplayed()) {
                    viewDetailsVisible = true;
                    System.out.println("✓ VIEW DETAILS is now visible!");
                    // Highlight it
                    ((JavascriptExecutor) driver).executeScript("arguments[0].style.border='2px solid #00FF00';", link);
                    break;
                }
            }
            
            // Look for Add to Wishlist (should be visible after hover)
            List<WebElement> wishlistAfter = firstProduct.findElements(By.xpath(".//a[contains(text(), 'Add to Wishlist')]"));
            System.out.println("Add to Wishlist links found after hover: " + wishlistAfter.size());
            
            boolean wishlistVisible = false;
            for (WebElement link : wishlistAfter) {
                if (link.isDisplayed()) {
                    wishlistVisible = true;
                    System.out.println("✓ Add to Wishlist is now visible!");
                    break;
                }
            }
            
            // Look for Add to Compare (should be visible after hover)
            List<WebElement> compareAfter = firstProduct.findElements(By.xpath(".//a[contains(text(), 'Add to Compare')]"));
            System.out.println("Add to Compare links found after hover: " + compareAfter.size());
            
            boolean compareVisible = false;
            for (WebElement link : compareAfter) {
                if (link.isDisplayed()) {
                    compareVisible = true;
                    System.out.println("✓ Add to Compare is now visible!");
                    break;
                }
            }
            
            // Step 6: Verify hover effect worked
            System.out.println("\nSTEP 6: Verify hover effect");
            
            boolean hoverEffectDetected = viewDetailsVisible || wishlistVisible || compareVisible;
            
            if (hoverEffectDetected) {
                System.out.println("\n✅ HOVER EFFECT SUCCESSFULLY DETECTED!");
                
                if (viewDetailsVisible) {
                    System.out.println("  - VIEW DETAILS link appeared on hover");
                }
                if (wishlistVisible) {
                    System.out.println("  - Add to Wishlist link appeared on hover");
                }
                if (compareVisible) {
                    System.out.println("  - Add to Compare link appeared on hover");
                }
                
                // Take one more screenshot to document the success
                takeScreenshot("04_hover_success.png");
                
                System.out.println("\n✅ TEST 3 PASSED!");
                
                // Also check CSS style changes if any
                try {
                    String productClass = firstProduct.getAttribute("class");
                    System.out.println("Product CSS classes: " + productClass);
                    
                    // Check if hover class was added
                    if (productClass.contains("hover") || productClass.contains("active")) {
                        System.out.println("CSS hover class detected: " + productClass);
                    }
                } catch (Exception e) {
                    // Ignore CSS check errors
                }
                
            } else {
                System.out.println("\n❌ HOVER EFFECT NOT DETECTED");
                System.out.println("The hover elements did not become visible.");
                
                // Debug: Check if we're hovering on the right element
                System.out.println("Debug info:");
                System.out.println("  Product location: " + firstProduct.getLocation());
                System.out.println("  Product size: " + firstProduct.getSize());
                System.out.println("  Product tag: " + firstProduct.getTagName());
                System.out.println("  Product class: " + firstProduct.getAttribute("class"));
                
                // Try hovering on the entire product div instead
                System.out.println("\nTrying alternative hover method...");
                actions.moveToElement(firstProduct).pause(1000).perform();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    System.out.println("Sleep interrupted: " + ie.getMessage());
                }
                takeScreenshot("05_alternative_hover.png");
                
                // Check again
                viewDetailsAfter = firstProduct.findElements(By.xpath(".//a[contains(text(), 'VIEW DETAILS')]"));
                for (WebElement link : viewDetailsAfter) {
                    if (link.isDisplayed()) {
                        System.out.println("✓ Found VIEW DETAILS with alternative hover!");
                        hoverEffectDetected = true;
                        break;
                    }
                }
                
                if (!hoverEffectDetected) {
                    Assert.fail("Hover effect not detected. The 'VIEW DETAILS', 'Add to Wishlist', or 'Add to Compare' links did not appear on hover.");
                }
            }
            
            System.out.println("\n📸 All screenshots saved in: " + new File(screenshotDir).getAbsolutePath());
            
        } catch (RuntimeException e) {
            System.out.println("\n❌ TEST 3 FAILED: " + e.getMessage());
            System.out.println("Cause: " + e);
            
            // Save screenshot on failure
            takeScreenshot("error_final_state.png");
            
            throw new RuntimeException("Test 3 failed", e);
        }
    }
    
    private void takeScreenshot(String filename) {
        try {
            TakesScreenshot ts = (TakesScreenshot) driver;
            File source = ts.getScreenshotAs(OutputType.FILE);
            File destination = new File(screenshotDir + filename);
            
            Files.copy(source.toPath(), destination.toPath(), 
                      java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            
            System.out.println("  📸 Screenshot saved: " + filename);
        } catch (java.io.IOException | org.openqa.selenium.WebDriverException e) {
            System.out.println("  ❌ Could not save screenshot '" + filename + "': " + e.getMessage());
        }
    }
    
    @AfterMethod
    public void tearDown() {
        System.out.println("\n========== CLEANUP ==========");
        
        try {
            // Try to logout
            System.out.println("Attempting logout...");
            WebElement accountLink = driver.findElement(By.xpath("//a[contains(text(), 'Account')]"));
            accountLink.click();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                System.out.println("Sleep interrupted: " + ie.getMessage());
            }
            
            WebElement logoutLink = driver.findElement(By.linkText("Log Out"));
            logoutLink.click();
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                System.out.println("Sleep interrupted: " + ie.getMessage());
            }
            System.out.println("✓ Logged out successfully");
        } catch (org.openqa.selenium.WebDriverException e) {
            System.out.println("⚠ Could not logout automatically: " + e.getMessage());
        }
        
        // Keep browser open for debugging
        System.out.println("\nBrowser will remain open for 5 seconds for inspection...");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            // Ignore
        }
        
        if (driver != null) {
            driver.quit();
            System.out.println("Browser closed");
        }
    }
}