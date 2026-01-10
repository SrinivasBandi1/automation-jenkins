package com.swaglabs.test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariDriver;

import io.github.bonigarcia.wdm.WebDriverManager;

public class BrowserDriverFactory {

	private static final Logger logger = LogManager.getLogger(BrowserDriverFactory.class);

	private ThreadLocal<WebDriver> driver = new ThreadLocal<WebDriver>();
	private String browser = null;
	private Boolean isHeadLess = false;
	private String osName = System.getProperty("os.name");
	private String LINUX_OS = "Linux";
	private String WINDOWS_OS = "Linux";
	private String MAC_OS = "Mac OS X";
	private String remote_url = "http://34.93.230.101:4444/";

	public BrowserDriverFactory() {
		this.browser = "chrome";
		this.isHeadLess = false;
	}

	public BrowserDriverFactory(String browser) {
		logger.debug("Browser is " + browser);

		this.browser = browser.toLowerCase();
		this.isHeadLess = false;
	}

	public BrowserDriverFactory(String browser, Boolean isHeadLess) {
		this.browser = browser.toLowerCase();
		this.isHeadLess = isHeadLess;
	}

	public BrowserDriverFactory(String browser, Boolean isHeadLess, String remoteURL) {

		this.browser = browser.toLowerCase();
		this.isHeadLess = isHeadLess;
		this.remote_url = remoteURL;

	}

	public WebDriver createDriver(String browser) {

		switch (browser) {
		case "chrome":
			this.driver.set(getChromeDriver());
			break;

		case "firefox":
			this.driver.set(getFirefoxDriver());
			break;

		case "safari":
			this.driver.set(getSafariDriver());
			break;
		case "edge":
			this.driver.set(getEdgeDriver());
			break;
		case "IE":
			this.driver.set(getIEDriver());
			break;

		case "Chromium":
			this.driver.set(getChromiumDriver());
			break;

		case "grid":
			this.driver.set(getChromeRemoteDriver());
			break;

		default:
			logger.debug("No browser details mentioned, using Chrome driver");
			this.driver.set(getChromeDriver());
			break;

		}
		return this.driver.get();
	}
	private WebDriver getEdgeDriver() {
	    if (this.osName.contains("Linux")) {
	        return getEdgeLinuxDriver(); // You can implement separately if needed
	    } else {
	        return getEdgeWindowsDriver();
	    }
	}

	private WebDriver getIEDriver() {
		WebDriver driver = null;
		WebDriverManager.iedriver().setup();
		this.driver.set(new InternetExplorerDriver());
		InternetExplorerOptions options = new InternetExplorerOptions();

        // Set the desired capabilities
        options.ignoreZoomSettings();
        options.introduceFlakinessByIgnoringSecurityDomains();
        options.ignoreZoomSettings(); 
        options.introduceFlakinessByIgnoringSecurityDomains();
        // Instantiate the Internet Explorer driver
         driver = new InternetExplorerDriver(options);
		return driver;
	}

	private WebDriver getChromiumDriver() {
		WebDriverManager.chromiumdriver().setup();
		this.driver.set(new EdgeDriver());
		return this.driver.get();

	}

	private WebDriver getSafariDriver() {
		String browserDriverPath = "/usr/bin/safaridriver";

		if (browserDriverPath.contains("safaridriver")) {
			System.setProperty("webdriver.safari.driver", browserDriverPath);
			this.driver.set(new SafariDriver());
		}

		logger.debug("Safari driver path " + browserDriverPath);
		return this.driver.get();
	}

	private WebDriver getChromeDriver() {

		if (this.osName.contains("Linux")) {
			return getChromeLinuxDriver();
		} else {
			return getChromeWindowDriver();
		}
	}

	private WebDriver getFirefoxDriver() {

		if (this.osName.contains("Linux")) {
			return getFirefoxLinuxDriver();
		} else {
			return getFirefoxWindowsDriver();
		}
	}

	private WebDriver getFirefoxLinuxDriver() {

		WebDriver driver = null;
		WebDriverManager.firefoxdriver().setup();
		FirefoxOptions options = new FirefoxOptions();
	//	options.setHeadless(true);
		options.addArguments("--no-sandbox");
		driver = new FirefoxDriver(options);

		return driver;
	}

	private WebDriver getFirefoxWindowsDriver() {
		WebDriver driver = null;
		WebDriverManager.firefoxdriver().setup();
		driver = new FirefoxDriver();
		return driver;
	}

	private WebDriver getChromeWindowDriver() {
		WebDriver driver = null;
		WebDriverManager.chromedriver().setup();
		ChromeOptions options = new ChromeOptions();

		// ✅ Disable notifications and infobars
		options.addArguments("--disable-notifications");
		options.addArguments("--disable-infobars");
		options.addArguments("--disable-popup-blocking");
		options.addArguments("--start-maximized");

		// ✅ Disable Chrome's credential services completely
		Map<String, Object> prefs = new HashMap<>();
		prefs.put("credentials_enable_service", false);
		prefs.put("profile.password_manager_enabled", false);
		prefs.put("profile.default_content_setting_values.notifications", 2);
		options.setExperimentalOption("prefs", prefs);

		// ✅ Optional: Run in incognito to avoid stored credentials
		options.addArguments("--incognito");
		return driver = new ChromeDriver(options);
	}
	private WebDriver getEdgeWindowsDriver() {
	    WebDriver driver = null;

	    // ✅ Explicitly set path to msedgedriver.exe if WebDriverManager isn't preferred
	    System.out.println(System.getProperty("user.dir")+"/src/test/resources/drivers/msedgedriver.exe");
	    System.setProperty("webdriver.edge.driver", System.getProperty("user.dir")+"/src/test/resources/drivers/msedgedriver.exe");

	    // ✅ Optionally, you can comment the below line if not using WebDriverManager
	    // WebDriverManager.edgedriver().setup();

	    EdgeOptions options = new EdgeOptions();

	    // ✅ Disable popups, notifications, infobars
	    options.addArguments("--disable-notifications");
	    options.addArguments("--disable-infobars");
	    options.addArguments("--disable-popup-blocking");
	    options.addArguments("--start-maximized");

	    // ✅ Disable credential-related services
	    Map<String, Object> prefs = new HashMap<>();
	    prefs.put("credentials_enable_service", false);
	    prefs.put("profile.password_manager_enabled", false);
	    prefs.put("profile.default_content_setting_values.notifications", 2);
	    options.setExperimentalOption("prefs", prefs);

	    // ✅ Optional: InPrivate mode (Incognito)
	    options.addArguments("-inprivate");

	    driver = new EdgeDriver(options);
	    return driver;
	}

	private WebDriver getEdgeLinuxDriver() {
	    WebDriver driver = null;

	    // Setup Edge driver for Linux
	    WebDriverManager.edgedriver().setup();

	    EdgeOptions options = new EdgeOptions();

	    // ✅ Headless mode for CI/CD pipelines or Linux environments
	    options.addArguments("--headless=new"); // Use --headless=new for latest Chromium headless
	    options.addArguments("--disable-gpu");
	    options.addArguments("--window-size=1920,1080");
	    options.addArguments("--no-sandbox");
	    options.addArguments("--disable-dev-shm-usage");

	    // ✅ Privacy & performance tweaks
	    options.addArguments("--disable-notifications");
	    options.addArguments("--disable-popup-blocking");
	    options.addArguments("--disable-infobars");

	    // ✅ Disable credentials/prompt popups
	    Map<String, Object> prefs = new HashMap<>();
	    prefs.put("credentials_enable_service", false);
	    prefs.put("profile.password_manager_enabled", false);
	    prefs.put("profile.default_content_setting_values.notifications", 2);
	    options.setExperimentalOption("prefs", prefs);

	    driver = new EdgeDriver(options);
	    return driver;
	}

	private WebDriver getChromeLinuxDriver() {
		WebDriver driver = null;
		WebDriverManager.chromedriver().setup();

		ChromeOptions options = new ChromeOptions();

		options.addArguments("enable-automation");
		options.addArguments("--headless");
		options.addArguments("--no-sandbox");
		// options.addArguments("--disable-extensions");
		options.addArguments("--dns-prefetch-disable");
		options.addArguments("--disable-gpu");
		options.setPageLoadStrategy(PageLoadStrategy.NORMAL);

		//options.setHeadless(true);
		options.addArguments("--headless"); // Bypass OS security model, MUST BE THE VERY FIRST OPTION
		options.addArguments("--window-size=1920,1080");
		options.setPageLoadStrategy(PageLoadStrategy.EAGER);// del
		options.addArguments("--disable-browser-side-navigation"); // del
		options.addArguments("--disable-dev-shm-usage"); // del
		options.addArguments("--disable-gpu");
		options.addArguments("--no-sandbox");
		// options.addArguments("load-extension=extension_2_3_164.crx");
		// options.setBinary("/opt/google/chrome/google-chrome");
		Map<String, Object> prefs = new HashMap<String, Object>();
		prefs.put("profile.default_content_settings.popups", 0);
		options.setExperimentalOption("prefs", prefs);

		DesiredCapabilities capabilities = new DesiredCapabilities();
		capabilities.setBrowserName("CHROME");
		capabilities.setCapability(ChromeOptions.CAPABILITY, options);
	//	capabilities.setCapability(CapabilityType.SUPPORTS_NETWORK_CONNECTION, true);
		capabilities.setCapability("applicationCacheEnabled", "true");

		driver = new ChromeDriver(options);
		return driver;
	}

	private WebDriver getChromeRemoteDriver() {

		WebDriver driver = null;
		DesiredCapabilities capabilities = new DesiredCapabilities();
		capabilities.setBrowserName("chrome");
		capabilities.setCapability("headless", this.isHeadLess);

		try {
			driver = new RemoteWebDriver(new URL(this.remote_url), capabilities);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return driver;
	}
}