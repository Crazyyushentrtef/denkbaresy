package de.knowwe.uitest;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.denkbares.strings.Strings;

/**
 * DiaFluxSystemTest for FireFox.
 * <p>
 * You will need a ST-BMI-FireFox wiki page in order to carry out this test locally
 * <p>
 * Created by Albrecht Striffler (denkbares GmbH) on 25.04.2015.
 */
public abstract class FireFoxDiaFluxSystemTest extends DiaFluxSystemTest {

	private static RemoteWebDriver driver;

	@BeforeClass
	public static void setUp() throws Exception {
		driver = UITestUtils.setUp(devMode, DesiredCapabilities.firefox(), FireFoxDiaFluxSystemTest.class.getSimpleName());
	}

	@AfterClass
	public static void tearDown() throws Exception {
		// if we quit, we don't see the status of the test at the end
		//noinspection ConstantConditions
		if (!devMode) driver.quit();
	}

	@Override
	protected WebDriver getDriver() {
		return driver;
	}

	@Override
	public String getTestName() {
		return "ST-BMI-FireFox";
	}

	@Override
	protected void clickTool(String markupClass, int nth, String tooltipContains) throws UnsupportedEncodingException {
		// Hack since FireFoxDriver does not support moveToElement() method
		WebElement markup = getDriver().findElements(By.className(markupClass)).get(nth - 1);
		WebElement toolMenu = markup.findElement(By.className("headerMenu"));
		WebElement editTool = markup.findElements(By.cssSelector(".markupMenu a.markupMenuItem"))
				.stream()
				.filter(element -> Strings.containsIgnoreCase(element.getAttribute("title"), tooltipContains))
				.findFirst().get();
		if (getDriver() instanceof JavascriptExecutor) {
			JavascriptExecutor jse = (JavascriptExecutor) getDriver();
			String js = URLDecoder.decode(editTool.getAttribute("href"), "UTF-8");
			jse.executeScript(js);
		}
	}
}
