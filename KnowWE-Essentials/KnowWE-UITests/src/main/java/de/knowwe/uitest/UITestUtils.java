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

import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
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
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.denkbares.strings.Strings;
import com.denkbares.utils.Log;

import static de.knowwe.uitest.UITestUtils.UseCase.LOGIN_PAGE;

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

	public enum Browser {
		firefox, chrome
	}

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
		}
		catch (NoAlertPresentException ignore) {
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

	public static void logIn(WebDriver driver, String username, String password, UseCase use, WikiTemplate template) throws InterruptedException {
		template.login(driver, use, username, password);
	}

	private static boolean isLoggedIn(WebDriver driver, WikiTemplate template) {
		String logoutSelector = template == HaddockTemplate.getInstance() ? "a.btn.btn-default.btn-block.logout" : "a.action.logout";
		return !driver.findElements(By.cssSelector(logoutSelector)).isEmpty();
	}

	public static void awaitStatusChange(WebDriver driver, String status) {
		new WebDriverWait(driver, 10).until(ExpectedConditions.not(ExpectedConditions.attributeToBe(By.cssSelector("#knowWEInfoStatus"), "value", status)));
	}

	public static String getCurrentStatus(WebDriver driver) {
		return driver.findElement(By.cssSelector("#knowWEInfoStatus")).getAttribute("value");
	}

	public static String getKnowWEUrl(WikiTemplate template, String articleName, boolean devMode) {
		String defaultUrl = template instanceof HaddockTemplate ? "https://knowwe-nightly-haddock.denkbares.com" : "https://knowwe-nightly.denkbares.com";
		String knowweUrl;
		if (devMode) {
			knowweUrl = System.getProperty(template instanceof HaddockTemplate ? "knowwe.haddock.url" : "knowwe.standard.url", defaultUrl);
		}
		else {
			knowweUrl = defaultUrl;
		}
		return knowweUrl + "/Wiki.jsp?page=" + articleName;
	}

	public static LinkedList<Object[]> getTestParametersChromeAndFireFox() {
		LinkedList<Object[]> params = new LinkedList<>();
		params.addAll(getTestParametersFireFox());
		params.addAll(getTestParametersChrome());
		return params;
	}

	public static LinkedList<Object[]> getTestParametersFireFox() {
		LinkedList<Object[]> params = new LinkedList<>();
		params.add(new Object[] { Browser.firefox, Platform.WINDOWS, HaddockTemplate.getInstance() });
		params.add(new Object[] { Browser.firefox, Platform.WINDOWS, DefaultTemplate.getInstance() });
//		params.add(new Object[] { Browser.firefox, Platform.MAC, HaddockTemplate.getInstance() });
//		params.add(new Object[] { Browser.firefox, Platform.MAC, DefaultTemplate.getInstance() });
//		params.add(new Object[] { Browser.firefox, Platform.LINUX, HaddockTemplate.getInstance() });
//		params.add(new Object[] { Browser.firefox, Platform.LINUX, DefaultTemplate.getInstance() });
		return params;
	}

	public static LinkedList<Object[]> getTestParametersChrome() {
		LinkedList<Object[]> params = new LinkedList<>();
		params.add(new Object[] { Browser.chrome, Platform.WINDOWS, HaddockTemplate.getInstance() });
		params.add(new Object[] { Browser.chrome, Platform.WINDOWS, DefaultTemplate.getInstance() });
//		params.add(new Object[] { Browser.chrome, Platform.MAC, HaddockTemplate.getInstance() });
//		params.add(new Object[] { Browser.chrome, Platform.MAC, DefaultTemplate.getInstance() });
//		params.add(new Object[] { Browser.chrome, Platform.LINUX, HaddockTemplate.getInstance() });
//		params.add(new Object[] { Browser.chrome, Platform.LINUX, DefaultTemplate.getInstance() });
		return params;
	}

	public static void awaitRerender(WebDriver driver, By by) {
		try {
			List<WebElement> elements = driver.findElements(by);
			if (!elements.isEmpty()) {
				new WebDriverWait(driver, 5).until(ExpectedConditions.stalenessOf(elements.get(0)));
			}
		}
		catch (TimeoutException ignore) {
		}
		new WebDriverWait(driver, 5).until(ExpectedConditions.presenceOfElementLocated(by));
	}

	public static RemoteWebDriver setUp(Browser browser, Platform os, WikiTemplate template, String articleName, boolean devMode) throws IOException, InterruptedException {

		String testName = "UITest-" + articleName + "-" + template + "-" + browser + "-" + os.name().toLowerCase();

		DesiredCapabilities capabilities = new DesiredCapabilities();
		capabilities.setCapability(CapabilityType.BROWSER_NAME, browser);

		if (browser == Browser.chrome) {
			String chromeBinary = System.getProperty("knowwe.chrome.binary");
			if (chromeBinary != null) {
				ChromeOptions chromeOptions = new ChromeOptions();
				chromeOptions.setBinary(chromeBinary);
				capabilities.setCapability(ChromeOptions.CAPABILITY, chromeOptions);
			}
		}
		else if (browser == Browser.firefox) {
		}
		else {
			throw new IllegalArgumentException("Browser " + browser + " not yet supported.");
		}

		RemoteWebDriver driver;
		if (devMode) {
			if (browser == Browser.chrome) {
				driver = new RemoteWebDriver(new URL("http://localhost:9515"), capabilities);
			}
			else //noinspection ConstantConditions
				if (browser == Browser.firefox) {
					driver = new FirefoxDriver();
				}
				else {
					throw new IllegalArgumentException();
				}
		}
		else {
			capabilities.setCapability("name", testName);
			capabilities.setCapability("platform", os);
			driver = new RemoteWebDriver(
					new URL("http://d3web:8c7e5a48-56dd-4cde-baf0-b17f83803044@ondemand.saucelabs.com:80/wd/hub"),
					capabilities);
		}
		driver.manage().window().setSize(new Dimension(1024, 768));
		driver.get(UITestUtils.getKnowWEUrl(template, "Main", devMode));
		if (!UITestUtils.isLoggedIn(driver, template)) {
			driver.get(UITestUtils.getKnowWEUrl(template, "Login", devMode));
			UITestUtils.logIn(driver, "UiTest", "fyyWWyVeHzzHfkUMZxUQ?3nDBPbTT6", LOGIN_PAGE, template);
		}
		driver.get(getKnowWEUrl(template, testName, devMode));
		if (!pageExists(template, driver)) {
			createDummyPage(template, driver);
		}
		Log.info("New web driver for test " + testName);
		return driver;
	}

	private static boolean pageExists(WikiTemplate template, WebDriver driver) {
		if (template instanceof HaddockTemplate) {
			try {
				new WebDriverWait(driver, 10).until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("a.createpage")));
			}
			catch (Exception e) {
				// Element not present
			}
			return driver.findElements(By.cssSelector("a.createpage")).isEmpty();
		}
		else {
			try {
				new WebDriverWait(driver, 10).until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("div.information a")));
			}
			catch (Exception e) {
				// Element not present
			}
			return driver.findElements(By.cssSelector("div.information a"))
					.stream()
					.noneMatch(webElement -> Strings.containsIgnoreCase(webElement.getText(), "create it"));
		}
	}

	private static void createDummyPage(WikiTemplate template, WebDriver driver) throws IOException {
		WebElement href;
		if (template instanceof HaddockTemplate) {
			href = driver.findElement(By.cssSelector("a.createpage"));
		}
		else {
			href = driver.findElements(By.cssSelector("div.information a"))
					.stream()
					.filter(webElement -> Strings.containsIgnoreCase(webElement.getText(), "create it"))
					.findFirst()
					.orElseThrow(() -> new WebDriverException("Create button not found"));
		}
		href.click();
		enterArticleText(Strings.readFile("src/test/resources/Dummy.txt"), driver, template);

	}

	public static void enterArticleText(String newText, WebDriver driver, WikiTemplate template) {
		String areaSelector = template == HaddockTemplate.getInstance() ? ".editor.form-control" : "#editorarea";
		List<WebElement> editorAreas = new WebDriverWait(driver, 10).until(ExpectedConditions.presenceOfAllElementsLocatedBy(By
				.cssSelector(areaSelector)));
		if (driver instanceof JavascriptExecutor) {
			// hacky but fast/instant!
			((JavascriptExecutor) driver).executeScript("var areas = document.querySelectorAll('" + areaSelector + "');" +
					"for (var i=0; i<areas.length; i++) { areas[i].value = arguments[0] };", newText);
		}
		else {
			// sets the keys one by one, pretty slow...
			editorAreas.forEach(WebElement::clear);
			editorAreas.forEach(webElement -> webElement.sendKeys(newText));
		}
		driver.findElement(By.name("ok")).click();
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
						}
						catch (Throwable t) {
							caughtThrowable = t;
							Log.severe("Run " + (i + 1) + "/" + retryCount + " of '" + description.getDisplayName() + "' failed", t);
						}
					}
					Log.severe("Giving up after " + retryCount + " failures of '" + description.getDisplayName() + "'");
					if (caughtThrowable != null) {
						throw caughtThrowable;
					}
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
						}
						catch (Throwable throwable) {
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
}
