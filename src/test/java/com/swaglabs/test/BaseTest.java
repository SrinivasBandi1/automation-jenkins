package com.swaglabs.test;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

public class BaseTest {
	protected static final Logger log = LogManager.getLogger(BaseTest.class);

	protected WebDriver driver;
	protected static Properties testDataProp = null;
	protected static Properties expectedAssertionsProp = null;
	private String siteURL = "https://www.saucedemo.com/v1/index.html";
	protected static String loginURL = null;
	private boolean isHeadLess = false;

	private String browser;
	private static Map<WebDriversEnum, WebDriver> webDriverPool = new Hashtable<WebDriversEnum, WebDriver>();

		@BeforeSuite(alwaysRun = true)
	@Parameters({"browser", "siteURL" })
	public void initAutomation(@Optional("chrome") String browser, @Optional("https://www.saucedemo.com/v1/index.html")String siteURL) {
		this.initTestAutomation(siteURL, browser);

		log.debug("Site URL :{} " + loginURL);
	}

	public void initTestAutomation(String siteURL, String browser) {

		if (siteURL != null) {
			loginURL = siteURL;
		}
		this.browser = browser;

		if (testDataProp == null) {
			FileReader testDataReader = null;
			FileReader assertionsReader = null;
			FileReader langxPathReader = null;

			try {
				System.out.println("+++++++++++++++++++++++++++++++++++++++" + Constants.TEST_DATA_FILE);

				String testDataFile = Constants.CONFIG_BASE_PATH + Constants.TEST_DATA_FILE;
				testDataReader = new FileReader(testDataFile);
				testDataProp = new Properties();
				testDataProp.load(testDataReader);

				String expectedAssertionFile = Constants.CONFIG_BASE_PATH + Constants.EXPECTED_ASSERTIONS_FILE;
				assertionsReader = new FileReader(expectedAssertionFile);
				expectedAssertionsProp = new Properties();
				expectedAssertionsProp.load(assertionsReader);
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (testDataReader != null) {
						testDataReader.close();
					}
					assertionsReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

	protected synchronized void quitDriver(WebDriver driver, WebDriversEnum driverName) {
		log.info("Starting of method quitDriver in BaseAutomationTest");

		try {
			if (driver != null) {
				driver.quit();
				log.debug(driverName + " Web driver quit successfully in BaseAutomationTest");
			}
		} catch (Exception ex) {
			log.warn("Exception during driver quit: " + ex.getMessage());
		} finally {
			driver = null;
			log.info("Ending of method quitDriver in BaseAutomationTest");
		}
	}

	protected WebDriver getWebDriver(WebDriversEnum webDriver, String browser) {
		log.info("Starting of method getWebDriver");

		if (driver != null) {
			return driver;
		}

		BrowserDriverFactory factory = null;
	    if (browser.equalsIgnoreCase("grid"))
			factory = new BrowserDriverFactory(browser, this.isHeadLess);
		else {
			factory = new BrowserDriverFactory(browser, this.isHeadLess);
		}
		driver = factory.createDriver(browser);

		driver.manage().window().maximize();
		driver.manage().deleteAllCookies();
		driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(60));
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(60));

		log.info("***************** Driver Successfully Created **************** {} ", driver.getTitle());

		log.info("End of method getWebDriver");
		return driver;

	}

	public void launchWebSite(WebDriver driver) {
		log.info("Starting of initSiteLogin method");

		driver.get(loginURL);

		log.info("Ending of initSiteLogin method");
	}

	@BeforeMethod
	public void logBeforeEachTestMethod(Method testMethod) {
		log.info("Enter into {}", testMethod.getName());
	}

	@AfterMethod
	public void logAfterEachTestMethod(Method testMethod) {
		log.info("Exit from {}", testMethod.getName());
	}

	public WebDriver getChildWebDriver() {
		return driver;
	}
}


