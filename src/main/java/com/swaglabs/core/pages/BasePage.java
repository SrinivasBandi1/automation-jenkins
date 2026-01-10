package com.swaglabs.core.pages;

import com.swaglabs.core.config.ConfigurationManager;
import com.swaglabs.core.utils.ScreenshotUtils;
import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * Enhanced base page class with enterprise-level features
 * Includes retry mechanisms, comprehensive error handling, and Allure integration
 */
public abstract class BasePage {
    
    protected final WebDriver driver;
    protected final WebDriverWait wait;
    protected final Actions actions;
    protected final Logger logger;
    protected final ConfigurationManager config;
    
    public BasePage(WebDriver driver) {
        this.driver = driver;
        this.config = ConfigurationManager.getInstance();
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(config.getExplicitWait()));
        this.actions = new Actions(driver);
        this.logger = LogManager.getLogger(this.getClass());
        
        PageFactory.initElements(driver, this);
        logger.info("Initialized page: {}", this.getClass().getSimpleName());
    }
    
    /**
     * Wait for element to be visible with retry mechanism
     */
    @Step("Wait for element to be visible: {element}")
    protected void waitForElementVisible(WebElement element) {
        waitForElementVisible(element, config.getExplicitWait());
    }
    
    @Step("Wait for element to be visible: {element} with timeout: {timeoutSeconds}")
    protected void waitForElementVisible(WebElement element, int timeoutSeconds) {
        try {
            WebDriverWait customWait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
            customWait.until(ExpectedConditions.visibilityOf(element));
            logger.debug("Element is visible: {}", element);
        } catch (TimeoutException e) {
            logger.error("Element not visible within {} seconds: {}", timeoutSeconds, element);
            captureScreenshot("ElementNotVisible_" + element.toString());
            throw new RuntimeException("Element not visible: " + element, e);
        }
    }
    
    /**
     * Wait for element to be clickable
     */
    @Step("Wait for element to be clickable: {element}")
    protected void waitForElementClickable(WebElement element) {
        try {
            wait.until(ExpectedConditions.elementToBeClickable(element));
            logger.debug("Element is clickable: {}", element);
        } catch (TimeoutException e) {
            logger.error("Element not clickable: {}", element);
            captureScreenshot("ElementNotClickable_" + element.toString());
            throw new RuntimeException("Element not clickable: " + element, e);
        }
    }
    
    /**
     * Click element with retry mechanism
     */
    @Step("Click element: {element}")
    protected void click(WebElement element) {
        click(element, 3); // Default retry count
    }
    
    @Step("Click element: {element} with retry count: {retryCount}")
    protected void click(WebElement element, int retryCount) {
        for (int attempt = 1; attempt <= retryCount; attempt++) {
            try {
                waitForElementClickable(element);
                element.click();
                logger.info("Successfully clicked element: {}", element);
                return;
            } catch (Exception e) {
                logger.warn("Click attempt {} failed for element: {}. Error: {}", attempt, element, e.getMessage());
                if (attempt == retryCount) {
                    captureScreenshot("ClickFailed_" + element.toString());
                    throw new RuntimeException("Failed to click element after " + retryCount + " attempts: " + element, e);
                }
                waitForSeconds(1);
            }
        }
    }
    
    /**
     * Enter text with retry mechanism
     */
    @Step("Enter text '{text}' into element: {element}")
    protected void enterText(WebElement element, String text) {
        enterText(element, text, 3);
    }
    
    @Step("Enter text '{text}' into element: {element} with retry count: {retryCount}")
    protected void enterText(WebElement element, String text, int retryCount) {
        for (int attempt = 1; attempt <= retryCount; attempt++) {
            try {
                waitForElementVisible(element);
                element.clear();
                element.sendKeys(text);
                logger.info("Successfully entered text '{}' into element: {}", text, element);
                return;
            } catch (Exception e) {
                logger.warn("Text entry attempt {} failed for element: {}. Error: {}", attempt, element, e.getMessage());
                if (attempt == retryCount) {
                    captureScreenshot("TextEntryFailed_" + element.toString());
                    throw new RuntimeException("Failed to enter text after " + retryCount + " attempts: " + element, e);
                }
                waitForSeconds(1);
            }
        }
    }
    
    /**
     * Get text from element
     */
    @Step("Get text from element: {element}")
    protected String getText(WebElement element) {
        try {
            waitForElementVisible(element);
            String text = element.getText();
            logger.debug("Retrieved text '{}' from element: {}", text, element);
            return text;
        } catch (Exception e) {
            logger.error("Failed to get text from element: {}", element, e);
            captureScreenshot("GetTextFailed_" + element.toString());
            throw new RuntimeException("Failed to get text from element: " + element, e);
        }
    }
    
    /**
     * Get attribute value from element
     */
    @Step("Get attribute '{attribute}' from element: {element}")
    protected String getAttribute(WebElement element, String attribute) {
        try {
            waitForElementVisible(element);
            String value = element.getAttribute(attribute);
            logger.debug("Retrieved attribute '{}' = '{}' from element: {}", attribute, value, element);
            return value;
        } catch (Exception e) {
            logger.error("Failed to get attribute '{}' from element: {}", attribute, element, e);
            throw new RuntimeException("Failed to get attribute from element: " + element, e);
        }
    }
    
    /**
     * Select option from dropdown
     */
    @Step("Select option '{value}' from dropdown: {element}")
    protected void selectByValue(WebElement element, String value) {
        try {
            waitForElementVisible(element);
            Select select = new Select(element);
            select.selectByValue(value);
            logger.info("Successfully selected option '{}' from dropdown: {}", value, element);
        } catch (Exception e) {
            logger.error("Failed to select option '{}' from dropdown: {}", value, element, e);
            captureScreenshot("SelectFailed_" + element.toString());
            throw new RuntimeException("Failed to select option from dropdown: " + element, e);
        }
    }
    
    @Step("Select option by visible text '{text}' from dropdown: {element}")
    protected void selectByVisibleText(WebElement element, String text) {
        try {
            waitForElementVisible(element);
            Select select = new Select(element);
            select.selectByVisibleText(text);
            logger.info("Successfully selected option '{}' from dropdown: {}", text, element);
        } catch (Exception e) {
            logger.error("Failed to select option '{}' from dropdown: {}", text, element, e);
            captureScreenshot("SelectFailed_" + element.toString());
            throw new RuntimeException("Failed to select option from dropdown: " + element, e);
        }
    }
    
    /**
     * Check if element is displayed
     */
    @Step("Check if element is displayed: {element}")
    protected boolean isElementDisplayed(WebElement element) {
        try {
            return element.isDisplayed();
        } catch (Exception e) {
            logger.debug("Element not displayed: {}", element);
            return false;
        }
    }
    
    /**
     * Check if element is enabled
     */
    @Step("Check if element is enabled: {element}")
    protected boolean isElementEnabled(WebElement element) {
        try {
            return element.isEnabled();
        } catch (Exception e) {
            logger.debug("Element not enabled: {}", element);
            return false;
        }
    }
    
    /**
     * Wait for page to load
     */
    @Step("Wait for page to load")
    protected void waitForPageLoad() {
        try {
            wait.until((Function<WebDriver, Boolean>) driver -> {
                String readyState = ((JavascriptExecutor) driver).executeScript("return document.readyState").toString();
                return "complete".equals(readyState);
            });
            logger.debug("Page loaded successfully");
        } catch (Exception e) {
            logger.warn("Page load wait failed: {}", e.getMessage());
        }
    }
    
    /**
     * Wait for custom condition
     */
    protected <T> T waitForCondition(ExpectedCondition<T> condition) {
        return wait.until(condition);
    }
    
    /**
     * Wait for custom condition with timeout
     */
    protected <T> T waitForCondition(ExpectedCondition<T> condition, int timeoutSeconds) {
        WebDriverWait customWait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
        return customWait.until(condition);
    }
    
    /**
     * Scroll to element
     */
    @Step("Scroll to element: {element}")
    protected void scrollToElement(WebElement element) {
        try {
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);
            waitForSeconds(1);
            logger.debug("Scrolled to element: {}", element);
        } catch (Exception e) {
            logger.warn("Failed to scroll to element: {}", element, e);
        }
    }
    
    /**
     * Execute JavaScript
     */
    @Step("Execute JavaScript: {script}")
    protected Object executeJavaScript(String script, Object... args) {
        try {
            Object result = ((JavascriptExecutor) driver).executeScript(script, args);
            logger.debug("Executed JavaScript: {} with result: {}", script, result);
            return result;
        } catch (Exception e) {
            logger.error("Failed to execute JavaScript: {}", script, e);
            throw new RuntimeException("JavaScript execution failed: " + script, e);
        }
    }
    
    /**
     * Get multiple element texts
     */
    @Step("Get first {count} element texts from: {elements}")
    protected List<String> getElementTexts(List<WebElement> elements, int count) {
        List<String> texts = new ArrayList<>();
        int maxCount = Math.min(count, elements.size());
        
        for (int i = 0; i < maxCount; i++) {
            try {
                String text = getText(elements.get(i));
                texts.add(text);
            } catch (Exception e) {
                logger.warn("Failed to get text from element at index {}: {}", i, e.getMessage());
                texts.add("");
            }
        }
        
        logger.debug("Retrieved {} texts from elements: {}", texts.size(), texts);
        return texts;
    }
    
    /**
     * Check if prices are sorted low to high
     */
    @Step("Check if first {count} prices are sorted low to high")
    protected boolean arePricesSortedLowToHigh(List<WebElement> priceElements, int count) {
        List<Double> prices = new ArrayList<>();
        
        for (int i = 0; i < count && i < priceElements.size(); i++) {
            try {
                String priceText = getText(priceElements.get(i)).replace("$", "").trim();
                prices.add(Double.parseDouble(priceText));
            } catch (NumberFormatException e) {
                logger.warn("Failed to parse price at index {}: {}", i, e.getMessage());
                return false;
            }
        }
        
        List<Double> sortedPrices = new ArrayList<>(prices);
        Collections.sort(sortedPrices);
        
        boolean isSorted = prices.equals(sortedPrices);
        logger.debug("Price sorting check result: {} for prices: {}", isSorted, prices);
        return isSorted;
    }
    
    /**
     * Wait for specified seconds
     */
    protected void waitForSeconds(int seconds) {
        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Thread interrupted during wait");
        }
    }
    
    /**
     * Capture screenshot and attach to Allure report
     */
    protected void captureScreenshot(String name) {
        if (!config.isScreenshotOnFailureEnabled()) {
            return;
        }

        try {
            byte[] screenshot = ScreenshotUtils.captureScreenshot(driver);

            Allure.getLifecycle().addAttachment(
                    name + "_" + System.currentTimeMillis(),
                    "image/png",
                    "png",
                    screenshot
            );

            logger.debug("Screenshot captured: {}", name);

        } catch (Exception e) {
            logger.warn("Failed to capture screenshot: {}", name, e);
        }
    }

    
    /**
     * Get current page title
     */
    @Step("Get page title")
    protected String getPageTitle() {
        String title = driver.getTitle();
        logger.debug("Page title: {}", title);
        return title;
    }
    
    /**
     * Get current page URL
     */
    @Step("Get page URL")
    protected String getPageUrl() {
        String url = driver.getCurrentUrl();
        logger.debug("Page URL: {}", url);
        return url;
    }
    
    /**
     * Navigate to URL
     */
    @Step("Navigate to URL: {url}")
    protected void navigateTo(String url) {
        try {
            driver.get(url);
            waitForPageLoad();
            logger.info("Successfully navigated to: {}", url);
        } catch (Exception e) {
            logger.error("Failed to navigate to: {}", url, e);
            throw new RuntimeException("Navigation failed: " + url, e);
        }
    }
    
    /**
     * Refresh page
     */
    @Step("Refresh page")
    protected void refreshPage() {
        try {
            driver.navigate().refresh();
            waitForPageLoad();
            logger.info("Page refreshed successfully");
        } catch (Exception e) {
            logger.error("Failed to refresh page", e);
            throw new RuntimeException("Page refresh failed", e);
        }
    }
    
    /**
     * Get WebDriver instance
     */
    protected WebDriver getDriver() {
        return driver;
    }
} 