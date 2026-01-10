package com.swaglabs.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import com.swaglabs.pages.base.BaseSwagLabsPage;

public class LoginPage extends BaseSwagLabsPage {
	@FindBy(id = "user-name")
	private WebElement txtUserName;

	@FindBy(id = "password")
	private WebElement txtPassword;

	@FindBy(id = "login_credentials")
	private WebElement lblExistingUsername;

	@FindBy(className = "login_password")
	private WebElement lblExistingPassword;

	@FindBy(css = "#login-button")
	private WebElement btnLogin;

	public LoginPage(WebDriver driver) {
		super(driver);
		log.info("======> Starting of LoginPage Constructor <======");
	    
		PageFactory.initElements(driver, this);
	    
		log.info("======> Ending of LoginPage Constructor <======");
	}

	public void enterUsername(String username) {
		log.info("Starting of enterUsername method");

		enterText(txtUserName, getUsername());
		
		log.info("Ending of enterUsername method");
	}

	public void enterPassword(String password) {
		log.info("Starting of enterPassword method");

		enterText(txtPassword, getPassword());

		log.info("Ending of enterPassword method");
	}

	public void clickLoginButton() {
		log.info("Starting of clickLoginButton method");

		click(btnLogin);
		
		log.info("Ending of clickLoginButton method");

	}

	public void login() {
		log.info("Starting of login method");

		enterText(txtUserName, getUsername());
		enterText(txtPassword, getPassword());
		click(btnLogin);
		log.info("Ending of login method");

	}

	public String getUsername() {
		log.info("Starting of getUsername method");

		String[] usernames = getText(lblExistingUsername).split("\n");
		System.out.println(usernames[1]);
		log.info("Ending of getUsername method");

		return usernames[1];
	}

	public String getPassword() {
		log.info("Starting of getPassword method");

		String[] password = getText(lblExistingPassword).split("\n");
		log.info("Ending of getPassword method");

		return password[1];

	}

}
