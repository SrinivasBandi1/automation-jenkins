package com.swaglabs.pages;

import java.util.List;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import com.swaglabs.pages.base.BaseSwagLabsPage;

public class YourCartPage extends BaseSwagLabsPage {
	@FindBy(xpath = "//div[@class='inventory_item_name']")
	private List<WebElement> lblItemNames;

	@FindBy(xpath = "//div[@class='inventory_item_price']")
	private List<WebElement> lblItemPrices;

	@FindBy(xpath = "//*[@id='checkout']")
	private WebElement btnCheckOut;

	@FindBy(xpath = "//span[@class='title']")
	private WebElement lblHeader;

	public YourCartPage(WebDriver driver) {
		super(driver);
		log.info("======> Starting of YourCartPage Constructor <======");

		PageFactory.initElements(driver, this);

		log.info("======> Ending of YourCartPage Constructor <======");
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

	public void clickOnCheckOutButton() {
		log.info("Starting of clickOnCheckOutButton method");

		click(btnCheckOut);
		
		log.info("Ending of clickOnCheckOutButton method");

	}

	public String getHeaderText() {
		log.info("Starting of getHeaderText method");
		log.info("Ending of getHeaderText method");

		return getText(lblHeader);
	}
}
