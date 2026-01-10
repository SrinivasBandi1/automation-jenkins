package com.swaglabs.core.listeners;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;

import com.swaglabs.core.config.ConfigurationManager;
import com.swaglabs.core.driver.DriverManager;

import io.qameta.allure.Allure;
import io.qameta.allure.Attachment;

/**
 * Enterprise-level TestNG Listener with Allure integration
 */
public class TestListener extends TestListenerAdapter {

    private static final Logger logger = LogManager.getLogger(TestListener.class);
    private static final ConfigurationManager config = ConfigurationManager.getInstance();
    private static final DateTimeFormatter TIMESTAMP =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final ConcurrentHashMap<String, TestExecutionData> executionData =
            new ConcurrentHashMap<>();

    @Override
    public void onTestStart(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        String className = result.getTestClass().getRealClass().getSimpleName();

        logger.info("🚀 STARTING TEST: {}.{}", className, testName);
        executionData.put(testName, new TestExecutionData(testName, className));

        addTestMetadata(result);
        addEnvironmentInfo();
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        long time = getExecutionTime(testName);

        logger.info("✅ PASSED: {} ({} ms)", testName, time);

		/*
		 * if (config.isEnableScreenshotOnSuccess()) { captureScreenshot(result); }
		 */

        addSuccessDetails(result, time);
        executionData.remove(testName);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        long time = getExecutionTime(testName);

        logger.error("❌ FAILED: {} ({} ms)", testName, time, result.getThrowable());

        captureScreenshot(result);
        addFailureDetails(result, time, result.getThrowable());
        attachPageSource(result);

        executionData.remove(testName);
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        logger.warn("⏭️ SKIPPED: {}", result.getMethod().getMethodName());
        addSkipDetails(result);
        executionData.remove(result.getMethod().getMethodName());
    }

    @Override
    public void onStart(ITestContext context) {
        logger.info("🎯 TEST SUITE STARTED: {}", context.getName());
        addSuiteInfo(context);
    }

    @Override
    public void onFinish(ITestContext context) {
        logger.info("🏁 TEST SUITE FINISHED: {}", context.getName());
        logExecutionSummary(context);
        addSuiteSummary(context);
    }

    /* ===================== Helpers ===================== */

    private long getExecutionTime(String testName) {
        TestExecutionData data = executionData.get(testName);
        return data == null ? 0 : data.getExecutionTime();
    }

    @Attachment(value = "Screenshot", type = "image/png")
    private byte[] captureScreenshot(ITestResult result) {
        try {
            WebDriver driver = getDriver(result);
            if (driver instanceof TakesScreenshot) {
                return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            }
        } catch (Exception e) {
            logger.warn("Screenshot capture failed", e);
        }
        return new byte[0];
    }

    @Attachment(value = "Page Source", type = "text/html")
    private String attachPageSource(ITestResult result) {
        try {
            WebDriver driver = getDriver(result);
            return driver != null ? driver.getPageSource() : "No Page Source";
        } catch (Exception e) {
            return "Page Source Not Available";
        }
    }

    private WebDriver getDriver(ITestResult result) {
        try {
            Object instance = result.getInstance();
            if (instance != null) {
                Field field = instance.getClass().getDeclaredField("driver");
                field.setAccessible(true);
                return (WebDriver) field.get(instance);
            }
        } catch (Exception ignored) {}
        return DriverManager.getDriver();
    }

    private void addTestMetadata(ITestResult result) {
        Allure.getLifecycle().updateTestCase(tc -> {
            tc.setName(result.getMethod().getMethodName());
            tc.setFullName(
                result.getTestClass().getRealClass().getSimpleName() + "." +
                result.getMethod().getMethodName()
            );
        });
    }

    private void addEnvironmentInfo() {
        String info = String.format(
                "Env: %s%nBrowser: %s%nHeadless: %s%nBaseURL: %s%nTime: %s",
                System.getProperty("env", "qa"),
                config.getBrowser(),
                config.isHeadless(),
                config.getBaseUrl(),
                LocalDateTime.now().format(TIMESTAMP)
        );
        Allure.addAttachment("Environment Info", info);
    }

    private void addSuccessDetails(ITestResult result, long time) {
        Allure.addAttachment("Success Details",
                "Test: " + result.getMethod().getMethodName() +
                "\nExecution Time: " + time + " ms");
    }

    private void addFailureDetails(ITestResult result, long time, Throwable t) {
        Allure.addAttachment("Failure Details",
                "Test: " + result.getMethod().getMethodName() +
                "\nExecution Time: " + time + " ms\nError: " +
                (t != null ? t.getMessage() : "Unknown"));
    }

    private void addSkipDetails(ITestResult result) {
        Allure.addAttachment("Skipped Test",
                "Test: " + result.getMethod().getMethodName());
    }

    private void addSuiteInfo(ITestContext context) {
        Allure.addAttachment("Suite Info",
                "Suite: " + context.getName() +
                "\nTotal Tests: " + context.getAllTestMethods().length);
    }

    private void addSuiteSummary(ITestContext context) {
        Allure.addAttachment("Suite Summary",
                "Passed: " + context.getPassedTests().size() +
                "\nFailed: " + context.getFailedTests().size() +
                "\nSkipped: " + context.getSkippedTests().size());
    }

    private void logExecutionSummary(ITestContext context) {
        logger.info("📊 SUMMARY → Passed: {}, Failed: {}, Skipped: {}",
                context.getPassedTests().size(),
                context.getFailedTests().size(),
                context.getSkippedTests().size());
    }

    /* ===================== Inner Class ===================== */

    private static class TestExecutionData {
        private final long startTime;

        TestExecutionData(String testName, String className) {
            this.startTime = System.currentTimeMillis();
        }

        long getExecutionTime() {
            return System.currentTimeMillis() - startTime;
        }
    }
}
