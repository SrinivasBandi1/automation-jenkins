package com.swaglabs.core.config;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Environment configuration model
 * Represents environment-specific settings for the automation framework
 */
public class EnvironmentConfig {
    
    @JsonProperty("baseUrl")
    private String baseUrl = "https://www.saucedemo.com/v1/index.html";
    
    @JsonProperty("browser")
    private String browser = "chrome";
    
    @JsonProperty("headless")
    private boolean headless = false;
    
    @JsonProperty("implicitWait")
    private int implicitWait = 10;
    
    @JsonProperty("explicitWait")
    private int explicitWait = 15;
    
    @JsonProperty("pageLoadTimeout")
    private int pageLoadTimeout = 30;
    
    @JsonProperty("screenshotPath")
    private String screenshotPath = "screenshots";
    
    @JsonProperty("parallelExecution")
    private boolean parallelExecution = false;
    
    @JsonProperty("threadCount")
    private int threadCount = 4;
    
    @JsonProperty("retryCount")
    private int retryCount = 2;
    
    @JsonProperty("gridUrl")
    private String gridUrl = "http://localhost:4444/wd/hub";
    
    @JsonProperty("enableVideoRecording")
    private boolean enableVideoRecording = false;
    
    @JsonProperty("enableScreenshotOnFailure")
    private boolean enableScreenshotOnFailure = true;
    
    @JsonProperty("enableScreenshotOnSuccess")
    private boolean enableScreenshotOnSuccess = false;
    
    // Default constructor
    public EnvironmentConfig() {}
    
    // Getters and Setters
    public String getBaseUrl() {
        return baseUrl;
    }
    
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
    
    public String getBrowser() {
        return browser;
    }
    
    public void setBrowser(String browser) {
        this.browser = browser;
    }
    
    public boolean isHeadless() {
        return headless;
    }
    
    public void setHeadless(boolean headless) {
        this.headless = headless;
    }
    
    public int getImplicitWait() {
        return implicitWait;
    }
    
    public void setImplicitWait(int implicitWait) {
        this.implicitWait = implicitWait;
    }
    
    public int getExplicitWait() {
        return explicitWait;
    }
    
    public void setExplicitWait(int explicitWait) {
        this.explicitWait = explicitWait;
    }
    
    public int getPageLoadTimeout() {
        return pageLoadTimeout;
    }
    
    public void setPageLoadTimeout(int pageLoadTimeout) {
        this.pageLoadTimeout = pageLoadTimeout;
    }
    
    public String getScreenshotPath() {
        return screenshotPath;
    }
    
    public void setScreenshotPath(String screenshotPath) {
        this.screenshotPath = screenshotPath;
    }
    
    public boolean isParallelExecution() {
        return parallelExecution;
    }
    
    public void setParallelExecution(boolean parallelExecution) {
        this.parallelExecution = parallelExecution;
    }
    
    public int getThreadCount() {
        return threadCount;
    }
    
    public void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }
    
    public int getRetryCount() {
        return retryCount;
    }
    
    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }
    
    public String getGridUrl() {
        return gridUrl;
    }
    
    public void setGridUrl(String gridUrl) {
        this.gridUrl = gridUrl;
    }
    
    public boolean isEnableVideoRecording() {
        return enableVideoRecording;
    }
    
    public void setEnableVideoRecording(boolean enableVideoRecording) {
        this.enableVideoRecording = enableVideoRecording;
    }
    
    public boolean isEnableScreenshotOnFailure() {
        return enableScreenshotOnFailure;
    }
    
    public void setEnableScreenshotOnFailure(boolean enableScreenshotOnFailure) {
        this.enableScreenshotOnFailure = enableScreenshotOnFailure;
    }
    
    public boolean isEnableScreenshotOnSuccess() {
        return enableScreenshotOnSuccess;
    }
    
    public void setEnableScreenshotOnSuccess(boolean enableScreenshotOnSuccess) {
        this.enableScreenshotOnSuccess = enableScreenshotOnSuccess;
    }
    
    @Override
    public String toString() {
        return "EnvironmentConfig{" +
                "baseUrl='" + baseUrl + '\'' +
                ", browser='" + browser + '\'' +
                ", headless=" + headless +
                ", implicitWait=" + implicitWait +
                ", explicitWait=" + explicitWait +
                ", pageLoadTimeout=" + pageLoadTimeout +
                ", screenshotPath='" + screenshotPath + '\'' +
                ", parallelExecution=" + parallelExecution +
                ", threadCount=" + threadCount +
                ", retryCount=" + retryCount +
                ", gridUrl='" + gridUrl + '\'' +
                ", enableVideoRecording=" + enableVideoRecording +
                ", enableScreenshotOnFailure=" + enableScreenshotOnFailure +
                ", enableScreenshotOnSuccess=" + enableScreenshotOnSuccess +
                '}';
    }
} 