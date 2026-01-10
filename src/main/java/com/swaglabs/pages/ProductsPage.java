package com.swaglabs.pages;

import java.util.List;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import com.swaglabs.pages.base.BaseSwagLabsPage;

public class ProductsPage extends BaseSwagLabsPage {

	@FindBy(xpath = "//select[@class='product_sort_container']")
	private WebElement lblExistingUsername;

	@FindBy(xpath = "//div[@class='inventory_item_name ']")
	private List<WebElement> lblItemNames;

	@FindBy(xpath = "//div[@class='inventory_item_price']")
	private List<WebElement> lblItemPrices;

	@FindBy(xpath = "//div[@class='pricebar']//button")
	private List<WebElement> btnAddToCarts;

	@FindBy(xpath = "//div[@id='shopping_cart_container']/a")
	private WebElement btnCart;

	public ProductsPage(WebDriver driver) {
		super(driver);

		log.info("======> Starting of ProductsPage Constructor <======");
		PageFactory.initElements(driver, this);
		log.info("======> Ending of ProductsPage Constructor <======");
	}

	public void selectLowToHigh(String dropdownValue) {
		log.info("Starting of selectLowToHigh method");

		selectByValue(lblExistingUsername, dropdownValue);

		log.info("Ending of selectLowToHigh method");
	}

	public List<String> getFirstFourItemNames(int numberOfProducts) {
		log.info("Starting of getFirstFourItemNames method");
		log.info("Ending of getFirstFourItemNames method");

		return getFirstNElementTexts(lblItemNames, numberOfProducts);
	}

	public boolean areFirstFourPricesSortedLowToHigh(int numberOfPrices) {
		log.info("Starting of areFirstFourPricesSortedLowToHigh method");
		log.info("Ending of areFirstFourPricesSortedLowToHigh method");

		return areFirstNPricesSortedLowToHigh(lblItemPrices, numberOfPrices);
	}

	public void clickFirstFourAddToCartButtons() {
		log.info("Starting of clickFirstFourAddToCartButtons method");

		for (int i = 0; i < 4 && i < btnAddToCarts.size(); i++) {
			btnAddToCarts.get(i).click();
		}

		log.info("Ending of clickFirstFourAddToCartButtons method");
	}

	public void clickOnCartButton() {
		log.info("Starting of clickOnCartButton method");

		click(btnCart);

		log.info("Ending of clickOnCartButton method");
	}

}
