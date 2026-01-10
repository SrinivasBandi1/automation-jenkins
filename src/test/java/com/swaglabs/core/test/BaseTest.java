package com.swaglabs.core.test;

import java.lang.reflect.Method;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.b2b.utils.*;

import org.openqa.selenium.WebDriver;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.swaglabs.core.config.ConfigurationManager;
import com.swaglabs.core.driver.DriverManager;
import com.swaglabs.core.utils.ScreenshotUtils;

import io.qameta.allure.*;

/**
 * Enhanced base test class with enterprise-level features
 * Includes proper driver management, retry mechanisms, and comprehensive Allure integration
 */
@Listeners({TestListener.class})
public abstract class BaseTest {
    
    protected static final Logger logger = LogManager.getLogger(BaseTest.class);
    protected static final ConfigurationManager config = ConfigurationManager.getInstance();
    
    protected WebDriver driver;
    
    @BeforeSuite(alwaysRun = true)
    public void beforeSuite() {
        logger.info("=== Starting Test Suite ===");
        logger.info("Environment: {}", System.getProperty("env", "qa"));
        logger.info("Browser: {}", config.getBrowser());
        logger.info("Headless: {}", config.isHeadless());
        logger.info("Parallel Execution: {}", config.isParallelExecution());
        logger.info("Thread Count: {}", config.getThreadCount());
        
        // Ensure screenshot directory exists
        ScreenshotUtils.ensureScreenshotDirectoryExists();
    }
    
    @BeforeMethod(alwaysRun = true)
    public void beforeMethod(Method method) {
        logger.info("=== Starting Test Method: {} ===", method.getName());
        
        // Initialize driver if not already done
        if (driver == null) {
            driver = DriverManager.getDriver();
        }
        
        // Navigate to base URL
        navigateToBaseUrl();
        
        // Add test metadata to Allure
        addTestMetadata(method);
    }
    
    @AfterMethod(alwaysRun = true)
    public void afterMethod(ITestResult result, Method method) {
        logger.info("=== Finishing Test Method: {} ===", method.getName());
        
        // Handle test result
        handleTestResult(result);
        
        // Clean up driver if test failed or if it's the last test in the class
        if (result.getStatus() == ITestResult.FAILURE || result.getStatus() == ITestResult.SUCCESS_PERCENTAGE_FAILURE) {
            logger.warn("Test failed, cleaning up driver");
            cleanupDriver();
        }
    }
    
    @AfterClass(alwaysRun = true)
    public void afterClass() {
        logger.info("=== Finishing Test Class: {} ===", this.getClass().getSimpleName());
        cleanupDriver();
    }
    
    @AfterSuite(alwaysRun = true)
    public void afterSuite() {
        logger.info("=== Finishing Test Suite ===");
        
        // Clean up all drivers
        DriverManager.quitAllDrivers();
        
        // Clean up old screenshots (keep last 7 days)
        ScreenshotUtils.cleanupOldScreenshots(7);
        
        // Log final statistics
        logFinalStatistics();
    }
    
    /**
     * Navigate to base URL
     */
    @Step("Navigate to base URL")
    protected void navigateToBaseUrl() {
        try {
            String baseUrl = config.getBaseUrl();
            logger.info("Navigating to base URL: {}", baseUrl);
            driver.get(baseUrl);
            
            // Wait for page to load
            waitForPageLoad();
            
            logger.info("Successfully navigated to base URL");
        } catch (Exception e) {
            logger.error("Failed to navigate to base URL", e);
            throw new RuntimeException("Navigation to base URL failed", e);
        }
    }
    
    /**
     * Wait for page to load
     */
    @Step("Wait for page to load")
    protected void waitForPageLoad() {
        try {
            // Wait for document ready state
            org.openqa.selenium.support.ui.WebDriverWait wait = 
                new org.openqa.selenium.support.ui.WebDriverWait(driver, 
                    java.time.Duration.ofSeconds(config.getPageLoadTimeout()));
            
            wait.until(webDriver -> {
                String readyState = (String) ((org.openqa.selenium.JavascriptExecutor) webDriver)
                    .executeScript("return document.readyState");
                return "complete".equals(readyState);
            });
            
            logger.debug("Page loaded successfully");
        } catch (Exception e) {
            logger.warn("Page load wait failed: {}", e.getMessage());
        }
    }
    
    /**
     * Handle test result
     */
    private void handleTestResult(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        
        switch (result.getStatus()) {
            case ITestResult.SUCCESS:
                logger.info("Test PASSED: {}", testName);
				/*
				 * if (config.isEnableScreenshotOnSuccess()) { captureScreenshot("PASS_" +
				 * testName); } break;
				 */
            case ITestResult.FAILURE:
                logger.error("Test FAILED: {}", testName);
                captureScreenshot("FAIL_" + testName);
                attachErrorDetails(result);
                break;
                
            case ITestResult.SKIP:
                logger.warn("Test SKIPPED: {}", testName);
                break;
                
            case ITestResult.SUCCESS_PERCENTAGE_FAILURE:
                logger.warn("Test FAILED WITH SUCCESS PERCENTAGE: {}", testName);
                captureScreenshot("FAIL_PERCENTAGE_" + testName);
                attachErrorDetails(result);
                break;
                
            default:
                logger.warn("Test status unknown: {} for test: {}", result.getStatus(), testName);
                break;
        }
    }
    
    /**
     * Capture screenshot
     */
    @Step("Capture screenshot: {screenshotName}")
    protected void captureScreenshot(String screenshotName) {
        try {
            ScreenshotUtils.captureScreenshotToFile(driver, screenshotName);
            logger.debug("Screenshot captured: {}", screenshotName);
        } catch (Exception e) {
            logger.warn("Failed to capture screenshot: {}", screenshotName, e);
        }
    }
    
    /**
     * Attach error details to Allure report
     */
    private void attachErrorDetails(ITestResult result) {
        try {
            Throwable throwable = result.getThrowable();
            if (throwable != null) {
                String errorMessage = throwable.getMessage();
                String stackTrace = getStackTrace(throwable);
                
                // Attach error message
                Allure.addAttachment("Error Message", "text/plain", errorMessage);
                
                // Attach stack trace
                Allure.addAttachment("Stack Trace", "text/plain", stackTrace);
                
                // Attach test parameters
                Object[] parameters = result.getParameters();
                if (parameters != null && parameters.length > 0) {
                    StringBuilder paramInfo = new StringBuilder();
                    for (int i = 0; i < parameters.length; i++) {
                        paramInfo.append("Parameter ").append(i + 1).append(": ")
                                .append(parameters[i]).append("\n");
                    }
                    Allure.addAttachment("Test Parameters", "text/plain", paramInfo.toString());
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to attach error details", e);
        }
    }
    
    /**
     * Get formatted stack trace
     */
    private String getStackTrace(Throwable throwable) {
        StringBuilder stackTrace = new StringBuilder();
        stackTrace.append(throwable.toString()).append("\n");
        
        for (StackTraceElement element : throwable.getStackTrace()) {
            stackTrace.append("\tat ").append(element.toString()).append("\n");
        }
        
        return stackTrace.toString();
    }
    
    /**
     * Add test metadata to Allure
     */
    private void addTestMetadata(Method method) {
        try {
            // Add test description
            Test testAnnotation = method.getAnnotation(Test.class);
            if (testAnnotation != null && testAnnotation.description().length() > 0) {
                Allure.getLifecycle().updateTestCase(testResult -> 
                    testResult.setDescription(testAnnotation.description()));
            }
            
            // Add test parameters
            Allure.getLifecycle().updateTestCase(testResult -> {
                testResult.setFullName(method.getDeclaringClass().getSimpleName() + "." + method.getName());
                testResult.setName(method.getName());
            });
            
        } catch (Exception e) {
            logger.warn("Failed to add test metadata", e);
        }
    }
    
    /**
     * Clean up driver
     */
    private void cleanupDriver() {
        try {
            if (driver != null) {
                DriverManager.quitDriver();
                driver = null;
                logger.debug("Driver cleaned up successfully");
            }
        } catch (Exception e) {
            logger.warn("Failed to cleanup driver", e);
        }
    }
    
    /**
     * Log final statistics
     */
    private void logFinalStatistics() {
        try {
            int screenshotCount = ScreenshotUtils.getScreenshotCount();
            long screenshotSize = ScreenshotUtils.getScreenshotDirectorySize();
            int activeDriverCount = DriverManager.getActiveDriverCount();
            
            logger.info("=== Final Statistics ===");
            logger.info("Total screenshots captured: {}", screenshotCount);
            logger.info("Screenshot directory size: {} bytes", screenshotSize);
            logger.info("Active drivers remaining: {}", activeDriverCount);
            
        } catch (Exception e) {
            logger.warn("Failed to log final statistics", e);
        }
    }
    
    /**
     * Get WebDriver instance
     */
    protected WebDriver getDriver() {
        return driver;
    }
    
    /**
     * Get configuration manager
     */
    protected ConfigurationManager getConfig() {
        return config;
    }
    
    /**
     * Wait for specified seconds
     */
    @Step("Wait for {seconds} seconds")
    protected void waitForSeconds(int seconds) {
        try {
            Thread.sleep(seconds * 1000L);
            logger.debug("Waited for {} seconds", seconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Thread interrupted during wait");
        }
    }
    
    /**
     * Refresh current page
     */
    @Step("Refresh current page")
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
     * Navigate back
     */
    @Step("Navigate back")
    protected void navigateBack() {
        try {
            driver.navigate().back();
            waitForPageLoad();
            logger.info("Navigated back successfully");
        } catch (Exception e) {
            logger.error("Failed to navigate back", e);
            throw new RuntimeException("Navigation back failed", e);
        }
    }
    
    /**
     * Navigate forward
     */
    @Step("Navigate forward")
    protected void navigateForward() {
        try {
            driver.navigate().forward();
            waitForPageLoad();
            logger.info("Navigated forward successfully");
        } catch (Exception e) {
            logger.error("Failed to navigate forward", e);
            throw new RuntimeException("Navigation forward failed", e);
        }
    }
    
    /**
     * Get current page title
     */
    @Step("Get current page title")
    protected String getPageTitle() {
        String title = driver.getTitle();
        logger.debug("Current page title: {}", title);
        return title;
    }
    
    /**
     * Get current page URL
     */
    @Step("Get current page URL")
    protected String getCurrentUrl() {
        String url = driver.getCurrentUrl();
        logger.debug("Current page URL: {}", url);
        return url;
    }
} 