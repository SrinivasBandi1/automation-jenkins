package com.swaglabs.core.driver;

import com.swaglabs.core.config.ConfigurationManager;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.safari.SafariOptions;

import java.net.URL;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe WebDriver management system
 * Supports parallel execution and proper resource cleanup
 */
public class DriverManager {
    private static final Logger logger = LogManager.getLogger(DriverManager.class);
    private static final ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();
    private static final ConcurrentHashMap<Long, WebDriver> driverPool = new ConcurrentHashMap<>();
    
    private static final ConfigurationManager config = ConfigurationManager.getInstance();
    
    private DriverManager() {
        // Private constructor to prevent instantiation
    }
    
    /**
     * Get the WebDriver instance for the current thread
     */
    public static WebDriver getDriver() {
        WebDriver driver = driverThreadLocal.get();
        if (driver == null) {
            driver = createDriver();
            driverThreadLocal.set(driver);
            driverPool.put(Thread.currentThread().getId(), driver);
            logger.info("Created new WebDriver instance for thread: {}", Thread.currentThread().getId());
        }
        return driver;
    }
    
    /**
     * Create a new WebDriver instance based on configuration
     */
    private static WebDriver createDriver() {
        String browser = config.getBrowser().toLowerCase();
        boolean isHeadless = config.isHeadless();
        
        logger.info("Creating WebDriver for browser: {} (headless: {})", browser, isHeadless);
        
        try {
            switch (browser) {
                case "chrome":
                    return createChromeDriver(isHeadless);
                case "firefox":
                    return createFirefoxDriver(isHeadless);
                case "edge":
                    return createEdgeDriver(isHeadless);
                case "safari":
                    return createSafariDriver();
                case "remote":
                    return createRemoteDriver();
                default:
                    logger.warn("Unsupported browser: {}. Using Chrome as default.", browser);
                    return createChromeDriver(isHeadless);
            }
        } catch (Exception e) {
            logger.error("Failed to create WebDriver for browser: {}", browser, e);
            throw new RuntimeException("WebDriver creation failed", e);
        }
    }
    
    private static WebDriver createChromeDriver(boolean headless) {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        
        // Basic options
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");
        
        // Privacy and performance options
        options.addArguments("--disable-notifications");
        options.addArguments("--disable-infobars");
        options.addArguments("--disable-popup-blocking");
        options.addArguments("--disable-extensions");
        options.addArguments("--disable-plugins");
        
        // Headless mode
        if (headless) {
            options.addArguments("--headless=new");
        }
        
        // Disable credential services
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("credentials_enable_service", false);
        prefs.put("profile.password_manager_enabled", false);
        prefs.put("profile.default_content_setting_values.notifications", 2);
        options.setExperimentalOption("prefs", prefs);
        
        ChromeDriver driver = new ChromeDriver(options);
        configureDriver(driver);
        return driver;
    }
    
    private static WebDriver createFirefoxDriver(boolean headless) {
        WebDriverManager.firefoxdriver().setup();
        FirefoxOptions options = new FirefoxOptions();
        
        if (headless) {
            options.addArguments("--headless");
        }
        
        options.addArguments("--width=1920");
        options.addArguments("--height=1080");
        
        FirefoxDriver driver = new FirefoxDriver(options);
        configureDriver(driver);
        return driver;
    }
    
    private static WebDriver createEdgeDriver(boolean headless) {
        WebDriverManager.edgedriver().setup();
        EdgeOptions options = new EdgeOptions();
        
        // Basic options
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");
        
        // Privacy options
        options.addArguments("--disable-notifications");
        options.addArguments("--disable-infobars");
        options.addArguments("--disable-popup-blocking");
        
        if (headless) {
            options.addArguments("--headless=new");
        }
        
        // Disable credential services
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("credentials_enable_service", false);
        prefs.put("profile.password_manager_enabled", false);
        prefs.put("profile.default_content_setting_values.notifications", 2);
        options.setExperimentalOption("prefs", prefs);
        
        EdgeDriver driver = new EdgeDriver(options);
        configureDriver(driver);
        return driver;
    }
    
    private static WebDriver createSafariDriver() {
        SafariOptions options = new SafariOptions();
        SafariDriver driver = new SafariDriver(options);
        configureDriver(driver);
        return driver;
    }
    
    private static WebDriver createRemoteDriver() {
        try {
            // This would be used for Selenium Grid
            // Implementation depends on your grid setup
            logger.warn("Remote driver creation not implemented. Using Chrome as fallback.");
            return createChromeDriver(false);
        } catch (Exception e) {
            logger.error("Failed to create remote driver", e);
            throw new RuntimeException("Remote driver creation failed", e);
        }
    }
    
    private static void configureDriver(WebDriver driver) {
        driver.manage().window().maximize();
        driver.manage().deleteAllCookies();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(config.getImplicitWait()));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(config.getPageLoadTimeout()));
        driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(30));
    }
    
    /**
     * Quit the WebDriver for the current thread
     */
    public static void quitDriver() {
        WebDriver driver = driverThreadLocal.get();
        if (driver != null) {
            try {
                logger.info("Quitting WebDriver for thread: {}", Thread.currentThread().getId());
                driver.quit();
            } catch (Exception e) {
                logger.warn("Error while quitting WebDriver", e);
            } finally {
                driverThreadLocal.remove();
                driverPool.remove(Thread.currentThread().getId());
            }
        }
    }
    
    /**
     * Quit all WebDriver instances
     */
    public static void quitAllDrivers() {
        logger.info("Quitting all WebDriver instances");
        driverPool.values().forEach(driver -> {
            try {
                driver.quit();
            } catch (Exception e) {
                logger.warn("Error while quitting WebDriver", e);
            }
        });
        driverPool.clear();
        driverThreadLocal.remove();
    }
    
    /**
     * Get the number of active drivers
     */
    public static int getActiveDriverCount() {
        return driverPool.size();
    }
    
    /**
     * Check if current thread has an active driver
     */
    public static boolean hasActiveDriver() {
        return driverThreadLocal.get() != null;
    }
} 