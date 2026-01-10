package com.swaglabs.core.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Enterprise-level configuration management system
 * Supports multiple environments and provides type-safe configuration access
 */
public class ConfigurationManager {
    private static final Logger logger = LogManager.getLogger(ConfigurationManager.class);
    private static ConfigurationManager instance;
    private Properties properties;
    private EnvironmentConfig environmentConfig;
    
    private ConfigurationManager() {
        loadConfiguration();
    }
    
    public static synchronized ConfigurationManager getInstance() {
        if (instance == null) {
            instance = new ConfigurationManager();
        }
        return instance;
    }
    
    private void loadConfiguration() {
        try {
            String environment = System.getProperty("env", "qa");
            logger.info("Loading configuration for environment: {}", environment);
            
            // Load environment-specific configuration
            loadEnvironmentConfig(environment);
            
            // Load properties file
            loadProperties();
            
        } catch (Exception e) {
            logger.error("Failed to load configuration", e);
            throw new RuntimeException("Configuration loading failed", e);
        }
    }
    
    private void loadEnvironmentConfig(String environment) {
        try {
            String configFile = String.format("config/environment-%s.yml", environment);
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(configFile);
            
            if (inputStream == null) {
                logger.warn("Environment config file not found: {}. Using default configuration.", configFile);
                environmentConfig = new EnvironmentConfig();
                return;
            }
            
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            environmentConfig = mapper.readValue(inputStream, EnvironmentConfig.class);
            logger.info("Environment configuration loaded successfully for: {}", environment);
            
        } catch (IOException e) {
            logger.error("Failed to load environment configuration", e);
            environmentConfig = new EnvironmentConfig();
        }
    }
    
    private void loadProperties() {
        properties = new Properties();
        try {
            String propertiesFile = "config/testdata.properties";
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propertiesFile);
            
            if (inputStream != null) {
                properties.load(inputStream);
                logger.info("Properties file loaded successfully");
            } else {
                logger.warn("Properties file not found: {}", propertiesFile);
            }
        } catch (IOException e) {
            logger.error("Failed to load properties file", e);
        }
    }
    
    // Environment-specific getters
    public String getBaseUrl() {
        return environmentConfig.getBaseUrl();
    }
    
    public String getBrowser() {
        return System.getProperty("browser", environmentConfig.getBrowser());
    }
    
    public boolean isHeadless() {
        return Boolean.parseBoolean(System.getProperty("headless", 
            String.valueOf(environmentConfig.isHeadless())));
    }
    
    public int getImplicitWait() {
        return environmentConfig.getImplicitWait();
    }
    
    public int getExplicitWait() {
        return environmentConfig.getExplicitWait();
    }
    
    public int getPageLoadTimeout() {
        return environmentConfig.getPageLoadTimeout();
    }
    
    public String getScreenshotPath() {
        return environmentConfig.getScreenshotPath();
    }
    
    public boolean isParallelExecution() {
        return Boolean.parseBoolean(System.getProperty("parallel", 
            String.valueOf(environmentConfig.isParallelExecution())));
    }
    
    public int getThreadCount() {
        return Integer.parseInt(System.getProperty("threadCount", 
            String.valueOf(environmentConfig.getThreadCount())));
    }
    
    // Properties getters
    public String getProperty(String key) {
        return properties.getProperty(key);
    }
    
    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
    
    public int getIntProperty(String key, int defaultValue) {
        try {
            return Integer.parseInt(properties.getProperty(key));
        } catch (NumberFormatException e) {
            logger.warn("Invalid integer property: {}. Using default: {}", key, defaultValue);
            return defaultValue;
        }
    }
    
    public boolean getBooleanProperty(String key, boolean defaultValue) {
        return Boolean.parseBoolean(properties.getProperty(key, String.valueOf(defaultValue)));
    }
    
    // Test data specific getters
    public String getFilterOptionLowToHigh() {
        return getProperty("filter.option.low.to.high", "lohi");
    }
    
    public String getFirstName() {
        return getProperty("input.first.name", "Test");
    }
    
    public String getLastName() {
        return getProperty("input.last.name", "User");
    }
    
    public String getPincode() {
        return getProperty("input.pincode", "12345");
    }
    
    public String getExpectedCartHeader() {
        return getProperty("header.cart.page", "Your Cart");
    }
    
    public String getExpectedOrderConfirmation() {
        return getProperty("header.order.confirmation", "THANK YOU FOR YOUR ORDER");
    }
    public boolean isScreenshotOnFailureEnabled() {
        return Boolean.parseBoolean(
                properties.getProperty("screenshot.on.failure", "true")
        );
    }

} 