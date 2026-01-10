package com.swaglabs.pages;

import java.util.List;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import java.util.logging.Logger;

import com.swaglabs.pages.base.BaseSwagLabsPage;

public class CheckOutConfirmationPage extends BaseSwagLabsPage {

	@FindBy(xpath = "//div[@class='inventory_item_name']")
	private List<WebElement> lblItemNames;

	@FindBy(xpath = "//div[@class='inventory_item_price']")
	private List<WebElement> lblItemPrices;

	@FindBy(className = "summary_subtotal_label")
	private WebElement itemTotalElement;

	@FindBy(xpath = "//*[@id='finish']")
	private WebElement btnFinish;

	@FindBy(xpath = "//h2[@class='complete-header']")
	private WebElement lblThankYouHeader;

	public CheckOutConfirmationPage(WebDriver driver) {
		super(driver);
	    log.info("======> Starting of CheckOutConfirmationPage Constructor <======");

		PageFactory.initElements(driver, this);
		
	    log.info("======> Ending of CheckOutConfirmationPage Constructor <======");
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

	public double getTotalCartPrice() {
		log.info("Starting of getTotalCartPrice method");
		
		double total = 0.0;

		for (WebElement priceElement : lblItemPrices) {
			String priceText = priceElement.getText().replace("$", "").trim();
			total += Double.parseDouble(priceText);
		}
		
		log.info("Ending of getTotalCartPrice method");
		return total;
	}

	public double getItemTotalAmount() {
		log.info("Starting of getItemTotalAmount method");
		
		String fullText = itemTotalElement.getText();

		log.info("Ending of getItemTotalAmount method");
		return Double.parseDouble(fullText.replace("Item total: $", "").trim());
	}
	public void clickOnFinishButton() {
		log.info("Starting of clickOnFinishButton method");
		
		click(btnFinish);

		log.info("Ending of clickOnFinishButton method");
	}
	public String getThankYouHeader() {
		log.info("Starting of getThankYouHeader method");
		log.info("Ending of getThankYouHeader method");
		
		return getText(lblThankYouHeader).toUpperCase();
	}
}