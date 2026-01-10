package com.swaglabs.pages;

import java.util.List;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import com.swaglabs.pages.base.BaseSwagLabsPage;

public class CheckOutInformationPage extends BaseSwagLabsPage {

	@FindBy(xpath = "//div[@class='checkout_info']//input")
	private List<WebElement> checkoutInputFields;
	
	@FindBy(xpath = "//input[@type='submit']")
	private WebElement btnContinue;

	public CheckOutInformationPage(WebDriver driver) {
	    super(driver);
	    log.info("======> Starting of CheckOutInformationPage Constructor <======");
	  
	    PageFactory.initElements(driver, this);
	    
	    log.info("======> Ending of CheckOutInformationPage Constructor <======");
	}


	public void enterCheckoutDetails(List<String> inputValues) {
		log.info("Starting of enterCheckoutDetails method");

		for (int i = 0; i < inputValues.size(); i++) {
			checkoutInputFields.get(i).clear();
			checkoutInputFields.get(i).sendKeys(inputValues.get(i));
		}
		log.info("Ending of enterCheckoutDetails method");

	}

	public void clickOnContinueButton() {
		log.info("Starting of clickOnContinueButton method");

		click(btnContinue);
		log.info("Ending of clickOnContinueButton method");

	}
}
