package com.swaglabs.pages.base;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

public class BaseSwagLabsPage {
	protected WebDriver driver = null;
	protected WebDriverWait wait;
	protected static final Logger log = LogManager.getLogger(BaseSwagLabsPage.class);

	public static String TEST_FILE_PATH = null;
	protected static Properties langXPathsProperties = null;
	protected static Properties tesdataProp;

	public BaseSwagLabsPage(WebDriver driver) {
		this.driver = driver;
		this.wait = new WebDriverWait(driver, java.time.Duration.ofSeconds(15));
		PageFactory.initElements(driver, this);
	}

	public WebDriver getWebDriver() {
		return this.driver;
	}

	public WebDriver getChildWebDriver() {
		return driver;
	}

	public void waitUntilVisible(WebElement element) {
		wait.until(ExpectedConditions.visibilityOf(element));
	}

	public void click(WebElement element) {
		waitUntilVisible(element);
		element.click();
	}

	public void enterText(WebElement element, String text) {
		waitUntilVisible(element);
		element.clear();
		element.sendKeys(text);
	}

	public String getText(WebElement element) {
		waitUntilVisible(element);
		return element.getText();
	}

	public void selectByValue(WebElement dropdownElement, String value) {
		waitUntilVisible(dropdownElement);
		Select select = new Select(dropdownElement);
		select.selectByValue(value);
	}

	public List<String> getFirstNElementTexts(List<WebElement> elements, int count) {
		List<String> texts = new ArrayList<>();

		for (int i = 0; i < count && i < elements.size(); i++) {
			texts.add(elements.get(i).getText().trim());
		}

		return texts;
	}

	// Reusable method to check if first N prices are sorted low to high
	public boolean areFirstNPricesSortedLowToHigh(List<WebElement> priceElements, int count) {
		List<Double> prices = new ArrayList<>();

		for (int i = 0; i < count && i < priceElements.size(); i++) {
			String priceText = priceElements.get(i).getText().replace("$", "").trim();
			prices.add(Double.parseDouble(priceText));
		}

		List<Double> sortedPrices = new ArrayList<>(prices);
		Collections.sort(sortedPrices);

		return prices.equals(sortedPrices);
	}
}
