/*
 * Copyright (C) 2015 denkbares GmbH, Germany
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

package de.knowwe.uitest;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.Platform;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.denkbares.utils.Log;

import static de.knowwe.uitest.WikiTemplate.haddock;

/**
 * Utils methods for selenium UI tests.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 03.07.15
 */
public class UITestUtils {

	public enum WebOS {
		windows, macOS, linux, other
	}

	/**
	 * In order to test locally set the following dev mode parameters
	 * -Dknowwe.devMode="true"
	 * -Dknowwe.url="your-URL"
	 */
	private static boolean DEV_MODE;
	private static String KNOWWE_URL;

	/**
	 * Loads the given article and waits for it to be loaded. If an alert pops up, it will be accepted.
	 *
	 * @param url         the url of the running wiki instance
	 * @param articleName name of the article to be loaded
	 * @param driver      the web driver
	 */
	public static void goToArticle(String url, String articleName, WebDriver driver) {
		driver.get(url + "/Wiki.jsp?page=" + articleName);
		try {
			driver.switchTo().alert().accept();
		} catch (NoAlertPresentException ignore) {
		}
	}

	public static void recompileCurrentArticle(WebDriver driver) {
		String currentUrl = driver.getCurrentUrl();
		if (!currentUrl.contains("&parse=full")) {
			currentUrl += "&parse=full";
		}
		driver.get(currentUrl);
	}

	public enum UseCase {
		LOGIN_PAGE, NORMAL_PAGE
	}

	/**
	 * Rule allowing for retries if a test fails.
	 */
	public static class RetryRule implements TestRule {

		private final int retryCount;

		public RetryRule(int retryCount) {
			this.retryCount = retryCount;
		}

		@Override
		public Statement apply(Statement base, Description description) {
			return statement(base, description);
		}

		private Statement statement(final Statement base, final Description description) {

			return new Statement() {
				@Override
				public void evaluate() throws Throwable {
					Throwable caughtThrowable = null;
					for (int i = 0; i < retryCount; i++) {
						try {
							base.evaluate();
							return;
						} catch (Throwable t) {
							caughtThrowable = t;
							Log.severe("Run " + (i + 1) + "/" + retryCount + " of '" + description.getDisplayName() + "' failed", t);
						}
					}
					Log.severe("Giving up after " + retryCount + " failures of '" + description.getDisplayName() + "'");
					throw caughtThrowable;
				}
			};
		}
	}

	/**
	 * Rule allowing for tests to run a defined number of times. Prints failures along the way.
	 */
	public static class RerunRule implements TestRule {

		private final int rerunCount;
		private int successes;

		public RerunRule(int rerunCount) {
			this.rerunCount = rerunCount;
			this.successes = 0;
		}

		@Override
		public Statement apply(Statement base, Description description) {
			return statement(base, description);
		}

		private Statement statement(final Statement base, final Description description) {

			return new Statement() {
				@Override
				public void evaluate() throws Throwable {
					Throwable caughtThrowable = null;
					for (int i = 0; i < rerunCount; i++) {
						try {
							base.evaluate();
							successes++;
							Log.severe("Run " + (i + 1) + "/" + rerunCount + " of '" + description.getDisplayName() + "' successful");
						} catch (Throwable throwable) {
							caughtThrowable = throwable;
							Log.severe("Run " + (i + 1) + "/" + rerunCount + " of '" + description.getDisplayName() + "' failed", throwable);
						}
					}
					Log.severe("Final statistic for " + description.getDisplayName() + ": " + successes + "/" + rerunCount + " successes");
					if (caughtThrowable != null) throw caughtThrowable;
				}
			};
		}
	}

	public static void logIn(WebDriver driver, String username, String password, UseCase use, WikiTemplate template) throws InterruptedException {
		List<WebElement> elements = null;
		if (use == UseCase.LOGIN_PAGE) {
			String idLoginElement = template == WikiTemplate.haddock ? "section-login" : "logincontent";
			elements = driver.findElements(By.id(idLoginElement));
		} else if (use == UseCase.NORMAL_PAGE) {
			if (template == WikiTemplate.haddock) {
				driver.findElement(By.className("userbox")).click();
				Thread.sleep(1000); //Animation
			}
			String loginSelector = template == WikiTemplate.haddock ? "a.btn.btn-primary.btn-block.login" : "a.action.login";
			elements = driver.findElements(By.cssSelector(loginSelector));
		}

		if (elements == null) {
			throw new NullPointerException("No Login Interface found.");
		} else if (elements.isEmpty()) {
			return; // already logged in
		}

		elements.get(0).click();
		driver.findElement(By.id("j_username")).sendKeys(username);
		driver.findElement(By.id("j_password")).sendKeys(password);
		driver.findElement(By.name("submitlogin")).click();
		String logoutSelector = template == WikiTemplate.haddock ? "a.btn.btn-default.btn-block.logout" : "a.action.logout";
		new WebDriverWait(driver, 10).until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(logoutSelector)));
		new WebDriverWait(driver, 10).until(ExpectedConditions.presenceOfElementLocated(By.id("edit-source-button")));
	}

	public static boolean isLoggedIn(WebDriver driver, WikiTemplate template) {
		String logoutSelector = template == WikiTemplate.haddock ? "a.btn.btn-default.btn-block.logout" : "a.action.logout";
		return !driver.findElements(By.cssSelector(logoutSelector)).isEmpty();
	}

	public static void awaitStatusChange(WebDriver driver, String status) {
		new WebDriverWait(driver, 10).until(ExpectedConditions.not(ExpectedConditions.attributeToBe(By.cssSelector("#knowWEInfoStatus"), "value", status)));
	}

	public static String getCurrentStatus(WebDriver driver) {
		return driver.findElement(By.cssSelector("#knowWEInfoStatus")).getAttribute("value");
	}

	public static String getKnowWEUrl(WikiTemplate template, String testName) {
		String defaultUrl = template == haddock ? "https://knowwe-nightly-haddock.denkbares.com" : "https://knowwe-nightly.denkbares.com";
		if (DEV_MODE) {
			KNOWWE_URL = System.getProperty("knowwe.url", defaultUrl);
		} else {
			KNOWWE_URL = defaultUrl;
		}
		return KNOWWE_URL + "/Wiki.jsp?page=" + testName;
	}

	public static boolean getDevMode() {
		return DEV_MODE;
	}

	public static void awaitRerender(WebDriver driver, By by) {
		try {
			List<WebElement> elements = driver.findElements(by);
			if (!elements.isEmpty()) {
				new WebDriverWait(driver, 5).until(ExpectedConditions.stalenessOf(elements.get(0)));
			}
		} catch (TimeoutException ignore) {
		}
		new WebDriverWait(driver, 5).until(ExpectedConditions.presenceOfElementLocated(by));
	}

	public static RemoteWebDriver setUp(DesiredCapabilities capabilities, String testClassName) throws MalformedURLException {

		DEV_MODE = Boolean.parseBoolean(System.getProperty("knowwe.devMode", "false"));
		String chromeBinary = System.getProperty("mate.chrome.binary");
		if (chromeBinary != null) {
			ChromeOptions chromeOptions = new ChromeOptions();
			chromeOptions.setBinary(chromeBinary);
			capabilities.setCapability(ChromeOptions.CAPABILITY, chromeOptions);
		}

		RemoteWebDriver driver;
		if (DEV_MODE) {
			driver = new RemoteWebDriver(new URL("http://localhost:9515"), capabilities);
		} else {
			capabilities.setCapability("name", testClassName);
			capabilities.setCapability("platform", Platform.WINDOWS);
			driver = new RemoteWebDriver(
					new URL("http://d3web:8c7e5a48-56dd-4cde-baf0-b17f83803044@ondemand.saucelabs.com:80/wd/hub"),
					capabilities);
		}
		driver.manage().window().setSize(new Dimension(1024, 768));
		return driver;
	}

	public static WebOS getWebOS(WebDriver driver) {
		JavascriptExecutor jse = (JavascriptExecutor) driver;
		String os = (String) jse.executeScript("return navigator.appVersion");
		os = os.toLowerCase();
		if (os.contains("win")) return WebOS.windows;
		if (os.contains("mac")) return WebOS.macOS;
		if (os.contains("nux") || os.contains("nix")) return WebOS.linux;
		return WebOS.other;
	}
}
