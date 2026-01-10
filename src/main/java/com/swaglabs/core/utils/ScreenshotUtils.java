package com.swaglabs.core.utils;

import com.swaglabs.core.config.ConfigurationManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for capturing and managing screenshots
 * Provides centralized screenshot functionality with proper error handling
 */
public class ScreenshotUtils {
    
    private static final Logger logger = LogManager.getLogger(ScreenshotUtils.class);
    private static final ConfigurationManager config = ConfigurationManager.getInstance();
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    
    private ScreenshotUtils() {
        // Private constructor to prevent instantiation
    }
    
    /**
     * Capture screenshot as byte array
     */
    public static byte[] captureScreenshot(WebDriver driver) {
        try {
            if (driver instanceof TakesScreenshot) {
                byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                logger.debug("Screenshot captured successfully");
                return screenshot;
            } else {
                logger.warn("WebDriver does not support screenshot capture");
                return new byte[0];
            }
        } catch (Exception e) {
            logger.error("Failed to capture screenshot", e);
            return new byte[0];
        }
    }
    
    /**
     * Capture screenshot and save to file
     */
    public static String captureScreenshotToFile(WebDriver driver, String testName) {
        try {
            if (driver instanceof TakesScreenshot) {
                String fileName = generateScreenshotFileName(testName);
                Path screenshotPath = getScreenshotDirectory().resolve(fileName);
                
                // Ensure directory exists
                Files.createDirectories(screenshotPath.getParent());
                
                // Capture and save screenshot
                File screenshotFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
                Files.copy(screenshotFile.toPath(), screenshotPath);
                
                logger.info("Screenshot saved to: {}", screenshotPath);
                return screenshotPath.toString();
            } else {
                logger.warn("WebDriver does not support screenshot capture");
                return null;
            }
        } catch (IOException e) {
            logger.error("Failed to save screenshot to file", e);
            return null;
        }
    }
    
    /**
     * Capture screenshot with custom name
     */
    public static String captureScreenshotToFile(WebDriver driver, String testName, String customName) {
        try {
            if (driver instanceof TakesScreenshot) {
                String fileName = generateScreenshotFileName(testName, customName);
                Path screenshotPath = getScreenshotDirectory().resolve(fileName);
                
                // Ensure directory exists
                Files.createDirectories(screenshotPath.getParent());
                
                // Capture and save screenshot
                File screenshotFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
                Files.copy(screenshotFile.toPath(), screenshotPath);
                
                logger.info("Screenshot saved to: {}", screenshotPath);
                return screenshotPath.toString();
            } else {
                logger.warn("WebDriver does not support screenshot capture");
                return null;
            }
        } catch (IOException e) {
            logger.error("Failed to save screenshot to file", e);
            return null;
        }
    }
    
    /**
     * Generate screenshot file name with timestamp
     */
    private static String generateScreenshotFileName(String testName) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        return String.format("%s_%s.png", testName, timestamp);
    }
    
    /**
     * Generate screenshot file name with custom name and timestamp
     */
    private static String generateScreenshotFileName(String testName, String customName) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        return String.format("%s_%s_%s.png", testName, customName, timestamp);
    }
    
    /**
     * Get screenshot directory path
     */
    private static Path getScreenshotDirectory() {
        String screenshotPath = config.getScreenshotPath();
        if (screenshotPath == null || screenshotPath.trim().isEmpty()) {
            screenshotPath = "screenshots";
        }
        
        // Create directory if it doesn't exist
        Path directory = Paths.get(screenshotPath);
        try {
            if (!Files.exists(directory)) {
                Files.createDirectories(directory);
                logger.debug("Created screenshot directory: {}", directory);
            }
        } catch (IOException e) {
            logger.error("Failed to create screenshot directory: {}", directory, e);
        }
        
        return directory;
    }
    
    /**
     * Clean up old screenshots (older than specified days)
     */
    public static void cleanupOldScreenshots(int daysToKeep) {
        try {
            Path screenshotDir = getScreenshotDirectory();
            if (!Files.exists(screenshotDir)) {
                return;
            }

            long cutoffTime = System.currentTimeMillis()
                    - (daysToKeep * 24L * 60 * 60 * 1000);

            AtomicInteger deletedCount = new AtomicInteger(0);

            Files.walk(screenshotDir)
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".png"))
                    .filter(path -> {
                        try {
                            return Files.getLastModifiedTime(path).toMillis() < cutoffTime;
                        } catch (IOException e) {
                            logger.warn("Failed to read file time: {}", path, e);
                            return false;
                        }
                    })
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                            deletedCount.incrementAndGet();
                            logger.debug("Deleted old screenshot: {}", path);
                        } catch (IOException e) {
                            logger.warn("Failed to delete screenshot: {}", path, e);
                        }
                    });

            logger.info("🧹 Cleaned up {} old screenshots", deletedCount.get());

        } catch (IOException e) {
            logger.error("Failed to cleanup old screenshots", e);
        }
    }

    
    /**
     * Get screenshot directory size in bytes
     */
    public static long getScreenshotDirectorySize() {
        try {
            Path screenshotDir = getScreenshotDirectory();
            if (!Files.exists(screenshotDir)) {
                return 0;
            }
            
            return Files.walk(screenshotDir)
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".png"))
                    .mapToLong(path -> {
                        try {
                            return Files.size(path);
                        } catch (IOException e) {
                            logger.warn("Failed to get file size: {}", path, e);
                            return 0;
                        }
                    })
                    .sum();
            
        } catch (IOException e) {
            logger.error("Failed to calculate screenshot directory size", e);
            return 0;
        }
    }
    
    /**
     * Get screenshot count in directory
     */
    public static int getScreenshotCount() {
        try {
            Path screenshotDir = getScreenshotDirectory();
            if (!Files.exists(screenshotDir)) {
                return 0;
            }
            
            return (int) Files.walk(screenshotDir)
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".png"))
                    .count();
            
        } catch (IOException e) {
            logger.error("Failed to count screenshots", e);
            return 0;
        }
    }
    
    /**
     * Create screenshot directory if it doesn't exist
     */
    public static void ensureScreenshotDirectoryExists() {
        try {
            Path screenshotDir = getScreenshotDirectory();
            if (!Files.exists(screenshotDir)) {
                Files.createDirectories(screenshotDir);
                logger.info("Created screenshot directory: {}", screenshotDir);
            }
        } catch (IOException e) {
            logger.error("Failed to create screenshot directory", e);
        }
    }
} 