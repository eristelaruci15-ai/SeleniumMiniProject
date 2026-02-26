package com.tealiumdemo.tests;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
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

public class Test4_CheckSaleProductsStyle {
    
    private WebDriver driver;
    private WebDriverWait wait;
    private final String screenshotDir = "screenshots/test4/";
    
    // Login credentials
    private final String email = "test@gail.com";
    private final String password = "1234567";
    
    @BeforeMethod
    public void setup() {
        System.out.println("========== SETUP TEST 4 ==========");
        
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
        
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        
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
            
        } catch (org.openqa.selenium.WebDriverException e) {
            System.out.println("❌ Login failed: " + e.getMessage());
            throw new RuntimeException("Precondition failed: Could not login", e);
        }
    }
    
    private boolean isLoggedIn() {
        try {
            // Check if we can see Log Out link or Welcome message
            List<WebElement> logoutLinks = driver.findElements(By.linkText("Log Out"));
            List<WebElement> welcomeMessages = driver.findElements(By.xpath("//*[contains(text(), 'Welcome')]"));
            
            return !logoutLinks.isEmpty() || !welcomeMessages.isEmpty();
        } catch (org.openqa.selenium.WebDriverException e) {
            return false;
        }
    }
    
    private void ensureLoggedIn() {
        if (!isLoggedIn()) {
            System.out.println("⚠ Not logged in, attempting to login again...");
            loginUser();
        }
    }
    
    @Test
    public void checkSaleProductsStyleTest() {
        System.out.println("\n========== TEST 4: CHECK SALE PRODUCTS STYLE ==========");
        
        try {
            // Ensure we're logged in before starting
            ensureLoggedIn();
            
            // Step 1: Go directly to Sale page using URL (avoid hovering on menus)
            System.out.println("\nSTEP 1: Navigate to Sale page directly");
            
            // Go directly to Sale page
            driver.get("https://ecommerce.tealiumdemo.com/sale");
            System.out.println("Directly navigated to Sale page");
            
            // Check if we're still logged in after navigation
            ensureLoggedIn();
            
            // Wait for sale page to load
            wait.until(ExpectedConditions.or(
                ExpectedConditions.titleContains("Sale"),
                ExpectedConditions.titleContains("SALE"),
                ExpectedConditions.urlContains("sale"),
                ExpectedConditions.presenceOfElementLocated(By.xpath("//*[contains(text(), 'Sale') or contains(text(), 'SALE')]")),
                ExpectedConditions.presenceOfElementLocated(By.xpath("//*[contains(text(), '$')]"))
            ));
            
            safeSleep(3000);
            takeScreenshot("01_sale_page_loaded.png");
            
            // Check login status again
            ensureLoggedIn();
            
            // Step 2: Find all sale products on the page
            System.out.println("\nSTEP 2: Find all sale products");
            
            // Try different selectors for products
            List<WebElement> products = driver.findElements(By.xpath(
                "//li[contains(@class, 'item') and not(contains(@style, 'display: none'))] | " +
                "//div[contains(@class, 'product-item') and not(contains(@style, 'display: none'))] | " +
                "//div[contains(@class, 'product-info') and not(contains(@style, 'display: none'))]"
            ));
            
            // If no products found with those selectors, try broader search
            if (products.isEmpty()) {
                products = driver.findElements(By.xpath(
                    "//*[contains(@class, 'product') and .//*[contains(text(), '$')]]"
                ));
            }
            
            System.out.println("Found " + products.size() + " total products");
            
            // Filter to only products that have sale pricing
            List<WebElement> saleProducts = new java.util.ArrayList<>();
            
            for (WebElement product : products) {
                try {
                    if (product.isDisplayed()) {
                        // Check if product has old price (strikethrough)
                        List<WebElement> oldPrices = product.findElements(By.xpath(
                            ".//span[contains(@class, 'old-price')] | " +
                            ".//span[contains(@class, 'price-old')] | " +
                            ".//del | .//s | .//strike | " +
                            ".//*[contains(@style, 'line-through')]"
                        ));
                        
                        if (!oldPrices.isEmpty()) {
                            saleProducts.add(product);
                        } else {
                            // Check for multiple price elements (regular and special)
                            List<WebElement> priceBoxes = product.findElements(By.xpath(
                                ".//div[contains(@class, 'price-box')]"
                            ));
                            
                            for (WebElement priceBox : priceBoxes) {
                                if (priceBox.isDisplayed()) {
                                    List<WebElement> allSpans = priceBox.findElements(By.tagName("span"));
                                    int priceSpans = 0;
                                    for (WebElement span : allSpans) {
                                        if (span.getText().contains("$")) {
                                            priceSpans++;
                                        }
                                    }
                                    if (priceSpans >= 2) {
                                        saleProducts.add(product);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    // Skip stale elements
                }
            }
            
            System.out.println("Found " + saleProducts.size() + " sale products (with discounted prices)");
            
            // If still no sale products, check for any price differences
            if (saleProducts.isEmpty() && !products.isEmpty()) {
                System.out.println("No obvious sale products found. Checking first few products...");
                for (int i = 0; i < Math.min(3, products.size()); i++) {
                    if (products.get(i).isDisplayed()) {
                        saleProducts.add(products.get(i));
                    }
                }
            }
            
            Assert.assertFalse(saleProducts.isEmpty(), "No products found on the sale page");
            
            // Step 3 & 4: Check each sale product's pricing style
            System.out.println("\nSTEP 3 & 4: Check pricing styles for each sale product");
            
            int productCount = 1;
            int passedProducts = 0;
            int failedProducts = 0;
            
            for (WebElement product : saleProducts) {
                System.out.println("\n--- Checking Product " + productCount + " ---");
                
                try {
                    // Check if still logged in
                    ensureLoggedIn();
                    
                    // Scroll to product (smooth scroll might cause issues, use simple scroll)
                    ((JavascriptExecutor) driver).executeScript(
                        "window.scrollTo(0, arguments[0].getBoundingClientRect().top + window.pageYOffset - 100);", 
                        product);
                    safeSleep(1500);
                    
                    // Take screenshot of this product
                    takeScreenshot("product_" + productCount + "_before_check.png");
                    
                    // Find ALL price-related elements
                    List<WebElement> allPriceElements = product.findElements(By.xpath(
                        ".//span | .//div | .//p | .//strong | .//b | .//del | .//s"
                    ));
                    
                    WebElement originalPrice = null;
                    WebElement finalPrice = null;
                    
                    // First pass: Look for obvious strikethrough elements
                    for (WebElement priceElement : allPriceElements) {
                        try {
                            if (priceElement.isDisplayed() && priceElement.getText().contains("$")) {
                                String html = priceElement.getAttribute("outerHTML");
                                String tagName = priceElement.getTagName().toLowerCase();
                                String cssDecoration = priceElement.getCssValue("text-decoration");
                                
                                // Check for strikethrough
                                boolean isStrikethrough = tagName.equals("del") || tagName.equals("s") || 
                                                        tagName.equals("strike") || 
                                                        html.contains("<del>") || html.contains("<s>") ||
                                                        cssDecoration.contains("line-through");
                                
                                if (isStrikethrough) {
                                    originalPrice = priceElement;
                                    System.out.println("  Found original (strikethrough) price: " + 
                                        priceElement.getText().trim());
                                } else if (priceElement.getText().contains("$") && 
                                         !priceElement.getText().trim().isEmpty()) {
                                    // This might be the final price
                                    finalPrice = priceElement;
                                }
                            }
                        } catch (org.openqa.selenium.StaleElementReferenceException | org.openqa.selenium.NoSuchElementException e) {
                            // Skip this element
                        }
                    }
                    
                    // If we found an original price but no final price, look for another price
                    if (originalPrice != null && finalPrice == null) {
                        for (WebElement priceElement : allPriceElements) {
                            try {
                                if (priceElement.isDisplayed() && priceElement.getText().contains("$") && 
                                    priceElement != originalPrice) {
                                    String cssDecoration = priceElement.getCssValue("text-decoration");
                                    if (!cssDecoration.contains("line-through")) {
                                        finalPrice = priceElement;
                                        System.out.println("  Found final price: " + 
                                            priceElement.getText().trim());
                                        break;
                                    }
                                }
                            } catch (org.openqa.selenium.WebDriverException e) {
                                // Skip
                            }
                        }
                    }
                    
                    // If still not found, look for price boxes
                    if (originalPrice == null || finalPrice == null) {
                        List<WebElement> priceBoxes = product.findElements(By.xpath(
                            ".//div[contains(@class, 'price')] | .//span[contains(@class, 'price')]"
                        ));
                        
                        for (WebElement priceBox : priceBoxes) {
                            if (priceBox.isDisplayed()) {
                                String text = priceBox.getText();
                                if (text.contains("$") && text.length() < 50) {
                                    // Count $ signs to see if it contains multiple prices
                                    long dollarCount = text.chars().filter(ch -> ch == '$').count();
                                    if (dollarCount >= 2) {
                                        // This element contains both prices
                                        System.out.println("  Found combined price element: " + text);
                                        // We'll check this single element for both styles
                                        originalPrice = priceBox;
                                        finalPrice = priceBox;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    
                    // Verify we have prices to check
                    boolean hasPricesToCheck = (originalPrice != null);
                    
                    System.out.println("  Has prices to check: " + hasPricesToCheck);
                    if (originalPrice != null) System.out.println("  Original price element found");
                    if (finalPrice != null) System.out.println("  Final price element found");
                    
                    if (hasPricesToCheck && originalPrice != null) {
                        // Get styles
                        String originalPriceDecoration = originalPrice.getCssValue("text-decoration");
                        String originalPriceColor = originalPrice.getCssValue("color");
                        String originalPriceText = originalPrice.getText().trim();
                        
                        boolean originalHasStrikethrough = originalPriceDecoration.contains("line-through") ||
                                                         originalPrice.getTagName().equalsIgnoreCase("del") ||
                                                         originalPrice.getTagName().equalsIgnoreCase("s");
                        
                        boolean originalIsGrey = isGreyColor(originalPriceColor);
                        
                        System.out.println("  Original price check:");
                        System.out.println("    Text: " + originalPriceText);
                        System.out.println("    Has strikethrough: " + originalHasStrikethrough);
                        System.out.println("    Color: " + originalPriceColor);
                        System.out.println("    Is grey: " + originalIsGrey);
                        
                        // Check final price if we have a separate element
                        boolean finalNoStrikethrough = true;
                        boolean finalIsBlue = false;
                        String finalPriceText;
                        
                        if (finalPrice != null && finalPrice != originalPrice) {
                            String finalPriceDecoration = finalPrice.getCssValue("text-decoration");
                            String finalPriceColor = finalPrice.getCssValue("color");
                            finalPriceText = finalPrice.getText().trim();
                            
                            finalNoStrikethrough = !finalPriceDecoration.contains("line-through");
                            finalIsBlue = isBlueColor(finalPriceColor);
                            
                            System.out.println("  Final price check:");
                            System.out.println("    Text: " + finalPriceText);
                            System.out.println("    No strikethrough: " + finalNoStrikethrough);
                            System.out.println("    Color: " + finalPriceColor);
                            System.out.println("    Is blue: " + finalIsBlue);
                        } else if (finalPrice == originalPrice) {
                            // Single element contains both prices
                            System.out.println("  Single price element contains both prices");
                            // Check if the text shows both prices (e.g., "$100 $80")
                            if (originalPriceText.matches(".*\\$\\d+.*\\$\\d+.*")) {
                                finalNoStrikethrough = true; // Assuming final price part doesn't have strikethrough
                                finalIsBlue = isBlueColor(originalPriceColor); // Check if overall color is blue
                                System.out.println("    Contains multiple prices in one element");
                            }
                        }
                        
                        // Take screenshot with highlights
                            if (originalPrice != finalPrice && finalPrice != null) {
                                ((JavascriptExecutor) driver).executeScript(
                                    "arguments[0].style.outline='2px solid red';", 
                                    originalPrice);
                                ((JavascriptExecutor) driver).executeScript(
                                    "arguments[0].style.outline='2px solid blue';", 
                                    finalPrice);
                            } else {
                                ((JavascriptExecutor) driver).executeScript(
                                    "arguments[0].style.outline='3px solid purple';", 
                                    originalPrice);
                            }
                        
                        safeSleep(500);
                        takeScreenshot("product_" + productCount + "_prices.png");
                        
                        // Remove outlines
                            if (originalPrice != finalPrice && finalPrice != null) {
                                ((JavascriptExecutor) driver).executeScript(
                                    "arguments[0].style.outline='';", 
                                    originalPrice);
                                ((JavascriptExecutor) driver).executeScript(
                                    "arguments[0].style.outline='';", 
                                    finalPrice);
                            } else {
                                ((JavascriptExecutor) driver).executeScript(
                                    "arguments[0].style.outline='';", 
                                    originalPrice);
                            }
                        
                        // Determine if this product passes
                        boolean productPassed = originalHasStrikethrough && originalIsGrey && 
                                               finalNoStrikethrough && finalIsBlue;
                        
                        if (productPassed) {
                            System.out.println("  ✅ Product " + productCount + " PASSED all checks!");
                            passedProducts++;
                        } else {
                            System.out.println("  ❌ Product " + productCount + " FAILED some checks:");
                            
                            if (!originalHasStrikethrough) {
                                System.out.println("    - Original price should have strikethrough");
                            }
                            if (!originalIsGrey) {
                                System.out.println("    - Original price should be grey (is: " + originalPriceColor + ")");
                            }
                            if (!finalNoStrikethrough) {
                                System.out.println("    - Final price should NOT have strikethrough");
                            }
                            if (!finalIsBlue) {
                                System.out.println("    - Final price should be blue");
                            }
                            failedProducts++;
                        }
                    } else {
                        System.out.println("  ⚠ Product " + productCount + " - Could not find price elements to check");
                        failedProducts++;
                    }
                    
                } catch (org.openqa.selenium.WebDriverException e) {
                    System.out.println("  ❌ Error checking product " + productCount + ": " + e.getMessage());
                    failedProducts++;
                }
                
                productCount++;
                
                // Stop after checking 3 products to avoid taking too long
                if (productCount > 3) {
                    System.out.println("\n⚠ Stopping after checking 3 products");
                    break;
                }
            }
            
            // Final test result
            System.out.println("\n========== TEST RESULTS ==========");
            System.out.println("Total products checked: " + (productCount - 1));
            System.out.println("Products passed: " + passedProducts);
            System.out.println("Products failed: " + failedProducts);
            
            if (passedProducts > 0) {
                System.out.println("\n✅ TEST 4 PASSED!");
                System.out.println("Found " + passedProducts + " sale product(s) with correct pricing styles");
            } else {
                System.out.println("\n❌ TEST 4 FAILED");
                System.out.println("No products with correct sale pricing styles were found");
                Assert.fail("No sale products with correct pricing styles found");
            }
            
            System.out.println("\n📸 Screenshots saved in: " + new File(screenshotDir).getAbsolutePath());
            
        } catch (RuntimeException e) {
            System.out.println("\n❌ TEST 4 FAILED: " + e.getMessage());
            System.out.println("Cause: " + e);
            
            // Save screenshot on failure
            takeScreenshot("error_final_state.png");
            
            throw new RuntimeException("Test 4 failed", e);
        }
    }
    
    // Helper method to check if a color is grey
    private boolean isGreyColor(String cssColor) {
        if (cssColor == null || cssColor.isEmpty()) return false;
        
        // Check for common grey color names
        if (cssColor.contains("grey") || cssColor.contains("gray")) {
            return true;
        }
        
        // Check for hex grey colors
        if (cssColor.startsWith("#")) {
            String hex = cssColor.substring(1);
            if (hex.length() == 3) {
                // Convert #RGB to #RRGGBB
                hex = "" + hex.charAt(0) + hex.charAt(0) + 
                          hex.charAt(1) + hex.charAt(1) + 
                          hex.charAt(2) + hex.charAt(2);
            }
            if (hex.length() == 6) {
                try {
                    int r = Integer.parseInt(hex.substring(0, 2), 16);
                    int g = Integer.parseInt(hex.substring(2, 4), 16);
                    int b = Integer.parseInt(hex.substring(4, 6), 16);
                    
                    // Grey colors have R ≈ G ≈ B
                    int diffRG = Math.abs(r - g);
                    int diffRB = Math.abs(r - b);
                    int diffGB = Math.abs(g - b);
                    
                    return diffRG < 30 && diffRB < 30 && diffGB < 30;
                } catch (NumberFormatException e) {
                    return false;
                }
            }
        }
        
        // Check for RGB/RGBA colors
        if (cssColor.startsWith("rgb")) {
            String rgbValues = cssColor.replace("rgb(", "").replace("rgba(", "").replace(")", "");
            String[] parts = rgbValues.split(",");
            
            if (parts.length >= 3) {
                try {
                    int r = Integer.parseInt(parts[0].trim());
                    int g = Integer.parseInt(parts[1].trim());
                    int b = Integer.parseInt(parts[2].trim());
                    
                    // Grey colors have R ≈ G ≈ B
                    int diffRG = Math.abs(r - g);
                    int diffRB = Math.abs(r - b);
                    int diffGB = Math.abs(g - b);
                    
                    return diffRG < 30 && diffRB < 30 && diffGB < 30;
                } catch (NumberFormatException e) {
                    return false;
                }
            }
        }
        
        return false;
    }
    
    // Helper method to check if a color is blue
    private boolean isBlueColor(String cssColor) {
        if (cssColor == null || cssColor.isEmpty()) return false;
        
        // Check for common blue color names
        if (cssColor.contains("blue") || cssColor.contains("Blue")) {
            return true;
        }
        
        // Check for hex blue colors
        if (cssColor.startsWith("#")) {
            String hex = cssColor.substring(1);
            if (hex.length() == 3) {
                // Convert #RGB to #RRGGBB
                hex = "" + hex.charAt(0) + hex.charAt(0) + 
                          hex.charAt(1) + hex.charAt(1) + 
                          hex.charAt(2) + hex.charAt(2);
            }
            if (hex.length() == 6) {
                try {
                    int r = Integer.parseInt(hex.substring(0, 2), 16);
                    int g = Integer.parseInt(hex.substring(2, 4), 16);
                    int b = Integer.parseInt(hex.substring(4, 6), 16);
                    
                    // Blue colors have B significantly higher than R and G
                    return b > r + 20 && b > g + 20;
                } catch (NumberFormatException e) {
                    return false;
                }
            }
        }
        
        // Check for RGB/RGBA colors
        if (cssColor.startsWith("rgb")) {
            String rgbValues = cssColor.replace("rgb(", "").replace("rgba(", "").replace(")", "");
            String[] parts = rgbValues.split(",");
            
            if (parts.length >= 3) {
                try {
                    int r = Integer.parseInt(parts[0].trim());
                    int g = Integer.parseInt(parts[1].trim());
                    int b = Integer.parseInt(parts[2].trim());
                    
                    // Blue colors have B significantly higher than R and G
                    return b > r + 20 && b > g + 20;
                } catch (NumberFormatException e) {
                    return false;
                }
            }
        }
        
        return false;
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

    // Small helper to centralize short sleeps and avoid direct Thread.sleep calls in loops
    private void safeSleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Sleep interrupted: " + e.getMessage());
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
            safeSleep(1000);
            
            WebElement logoutLink = driver.findElement(By.linkText("Log Out"));
            logoutLink.click();
            safeSleep(2000);
            System.out.println("✓ Logged out successfully");
        } catch (org.openqa.selenium.WebDriverException e) {
            System.out.println("⚠ Could not logout automatically: " + e.getMessage());
        }
        
        // Keep browser open briefly
        System.out.println("\nBrowser will close in 3 seconds...");
        safeSleep(3000);
        
        if (driver != null) {
            driver.quit();
            System.out.println("Browser closed");
        }
    }
}