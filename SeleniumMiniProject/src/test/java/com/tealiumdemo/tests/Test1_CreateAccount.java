package com.tealiumdemo.tests;

import java.util.List;
import java.util.UUID;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class Test1_CreateAccount {
    
    private WebDriver driver;
    private String email;
    private String password;
    
    @BeforeMethod
    public void setup() {
        System.out.println("========== DEBUG SETUP ==========");
        
        // Generate test data
        // Use a unique address (avoid multiple '@')
        email = "test@gail.com" + UUID.randomUUID().toString().substring(0, 8) + "@test.com";
        password = "1234567";
        
        System.out.println("Test email: " + email);
        
        // Setup ChromeDriver
        System.setProperty("webdriver.chrome.driver", "chromedriver.exe");
        
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        options.addArguments("--remote-allow-origins=*");
        
        driver = new ChromeDriver(options);
        
        // Go to website
        driver.get("https://ecommerce.tealiumdemo.com/");
        System.out.println("Page opened. Title: " + driver.getTitle());
        System.out.println("URL: " + driver.getCurrentUrl());
        
        // Let's see the page for a moment
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Sleep interrupted: " + e.getMessage());
        }
    }
    
    @Test
    public void debugCreateAccountTest() {
        System.out.println("\n========== DEBUG TEST STARTING ==========");
        
        try {
            // STEP 1: EXPLORE THE PAGE FIRST
            System.out.println("\nSTEP 1: Exploring page structure...");
            
            // Take a look at the page source
            String pageSource = driver.getPageSource();
            System.out.println("Page contains 'Account'? " + pageSource.contains("Account"));
            System.out.println("Page contains 'account'? " + pageSource.contains("account"));
            System.out.println("Page contains 'REGISTER'? " + pageSource.contains("REGISTER"));
            System.out.println("Page contains 'register'? " + pageSource.contains("register"));
            
            // Find ALL links on the page
            List<WebElement> allLinks = driver.findElements(By.tagName("a"));
            System.out.println("\nFound " + allLinks.size() + " total links on page.");
            
            System.out.println("\nFirst 20 links (with text):");
            int count = 0;
            for (WebElement link : allLinks) {
                try {
                    String linkText = link.getText().trim();
                    if (!linkText.isEmpty()) {
                        count++;
                        System.out.println("  " + count + ". '" + linkText + "'");
                        if (count >= 20) break;
                    }
                } catch (Exception e) {
                    // Skip if can't get text
                }
            }
            
            // PAUSE: Look at the browser window
            System.out.println("\n⏸️  Please look at the browser now.");
            System.out.println("Do you see an 'Account' link? What does it say exactly?");
            System.out.println("Press Enter in the console to continue...");
            try {
                System.in.read();
            } catch (java.io.IOException e) {
                System.out.println("Input read failed: " + e.getMessage());
            }
            
            // STEP 2: TRY TO FIND ACCOUNT LINK
            System.out.println("\nSTEP 2: Trying to find Account link...");
            
            // Try different selectors
            String[] accountSelectors = {
                "//a[text()='Account']",  // Exact match
                "//a[contains(text(), 'Account')]",  // Contains text
                "//a[contains(@class, 'account')]",  // Class contains account
                "//a[@href*='account']",  // Href contains account
                "//a[@title='Account']",  // Title is Account
                "//a[@data-target='#account']",  // Data attribute
                "//header//a",  // Any link in header
                "//nav//a",  // Any link in nav
                "//div[contains(@class, 'header')]//a",  // Link in header div
                "//a[.//span[text()='Account']]"  // Account text in child span
            };
            
            WebElement accountLink = null;
            for (int i = 0; i < accountSelectors.length; i++) {
                try {
                    accountLink = driver.findElement(By.xpath(accountSelectors[i]));
                    System.out.println("✅ FOUND Account with selector #" + (i+1) + ": " + accountSelectors[i]);
                    System.out.println("   Text: '" + accountLink.getText() + "'");
                    System.out.println("   Href: " + accountLink.getAttribute("href"));
                    break;
                } catch (Exception e) {
                    System.out.println("   ❌ Selector #" + (i+1) + " failed: " + accountSelectors[i]);
                }
            }
            
            if (accountLink == null) {
                System.out.println("\n❌ COULD NOT FIND ACCOUNT LINK WITH ANY SELECTOR!");
                System.out.println("Let me show you what the page HTML looks like...");
                
                // Show relevant HTML snippet
                String html = driver.getPageSource();
                if (html.contains("skip") || html.contains("header") || html.contains("nav")) {
                    int startIndex = html.indexOf("<header");
                    if (startIndex == -1) startIndex = html.indexOf("<nav");
                    if (startIndex == -1) startIndex = html.indexOf("skip-account");
                    
                    if (startIndex != -1) {
                        int endIndex = Math.min(startIndex + 1000, html.length());
                        System.out.println("\nHTML snippet (from position " + startIndex + "):");
                        System.out.println(html.substring(startIndex, endIndex));
                    }
                }
                
                // Ask user to click manually
                System.out.println("\n🔍 MANUAL HELP NEEDED:");
                System.out.println("1. Look at the browser window");
                System.out.println("2. What do you see where the Account link should be?");
                System.out.println("3. Is there a dropdown menu? A button? What's the exact text?");
                System.out.println("\nPress Enter after you've looked...");
                try {
                    System.in.read();
                } catch (java.io.IOException e) {
                    System.out.println("Input read failed: " + e.getMessage());
                }
                
                // Try one more thing - maybe it's not a link but a button or span
                System.out.println("\nChecking for non-link elements with 'Account' text...");
                List<WebElement> accountElements = driver.findElements(By.xpath("//*[contains(text(), 'Account')]"));
                System.out.println("Found " + accountElements.size() + " elements containing 'Account' text");
                
                for (WebElement elem : accountElements) {
                    try {
                        System.out.println("  Element: " + elem.getTagName() + " - Text: '" + elem.getText() + "'");
                    } catch (Exception e) {
                        // Skip
                    }
                }
                
                // Stop the test here
                System.out.println("\n🚫 STOPPING TEST - Cannot proceed without finding Account link");
                return;
            }
            
            // STEP 3: CLICK ACCOUNT
            System.out.println("\nSTEP 3: Clicking Account link...");
            accountLink.click();
            System.out.println("✅ Clicked Account");
            
            // Wait for dropdown
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Sleep interrupted: " + e.getMessage());
            }
            
            // STEP 4: LOOK FOR REGISTER
            System.out.println("\nSTEP 4: Looking for Register link...");
            
            // Try to find Register
            WebElement registerLink = null;
            try {
                registerLink = driver.findElement(By.linkText("Register"));
                System.out.println("✅ Found Register by exact link text");
            } catch (Exception e) {
                System.out.println("Register not found by exact text, trying alternatives...");
                
                // Try partial text
                try {
                    registerLink = driver.findElement(By.partialLinkText("Regist"));
                    System.out.println("✅ Found Register by partial text 'Regist'");
                } catch (Exception e2) {
                    // Try any element with Register text
                    List<WebElement> registerElements = driver.findElements(
                        By.xpath("//*[contains(text(), 'Register')]"));
                    System.out.println("Found " + registerElements.size() + " elements with 'Register' text");
                    
                    for (WebElement elem : registerElements) {
                        try {
                            if (elem.isDisplayed()) {
                                System.out.println("  Found visible element: " + elem.getTagName() + 
                                                  " - Text: '" + elem.getText() + "'");
                                registerLink = elem;
                                break;
                            }
                        } catch (Exception e3) {
                            // Skip
                        }
                    }
                }
            }
            
            if (registerLink == null) {
                System.out.println("\n❌ Could not find Register link!");
                System.out.println("Current page HTML snippet:");
                System.out.println(driver.getPageSource().substring(0, Math.min(500, driver.getPageSource().length())));
                
                System.out.println("\nPress Enter to try direct registration URL...");
                try {
                    System.in.read();
                } catch (java.io.IOException e) {
                    System.out.println("Input read failed: " + e.getMessage());
                }
                
                // Try going directly to registration page
                driver.get("https://ecommerce.tealiumdemo.com/customer/account/create/");
                System.out.println("Went directly to registration URL");
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.out.println("Sleep interrupted: " + e.getMessage());
                }
            } else {
                System.out.println("Clicking Register...");
                registerLink.click();
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.out.println("Sleep interrupted: " + e.getMessage());
                }
            }
            
            // STEP 5: CHECK IF WE'RE ON REGISTRATION PAGE
            System.out.println("\nSTEP 5: Checking if we're on registration page...");
            System.out.println("Current URL: " + driver.getCurrentUrl());
            System.out.println("Page title: " + driver.getTitle());
            
            // Look for registration form
            List<WebElement> formFields = driver.findElements(By.xpath("//input[@id='firstname' or @id='lastname' or @id='email_address']"));
            if (formFields.size() >= 3) {
                System.out.println("✅ On registration page - found form fields");
            } else {
                System.out.println("❌ Not on registration page - form fields not found");
                System.out.println("Let's try the other common registration URL...");
                driver.get("https://ecommerce.tealiumdemo.com/index.php/customer/account/create/");
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.out.println("Sleep interrupted: " + e.getMessage());
                }
            }
            
            // STEP 6: FILL FORM (if we're on the right page)
            System.out.println("\nSTEP 6: Filling registration form...");
            
            try {
                driver.findElement(By.id("firstname")).sendKeys("Test");
                driver.findElement(By.id("lastname")).sendKeys("User");
                driver.findElement(By.id("email_address")).sendKeys(email);
                driver.findElement(By.id("password")).sendKeys(password);
                driver.findElement(By.id("confirmation")).sendKeys(password);
                System.out.println("✅ Form filled successfully!");
            } catch (Exception e) {
                System.out.println("❌ Could not fill form: " + e.getMessage());
                System.out.println("Available input fields:");
                List<WebElement> inputs = driver.findElements(By.tagName("input"));
                for (WebElement input : inputs) {
                    try {
                        String id = input.getAttribute("id");
                        String name = input.getAttribute("name");
                        String type = input.getAttribute("type");
                        if (id != null || name != null) {
                            System.out.println("  Input - id: " + id + ", name: " + name + ", type: " + type);
                        }
                    } catch (Exception ex) {
                        // Skip
                    }
                }
            }
            
            // STEP 7: CLICK REGISTER BUTTON
            System.out.println("\nSTEP 7: Looking for Register button...");
            
            try {
                WebElement registerBtn = driver.findElement(By.xpath("//button[@title='Register']"));
                System.out.println("Found Register button with title attribute");
                registerBtn.click();
                System.out.println("✅ Clicked Register button");
            } catch (Exception e) {
                System.out.println("Button with title='Register' not found, trying other selectors...");
                
                // Try other button selectors
                String[] buttonSelectors = {
                    "//button[contains(text(), 'Register')]",
                    "//input[@type='submit' and @value='Register']",
                    "//button[@type='submit']",
                    "//*[contains(text(), 'Create Account')]",
                    "//input[@type='submit']"
                };
                
                boolean clicked = false;
                for (String selector : buttonSelectors) {
                    try {
                        WebElement btn = driver.findElement(By.xpath(selector));
                        System.out.println("Found button with selector: " + selector);
                        btn.click();
                        System.out.println("✅ Clicked button");
                        clicked = true;
                        break;
                    } catch (Exception ex) {
                        // Try next
                    }
                }
                
                if (!clicked) {
                    System.out.println("❌ Could not find Register button!");
                }
            }
            
            // Wait for result
            System.out.println("\nWaiting for registration result...");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Sleep interrupted: " + e.getMessage());
            }
            
            // STEP 8: CHECK RESULT
            System.out.println("\nSTEP 8: Checking registration result...");
            System.out.println("Current URL: " + driver.getCurrentUrl());
            System.out.println("Page title: " + driver.getTitle());
            
            String resultPageSource = driver.getPageSource().toLowerCase();
            if (resultPageSource.contains("thank you")) {
                System.out.println("✅ SUCCESS: 'Thank you' found in page!");
            } else if (resultPageSource.contains("my account")) {
                System.out.println("✅ SUCCESS: 'My Account' found in page!");
            } else if (driver.getCurrentUrl().contains("account")) {
                System.out.println("✅ SUCCESS: URL contains 'account'!");
            } else if (resultPageSource.contains("error")) {
                System.out.println("⚠ WARNING: 'Error' found in page - registration might have failed");
            } else {
                System.out.println("⚠ UNKNOWN: Can't determine if registration was successful");
            }
            
            System.out.println("\n========== DEBUG TEST COMPLETED ==========");
            System.out.println("Test email used: " + email);
            System.out.println("Test password: " + password);
            
        } catch (RuntimeException e) {
            System.out.println("\n❌ TEST FAILED WITH EXCEPTION:");
            System.out.println("Error: " + e.getMessage());
            System.out.println("Cause: " + e);
        }
    }
    
    @AfterMethod
    public void tearDown() throws Exception {
        System.out.println("\n========== FINAL CLEANUP ==========");
        System.out.println("Test execution complete.");
        System.out.println("\nPress ENTER in this console to close the browser...");
        
        try {
            // This will wait indefinitely until you press Enter
            System.in.read();
        } catch (java.io.IOException e) {
            System.out.println("Input error: " + e.getMessage() + ", waiting 10 seconds instead...");
            try {
                Thread.sleep(10000);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                System.out.println("Sleep interrupted: " + ie.getMessage());
            }
        }
        
        if (driver != null) {
            driver.quit();
            System.out.println("Browser closed.");
        }
        
        System.out.println("\nDebug session ended.");
    }
}