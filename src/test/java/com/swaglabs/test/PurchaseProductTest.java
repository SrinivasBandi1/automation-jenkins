package com.swaglabs.test;

import java.util.Arrays;
import java.util.List;

import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.swaglabs.pages.CheckOutConfirmationPage;
import com.swaglabs.pages.CheckOutInformationPage;
import com.swaglabs.pages.LoginPage;
import com.swaglabs.pages.ProductsPage;
import com.swaglabs.pages.YourCartPage;

import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;

public class PurchaseProductTest extends BaseTest {

	WebDriver driver = null;
	LoginPage loginPage = null;
	ProductsPage productsPage = null;
	YourCartPage yourCartPage = null;
	CheckOutInformationPage checkOutInformationPage = null;
	CheckOutConfirmationPage checkOutConfirmationPage = null;
	List<String> productsNamesInproductPage = null;

	@BeforeClass
	@Parameters({ "browser", "siteURL" })

	public void initSiteLogin(@Optional("chrome") String browser, @Optional("chrome") String siteURL) {
		this.driver = getWebDriver(WebDriversEnum.PURCHASE_PRODUCT_TEST, browser);
		this.loginPage = new LoginPage(driver);
		this.productsPage = new ProductsPage(driver);
		this.yourCartPage = new YourCartPage(driver);
		this.checkOutInformationPage = new CheckOutInformationPage(driver);
		this.checkOutConfirmationPage = new CheckOutConfirmationPage(driver);
		launchWebSite(driver);
		loginPage.login();

	}

	@Test( description = "Verify products are sorted from low to high")
	@Description("Test Description: Verify product prices are displayed in ascending order after applying sort")
	@Severity(SeverityLevel.NORMAL)
	@Story("Product sorting functionality")
	public void A_testProductsAreSortedLowToHigh() {
		productsPage.selectLowToHigh(testDataProp.getProperty(Constants.FILTER_OPTION_LOW_TO_HIGH));

		this.productsNamesInproductPage = productsPage.getFirstFourItemNames(4);

		Assert.assertTrue(productsPage.areFirstFourPricesSortedLowToHigh(4));
		// Thread.sleep(5000);

	}

	@Test( description = "Verify user can add first four products to the cart")
	@Description("Test Description: Ensure user can add first 4 sorted products to the cart successfully")
	@Severity(SeverityLevel.CRITICAL)
	@Story("Add products to cart")
	public void B_testAddFirstFourItemsToCart() {
		productsPage.clickFirstFourAddToCartButtons();
		productsPage.clickOnCartButton();

	}

	@Test( description = "Verify selected products are displayed in the cart")
	@Description("Test Description: Validate product names in the cart match selected items from products page")
	@Severity(SeverityLevel.CRITICAL)
	@Story("Cart validation")
	public void C_testVerifyCartItemsMatchSelectedProducts() {
		List<String> productsNamesInCartPage = yourCartPage.getFirstFourItemNames(4);

		Assert.assertEquals(yourCartPage.getHeaderText(), expectedAssertionsProp.getProperty(Constants.HEADER_CART_PAGE));
		Assert.assertEquals(productsNamesInproductPage, productsNamesInCartPage);

		Assert.assertTrue(yourCartPage.areFirstFourPricesSortedLowToHigh(4));
	}

	@Test( description = "Verify user can fill in checkout form")
	@Description("Test Description: Ensure user can enter First Name, Last Name, and Zip Code on checkout page")
	@Severity(SeverityLevel.NORMAL)
	@Story("Checkout form entry")
	public void D_testEnterCheckoutInformation() {
		yourCartPage.clickOnCheckOutButton();

		checkOutInformationPage.enterCheckoutDetails(Arrays.asList(testDataProp.getProperty(Constants.INPUT_FIRST_NAME),
				testDataProp.getProperty(Constants.INPUT_LAST_NAME),testDataProp.getProperty(Constants.INPUT_PINCODE)));
		checkOutInformationPage.clickOnContinueButton();
	}

	@Test( description = "Verify products and total price in checkout summary page")
	@Description("Test Description: Ensure checkout summary page shows selected products and correct total price")
	@Severity(SeverityLevel.CRITICAL)
	@Story("Checkout summary verification")
	public void E_testVerifyOrderSummaryAndPrices() {
		List<String> productsNamesInCheckoutPage = checkOutConfirmationPage.getFirstFourItemNames(4);

		Assert.assertEquals(productsNamesInproductPage, productsNamesInCheckoutPage);

		Double productPrice = checkOutConfirmationPage.getTotalCartPrice();

		Double totalPrice = checkOutConfirmationPage.getItemTotalAmount();

		Assert.assertEquals(productPrice, totalPrice);
	}

	@Test(priority = 7, description = "Verify user sees success message after placing order")
	@Description("Test Description: Ensure 'THANK YOU FOR YOUR ORDER' message is displayed after finishing checkout")
	@Severity(SeverityLevel.CRITICAL)
	@Story("Order completion")
	public void F_testOrderCompletionDisplaysSuccessMessage() {
		checkOutConfirmationPage.clickOnFinishButton();

//		Assert.assertEquals(checkOutConfirmationPage.getThankYouHeader(),expectedAssertionsProp.getProperty(Constants.HEADER_ORDER_CONFIRMATION) );
		Assert.assertEquals(checkOutConfirmationPage.getThankYouHeader(),"THANK YOU FOR YOUR ORDE" );

		
	}
	// @Test(priority = 1, description = "Verify user can create an order")
	@Description("Test Description:verify when tutor create a course, 6 modules reflect on the tutor screen")
	@Severity(SeverityLevel.NORMAL)
	@Story("verify when tutor create a course, 6 modules reflect on the tutor screen")
	public void createOrder() throws InterruptedException {

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

	@AfterClass
	public void quitDriver() {
		log.info("Starting of quitDriver method");

		try {
			Thread.sleep(2000);
			// this.loginPage.clickOnLogOut();
			if (this.driver != null) {
				super.quitDriver(driver, WebDriversEnum.PURCHASE_PRODUCT_TEST);
				log.debug("Driver quit successfully");
			}
		} catch (Exception ex) {
			log.error(ex.getMessage());
		}
		log.info("Ending of quitDriver method");
	}
}

