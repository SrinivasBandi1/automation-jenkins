package com.b2b.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;

import com.swaglabs.test.BaseTest;

import io.qameta.allure.Allure;
import io.qameta.allure.Attachment;

public class TestListener extends TestListenerAdapter {
	private static final Logger logger = LogManager.getLogger(TestListener.class);

	private static String getTestMethodName(ITestResult iTestResult) {
		return iTestResult.getMethod().getConstructorOrMethod().getName();
	}

	@Attachment(value = "{0}", type = "text/plain")
	public static String saveTextLog(String message) {
		return message;
	}

	@Attachment(value = "{0}", type = "text/html")
	public static String attachHtml(String html) {
		return html;
	}

	// ✅ Allure attachment method (STATIC)
	@Attachment(value = "{0}", type = "image/png")
	public static byte[] captureScreenshot(String name, WebDriver driver) {
		return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
	}

	@Override
	public void onStart(ITestContext iTestContext) {
		logger.info("In onStart method " + iTestContext.getName());
	}
	public void saveScreenshotPNG(String screenshotType, WebDriver driver) {

		byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
		Allure.getLifecycle().addAttachment(
				screenshotType + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MMM-yy_hh:mm:ss")),
				"image/png", "png", screenshot);
	}
	@Override
	public void onTestStart(ITestResult iTestResult) {
		logger.info(getTestMethodName(iTestResult) + " test is starting.");
	}

	@Override
	public void onTestSuccess(ITestResult iTestResult) {
		logger.info(getTestMethodName(iTestResult) + " test succeeded.");

		Object testClass = iTestResult.getInstance();
		WebDriver driver = ((BaseTest) testClass).getChildWebDriver();

		if (driver != null) {
			saveScreenshotPNG("Screenshot on Test Success - " + getTestMethodName(iTestResult), driver);
		}
	}

	@Override
	public void onTestFailure(ITestResult iTestResult) {
		logger.info(getTestMethodName(iTestResult) + " test failed.");

		Object testClass = iTestResult.getInstance();
		WebDriver driver = ((BaseTest) testClass).getChildWebDriver();

		if (driver != null) {
			logger.info("Capturing screenshot for failed test case: " + getTestMethodName(iTestResult));
			captureScreenshot("Screenshot on Test Failure - " + getTestMethodName(iTestResult), driver);
		}
	}

	@Override
	public void onTestSkipped(ITestResult iTestResult) {
		logger.info(getTestMethodName(iTestResult) + " test is skipped.");
	}

	@Override
	public void onTestFailedButWithinSuccessPercentage(ITestResult iTestResult) {
		logger.info("Test failed but it is in defined success ratio: " + getTestMethodName(iTestResult));
	}

	@Override
	public void onTestFailedWithTimeout(ITestResult iTestResult) {
		logger.info(getTestMethodName(iTestResult) + " test failed with Timeout.");
		onTestFailure(iTestResult);
	}
}
