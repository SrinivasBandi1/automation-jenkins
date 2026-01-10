package com.swaglabs.test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.Assert;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.swaglabs.pages.CheckOutConfirmationPage;
import com.swaglabs.pages.CheckOutInformationPage;
import com.swaglabs.pages.LoginPage;
import com.swaglabs.pages.ProductsPage;
import com.swaglabs.pages.YourCartPage;

import io.github.bonigarcia.wdm.WebDriverManager;
import io.qameta.allure.Description;
import io.qameta.allure.Owner;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;

@Owner("srinivas")
public class EndToEndPurchaseTest extends BaseTest {

	WebDriver driver = null;
	LoginPage loginPage = null;
	ProductsPage productsPage = null;
	YourCartPage yourCartPage = null;
	CheckOutInformationPage checkOutInformationPage = null;
	CheckOutConfirmationPage checkOutConfirmationPage = null;

	@Test(priority = 1, description = "Verify user can create an order")
	@Description("Test Description:verify when tutor create a course, 6 modules reflect on the tutor screen")
	@Severity(SeverityLevel.NORMAL)
	@Story("verify when tutor create a course, 6 modules reflect on the tutor screen")
	@Parameters("browser")
	public void createOrder(@Optional("chrome") String browser) throws InterruptedException {

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
		driver = new ChromeDriver(options);
		driver.manage().window().maximize();
		this.loginPage = new LoginPage(driver);
		this.productsPage = new ProductsPage(driver);
		this.yourCartPage = new YourCartPage(driver);
		this.checkOutInformationPage = new CheckOutInformationPage(driver);
		this.checkOutConfirmationPage = new CheckOutConfirmationPage(driver);
		driver.get("https://www.saucedemo.com/v1/index.html");
		loginPage.login();
		// driver.switchTo().alert().accept();
		productsPage.selectLowToHigh("lohi");

		List<String> productsNamesInproductPage = productsPage.getFirstFourItemNames(4);

		Assert.assertTrue(productsPage.areFirstFourPricesSortedLowToHigh(4));
		// Thread.sleep(5000);
		productsPage.clickFirstFourAddToCartButtons();

		productsPage.clickOnCartButton();

		List<String> productsNamesInCartPage = yourCartPage.getFirstFourItemNames(4);

		Assert.assertEquals(yourCartPage.getHeaderText(), "Your Cart");

		Assert.assertEquals(productsNamesInproductPage, productsNamesInCartPage);

		Assert.assertTrue(yourCartPage.areFirstFourPricesSortedLowToHigh(4));

		yourCartPage.clickOnCheckOutButton();

		checkOutInformationPage.enterCheckoutDetails(Arrays.asList("srinivas", "bandi", "505209"));
		checkOutInformationPage.clickOnContinueButton();

		List<String> productsNamesInCheckoutPage = checkOutConfirmationPage.getFirstFourItemNames(4);

		Assert.assertEquals(productsNamesInproductPage, productsNamesInCheckoutPage);

		Double productPrice = checkOutConfirmationPage.getTotalCartPrice();

		Double totalPrice = checkOutConfirmationPage.getItemTotalAmount();

		Assert.assertEquals(productPrice, totalPrice);
		checkOutConfirmationPage.clickOnFinishButton();

		Assert.assertEquals(checkOutConfirmationPage.getThankYouHeader(), "THANK YOU FOR YOUR ORDER");

	}

}
