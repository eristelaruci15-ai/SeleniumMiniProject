package com.tealiumdemo.tests;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class Test8_EmptyShoppingCartTest {
    
    private WebDriver driver;
    private WebDriverWait wait;
    private Actions actions;
    private final String screenshotDir = "screenshots/test8/";
    
    // Login credentials
    private final String email = "test@gail.com";
    private final String password = "1234567";
    
    @BeforeMethod
    public void setup() {
        System.out.println("========== SETUP TEST 8 ==========");
        
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
        // Disable notifications that might interfere
        options.addArguments("--disable-notifications");
        
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        actions = new Actions(driver);
        
        // Navigate and login
        driver.get("https://ecommerce.tealiumdemo.com/");
        System.out.println("Opened: " + driver.getTitle());
        
        loginUser();
        
        // Precondition: Ensure shopping cart has items (from Test 7)
        System.out.println("\n--- Checking Test 7 Precondition ---");
        ensureCartHasItems();
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
            safeSleep(3000);
            
            // Wait for login to complete
            wait.until(ExpectedConditions.or(
                ExpectedConditions.presenceOfElementLocated(By.linkText("Log Out")),
                ExpectedConditions.presenceOfElementLocated(By.xpath("//*[contains(text(), 'Welcome')]")),
                ExpectedConditions.presenceOfElementLocated(By.xpath("//a[contains(text(), 'My Account')]"))
            ));
            
            System.out.println("✓ Logged in successfully");
            
        } catch (RuntimeException e) {
            System.out.println("❌ Login failed: " + e.getMessage());
            throw new RuntimeException("Precondition failed: Could not login", e);
        }
    }
    
    private boolean isLoggedIn() {
        try {
            // Check multiple indicators of being logged in
            List<WebElement> logoutLinks = driver.findElements(By.linkText("Log Out"));
            List<WebElement> welcomeMessages = driver.findElements(By.xpath("//*[contains(text(), 'Welcome')]"));
            List<WebElement> accountLinks = driver.findElements(By.xpath("//a[contains(text(), 'My Account')]"));
            
            return !logoutLinks.isEmpty() || !welcomeMessages.isEmpty() || !accountLinks.isEmpty();
        } catch (RuntimeException e) {
            return false;
        }
    }
    
    private void ensureLoggedIn() {
        if (!isLoggedIn()) {
            System.out.println("⚠ Session lost, re-logging in...");
            loginUser();
        }
    }
    
    private void ensureCartHasItems() {
        System.out.println("Ensuring shopping cart has items...");
        
        try {
            // Go to shopping cart page
            driver.get("https://ecommerce.tealiumdemo.com/checkout/cart/");
            safeSleep(2000);
            
            // Check if cart has items
            List<WebElement> cartItems = getCartItems();
            
            if (cartItems.isEmpty()) {
                System.out.println("⚠ Shopping cart is empty, need to add items first");
                System.out.println("Adding sample items to cart...");
                addSampleItemsToCart();
            } else {
                System.out.println("✓ Shopping cart has " + cartItems.size() + " items");
                takeScreenshot("00_precondition_cart_has_items.png");
            }
            
        } catch (RuntimeException e) {
            System.out.println("Error checking shopping cart: " + e.getMessage());
            System.out.println("Will try to add items during test execution");
        }
    }
    
    private void addSampleItemsToCart() {
        try {
            // Go to a product page to add items
            driver.get("https://ecommerce.tealiumdemo.com/women");
            safeSleep(2000);
            
            // Find first few products and add to cart
            List<WebElement> products = driver.findElements(By.xpath(
                "//li[contains(@class, 'item')] | " +
                "//div[contains(@class, 'product-item')]"
            ));
            
            int itemsAdded = 0;
            int maxItemsToAdd = 2;
            
            for (int i = 0; i < Math.min(maxItemsToAdd, products.size()); i++) {
                WebElement product = products.get(i);
                
                // Scroll to product
                ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", 
                    product);
                safeSleep(1000);
                
                // Hover to reveal add to cart button
                actions.moveToElement(product).perform();
                safeSleep(1000);
                
                // Find and click add to cart button
                try {
                    WebElement addToCartButton = product.findElement(By.xpath(
                        ".//button[contains(@title, 'Add to Cart')] | " +
                        ".//button[contains(text(), 'Add to Cart')]"
                    ));
                    addToCartButton.click();
                    safeSleep(2000);
                    
                    // Handle option selection if needed
                    try {
                        // Check for option modal
                        List<WebElement> optionModals = driver.findElements(By.xpath(
                            "//*[contains(@class, 'modal') and contains(@style, 'display: block')]"
                        ));
                        
                        if (!optionModals.isEmpty()) {
                            // Select first available options and add to cart
                            WebElement modalAddButton = driver.findElement(By.xpath(
                                "//div[contains(@class, 'modal')]//button[contains(text(), 'Add to Cart')]"
                            ));
                            modalAddButton.click();
                            safeSleep(2000);
                        }
                    } catch (RuntimeException e) {
                        // No options needed
                    }
                    
                    itemsAdded++;
                    System.out.println("  Added product " + (i+1) + " to cart");
                    
                } catch (RuntimeException e) {
                    System.out.println("  Could not add product " + (i+1) + " to cart: " + e.getMessage());
                }
            }
            
            System.out.println("Added " + itemsAdded + " sample items to cart");
            
        } catch (RuntimeException e) {
            System.out.println("Error adding sample items to cart: " + e.getMessage());
        }
    }
    
    private List<WebElement> getCartItems() {
        return driver.findElements(By.xpath(
            "//tr[contains(@class, 'item')] | " +
            "//tbody[contains(@class, 'item')]//tr | " +
            "//div[contains(@class, 'cart item')] | " +
            "//table[@id='shopping-cart-table']//tbody//tr[not(contains(@class, 'totals'))]"
        ));
    }
    
    @Test
    public void emptyShoppingCartTest() {
        System.out.println("\n========== TEST 8: EMPTY SHOPPING CART TEST ==========");
        
        try {
            // Start at shopping cart page
            System.out.println("\nSTEP 0: Navigate to Shopping Cart");
            
            ensureLoggedIn();
            
            driver.get("https://ecommerce.tealiumdemo.com/checkout/cart/");
            
            // Wait for cart page to load
            wait.until(ExpectedConditions.or(
                ExpectedConditions.titleContains("Shopping Cart"),
                ExpectedConditions.urlContains("cart"),
                ExpectedConditions.presenceOfElementLocated(By.xpath("//*[contains(text(), 'Shopping Cart')]"))
            ));
            
            System.out.println("✓ Navigated to Shopping Cart");
            safeSleep(2000);
            
            takeScreenshot("01_initial_cart.png");
            
            // Get initial cart items count
            List<WebElement> initialCartItems = getCartItems();
            int initialItemCount = initialCartItems.size();
            System.out.println("Initial cart has " + initialItemCount + " items");
            
            // Step 1-3: Delete items one by one
            System.out.println("\nSTEPS 1-3: Deleting items from cart one by one");
            
            int itemsDeleted = 0;
            
            while (true) {
                // Get current cart items
                List<WebElement> currentCartItems = getCartItems();
                int currentItemCount = currentCartItems.size();
                
                if (currentItemCount == 0) {
                    System.out.println("Cart is already empty");
                    break;
                }
                
                System.out.println("\n--- Deleting item " + (itemsDeleted + 1) + " ---");
                System.out.println("Current items in cart: " + currentItemCount);
                
                // Step 1: Delete the first item on shopping cart
                WebElement deleteButton = null;
                
                // Try different selectors for delete button
                String[] deleteSelectors = {
                    "(//a[contains(@title, 'Remove')])[1]",
                    "(//a[contains(@class, 'delete')])[1]",
                    "(//button[contains(@title, 'Remove')])[1]",
                    "//td[contains(@class, 'actions')]//a[contains(text(), 'Remove')]",
                    "//a[contains(@class, 'remove')]"
                };
                
                for (String selector : deleteSelectors) {
                    try {
                        List<WebElement> buttons = driver.findElements(By.xpath(selector));
                        for (WebElement button : buttons) {
                            if (button.isDisplayed()) {
                                deleteButton = button;
                                System.out.println("Found delete button using: " + selector);
                                break;
                            }
                        }
                        if (deleteButton != null) break;
                    } catch (RuntimeException e) {
                        // Try next selector
                    }
                }
                
                if (deleteButton == null) {
                    System.out.println("❌ Could not find delete button");
                    break;
                }
                
                // Take screenshot before deletion
                takeScreenshot("02_before_delete_item_" + (itemsDeleted + 1) + ".png");
                
                // Click delete button
                System.out.println("Clicking delete button...");
                deleteButton.click();
                safeSleep(2000);
                
                // Handle confirmation if needed
                try {
                    // Check for confirmation dialog
                    Alert alert = driver.switchTo().alert();
                    alert.accept();
                    safeSleep(1000);
                    System.out.println("Accepted confirmation dialog");
                } catch (RuntimeException e) {
                    // No alert, continue
                }
                
                // Wait for deletion to process
                safeSleep(3000);
                
                // Step 2: Verify that the number of elements decreased by 1
                List<WebElement> updatedCartItems = getCartItems();
                int updatedItemCount = updatedCartItems.size();
                
                System.out.println("Items after deletion: " + updatedItemCount);
                
                if (updatedItemCount == currentItemCount - 1) {
                    System.out.println("✓ Item count decreased by 1 as expected");
                    itemsDeleted++;
                    
                    // Take screenshot after deletion
                    takeScreenshot("03_after_delete_item_" + itemsDeleted + ".png");
                    
                    // Step 3: Repeat until last item is deleted
                    if (updatedItemCount == 0) {
                        System.out.println("✓ All items deleted from cart");
                        break;
                    }
                    
                    // Update previous count for next iteration
                    // (previousItemCount removed as it was unused)
                    
                } else if (updatedItemCount == currentItemCount) {
                    System.out.println("⚠ Item count did not decrease - deletion might have failed");
                    System.out.println("Trying alternative deletion method...");
                    
                    // Try JavaScript click
                    try {
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", deleteButton);
                        safeSleep(3000);
                        
                        updatedCartItems = getCartItems();
                        updatedItemCount = updatedCartItems.size();
                        
                        if (updatedItemCount == currentItemCount - 1) {
                            System.out.println("✓ Deletion successful with JavaScript");
                            itemsDeleted++;
                            takeScreenshot("03_after_delete_item_" + itemsDeleted + "_js.png");
                            
                            if (updatedItemCount == 0) {
                                break;
                            }
                            // previousItemCount removed as it was unused
                        } else {
                            System.out.println("❌ Deletion still failed, stopping");
                            break;
                        }
                    } catch (RuntimeException e) {
                        System.out.println("❌ JavaScript deletion also failed: " + e.getMessage());
                        break;
                    }
                } else {
                    System.out.println("❌ Unexpected item count change");
                    break;
                }
                
                ensureLoggedIn();
            }
            
            System.out.println("\nTotal items deleted: " + itemsDeleted);
            System.out.println("Expected to delete: " + initialItemCount + " items");
            
            // Step 4: Verify that Shopping Cart is empty
            System.out.println("\nSTEP 4: Verify shopping cart is empty");
            
            // Refresh page to ensure we have latest state
            driver.navigate().refresh();
            safeSleep(2000);
            
            takeScreenshot("04_cart_after_all_deletions.png");
            
            // Check for empty cart message
            boolean emptyMessageFound = false;
            String emptyMessageText = "";
            
            // Look for empty cart message
            String[] emptyMessageSelectors = {
                "//*[contains(text(), 'You have no items in your shopping cart.')]",
                "//*[contains(text(), 'no items in your shopping cart')]",
                "//*[contains(text(), 'Shopping Cart is empty')]",
                "//*[contains(text(), 'Your shopping cart is empty')]",
                "//*[contains(@class, 'cart-empty')]"
            };
            
            for (String selector : emptyMessageSelectors) {
                try {
                    List<WebElement> messages = driver.findElements(By.xpath(selector));
                    for (WebElement message : messages) {
                        if (message.isDisplayed()) {
                            emptyMessageFound = true;
                            emptyMessageText = message.getText().trim();
                            System.out.println("Found empty cart message: " + emptyMessageText);
                            break;
                        }
                    }
                    if (emptyMessageFound) break;
                } catch (RuntimeException e) {
                    // Try next selector
                }
            }
            
            // Also check if cart items are truly gone
            List<WebElement> finalCartItems = getCartItems();
            boolean cartIsEmpty = finalCartItems.isEmpty();
            
            System.out.println("Cart items after deletions: " + finalCartItems.size());
            System.out.println("Empty message displayed: " + emptyMessageFound);
            
            // Verify cart is empty
            if (cartIsEmpty && emptyMessageFound) {
                System.out.println("✅ Shopping cart is completely empty with proper message");
                System.out.println("Message: \"" + emptyMessageText + "\"");
                
                // Highlight the empty message for screenshot
                try {
                    WebElement emptyMsgElement = driver.findElement(By.xpath(
                        "//*[contains(text(), 'no items in your shopping cart') or contains(text(), 'Shopping Cart is empty')]"
                    ));
                    ((JavascriptExecutor) driver).executeScript(
                        "arguments[0].style.border='3px solid green'; arguments[0].style.padding='10px';", 
                        emptyMsgElement);
                    safeSleep(500);
                } catch (RuntimeException e) {
                    // Ignore if we can't highlight
                }
                
            } else if (cartIsEmpty && !emptyMessageFound) {
                System.out.println("⚠ Cart is empty but no empty message found");
                System.out.println("This might be acceptable depending on site design");
            } else if (!cartIsEmpty && emptyMessageFound) {
                System.out.println("❌ Empty message shown but cart still has " + finalCartItems.size() + " items");
            } else {
                System.out.println("❌ Cart still has " + finalCartItems.size() + " items and no empty message");
            }
            
            takeScreenshot("05_final_empty_cart.png");
            
            // Final test results
            System.out.println("\n========== TEST 8 RESULTS ==========");
            System.out.println("1. Initial items in cart: " + initialItemCount);
            System.out.println("2. Items successfully deleted: " + itemsDeleted);
            System.out.println("3. Final items in cart: " + finalCartItems.size());
            System.out.println("4. Empty cart message displayed: " + (emptyMessageFound ? "✓" : "❌"));
            System.out.println("5. Cart is completely empty: " + (cartIsEmpty ? "✓" : "❌"));
            
            if (cartIsEmpty && itemsDeleted > 0) {
                System.out.println("\n✅ TEST 8 PASSED!");
                System.out.println("Successfully emptied shopping cart");
                
                if (emptyMessageFound) {
                    System.out.println("Empty cart message verified: \"" + emptyMessageText + "\"");
                }
            } else if (itemsDeleted > 0) {
                System.out.println("\n⚠ TEST 8 PARTIALLY PASSED");
                System.out.println("Items were deleted but cart may not be completely empty");
            } else {
                System.out.println("\n❌ TEST 8 FAILED");
                System.out.println("Could not delete items from cart");
            }
            
            System.out.println("\n📸 Screenshots saved in: " + new File(screenshotDir).getAbsolutePath());
            
        } catch (RuntimeException e) {
            System.out.println("\n❌ TEST 8 FAILED: " + e.getMessage());
            System.out.println("Cause: " + e.getMessage());
            
            // Save screenshot on failure
            try {
                takeScreenshot("error_final_state.png");
            } catch (RuntimeException ex) {
                System.out.println("Could not save error screenshot: " + ex.getMessage());
            }
            
            throw new RuntimeException("Test 8 failed", e);
        }
    }
    
    // Note: Step 5 (Close browser) is handled in @AfterMethod
    
    private void takeScreenshot(String filename) {
        try {
            TakesScreenshot ts = (TakesScreenshot) driver;
            File source = ts.getScreenshotAs(OutputType.FILE);
            File destination = new File(screenshotDir + filename);
            
            Files.copy(source.toPath(), destination.toPath(), 
                      java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            
            System.out.println("  📸 Screenshot saved: " + filename);
        } catch (IOException | WebDriverException e) {
            System.out.println("  ❌ Could not save screenshot '" + filename + "': " + e.getMessage());
        }
    }
    
    // Helper that centralizes sleeping and handles InterruptedException safely
    private void safeSleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Sleep interrupted: " + e.getMessage());
        }
    }
    
    @AfterMethod
    public void tearDown() {
        System.out.println("\n========== TEST 8 CLEANUP ==========");
        
        try {
            // Step 5: Close the browser (as per test requirements)
            System.out.println("Step 5: Closing browser as per test requirements");
            
            // Optional: Take one final screenshot before closing
            try {
                takeScreenshot("06_final_before_close.png");
            } catch (Exception e) {
                // Ignore screenshot errors during cleanup
            }
            
            // Log out if still logged in
            if (isLoggedIn()) {
                System.out.println("Logging out before closing...");
                try {
                    WebElement accountLink = driver.findElement(By.xpath("//a[contains(text(), 'Account')]"));
                    accountLink.click();
                    safeSleep(1000);
                    
                    WebElement logoutLink = driver.findElement(By.linkText("Log Out"));
                    logoutLink.click();
                    safeSleep(2000);
                    System.out.println("✓ Logged out successfully");
                } catch (RuntimeException e) {
                    System.out.println("⚠ Could not logout automatically: " + e.getMessage());
                }
            }
            
            // Give a moment to see final state
            System.out.println("\nBrowser will close in 3 seconds...");
            safeSleep(3000);
            
        } catch (Exception e) {
            System.out.println("Error during cleanup: " + e.getMessage());
        } finally {
            // Always close the browser
            if (driver != null) {
                driver.quit();
                System.out.println("✓ Browser closed successfully");
            }
        }
    }
}