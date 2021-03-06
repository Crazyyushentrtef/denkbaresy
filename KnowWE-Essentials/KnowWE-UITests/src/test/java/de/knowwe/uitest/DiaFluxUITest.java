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
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.denkbares.strings.Strings;
import com.denkbares.test.RetryRule;

import static junit.framework.TestCase.assertEquals;

/**
 * Created by Veronika Sehne, Albrecht Striffler (denkbares GmbH) on 28.01.15.
 * <p>
 * Test the Test Protocol for DiaFlux (System Test - Manual DiaFlux BMI)
 */
@RunWith(Parameterized.class)
public class DiaFluxUITest extends KnowWEUITest {


	public DiaFluxUITest(UITestUtils.Browser browser, Platform os, WikiTemplate template) throws IOException, InterruptedException {
		super(browser, os, template);
	}

	@Parameterized.Parameters(name = "{index}: UITest-DiaFlux-{2}-{0}-{1})")
	public static Collection<Object[]> parameters() {
		return UITestUtils.getTestParametersChrome();
	}

	@Override
	public String getArticleName() {
		return "DiaFlux";
	}

	@Rule
	public RetryRule retry = new RetryRule(2);

	@Test
	public void addTerminology() throws IOException {
		changeArticleText(readFile("Step1.txt"));

		checkNoErrorsExist();
	}

	@Test
	public void addFlowchartStumps() throws Exception {
		changeArticleText(readFile("Step2.txt"));

		String article = getDriver().getWindowHandle();

		// first DiaFlux panel
		createNextFlow();
		switchToEditor(article);

		getDriver().findElement(By.id("properties.autostart")).click();

		setFlowName("BMI-Main");
		addStartNode(-150, 300);
		addExitNode(250, 300);

		saveAndSwitchBack(article);

		// second DiaFlux panel
		createNextFlow();
		switchToEditor(article);

		setFlowName("BMI-Anamnesis");

		addStartNode(-150, 300);
		addExitNode(50, 450, "Illegal arguments");
		addExitNode(-100, 600, "Weight ok");
		addExitNode(150, 600, "Weight problem");

		saveAndSwitchBack(article);

		// third DiaFlux panel
		createNextFlow();
		switchToEditor(article);

		setFlowName("BMI-SelectTherapy");
		addStartNode(-150, 300, "Mild therapy");
		addStartNode(-150, 400, "Rigorous therapy");
		addExitNode(50, 350, "Done");

		saveAndSwitchBack(article);

		// third DiaFlux panel
		createNextFlow();
		switchToEditor(article);

		setFlowName("BMI-SelectMode");
		addStartNode(-150, 300);
		addExitNode(50, 450, "Pediatrics");
		addExitNode(150, 450, "Adult");

		saveAndSwitchBack(article);

	}

	@Test
	public void implementBMIMain() throws Exception {
		changeArticleText(readFile("Step3.txt"));

		String articleHandle = getDriver().getWindowHandle();

		openVisualEditor(1);

		switchToEditor(articleHandle);

		addActionNode(-150, 60, "BMI-SelectMode");

		connect(2, 4);
		connect(4, 3, "Pediatrics");

		addActionNode(50, 150, "BMI-Anamnesis"); // 7

		connect(4, 7, "Adult");
		connect(7, 3, "Illegal arguments");
		connect(7, 3, "Weight ok");

		addActionNode(0, 220, "bmi"); // 11

		connect(7, 11, "Weight problem");

		addActionNode(-350, 220, "BMI-SelectTherapy"); // 13
		addActionNode(-350, 320, "BMI-SelectTherapy", "Rigorous therapy"); // 14

		connect(11, 13, "Formula", "gradient(bmi[-7d, 0s]) >= 0 & gradient(bmi[-7d, 0s]) < 5");
		connect(11, 14, "Formula", "gradient(bmi[-7d, 0s]) >= 5");

		addActionNode(0, 280, "Continue selected therapy", "ask"); // 17

		connect(11, 17, "Formula", "gradient(bmi[-7d, 0s]) < 0");

		addSnapshotNode(-10, 340); // 19

		connect(13, 19, "Done");
		connect(14, 19, "Done");
		connect(17, 19);

		connect(19, 7);

		saveAndSwitchBack(articleHandle);

		checkNoErrorsExist();
	}

	@Test
	public void implementBMIAnamnesis() throws Exception {
		changeArticleText(readFile("Step4.txt"));

		String articleHandle = getDriver().getWindowHandle();

		openVisualEditor(2);

		switchToEditor(articleHandle);

		addActionNode(-150, 60, "Height", "ask"); // 6
		addActionNode(50, 60, "Weight", "always ask"); // 7

		connect(2, 6);
		connect(6, 7, "> ", "0");

		addActionNode(-150, 150, "Illegal arguments", "established"); // 10

		connect(6, 10, "= ", "0");
		connect(10, 3);

		addActionNode(50, 250, "bmi", "Formula", "Weight / (Height * Height)"); // 13

		connect(7, 13, "known");

		addActionNode(-350, 250, "Weight classification", "Normal weight"); // 15
		addActionNode(-50, 320, "Weight classification", "Overweight"); // 16
		addActionNode(150, 320, "Weight classification", "Severe overweight"); // 17

		connect(13, 15, "[  ..  [", "18.5", "25");
		connect(13, 16, "[  ..  [", "25", "30");
		connect(13, 17, "??? ", "30");

		connect(15, 4);
		connect(16, 5);
		connect(17, 5);

		saveAndSwitchBack(articleHandle);

		checkNoErrorsExist();
	}

	@Test
	public void implementBMISelectTherapy() throws Exception {
		changeArticleText(readFile("Step5.txt"));

		String articleHandle = getDriver().getWindowHandle();

		openVisualEditor(3);

		switchToEditor(articleHandle);

		// white spaces to give the auto complete some time
		addActionNode(-150, 60, "Therapy       " + Keys.ARROW_DOWN + Keys.ARROW_DOWN, "Mild therapy"); // 5
		addActionNode(-150, 120, "Therapy       " + Keys.ARROW_DOWN + Keys.ARROW_DOWN, "Rigorous therapy"); // 6

		connect(2, 5);
		connect(3, 6);
		connect(5, 4);
		connect(6, 4);

		saveAndSwitchBack(articleHandle);

		checkNoErrorsExist();
	}

	@Test
	public void implementBMISelectMode() throws Exception {
		changeArticleText(readFile("Step6.txt"));

		String articleHandle = getDriver().getWindowHandle();

		openVisualEditor(4);

		switchToEditor(articleHandle);

		addActionNode(-250, 60, "Age ", "ask"); // 5
		addActionNode(0, 60, "Age classification", "Adult"); // 6
		addActionNode(-250, 120, "Age classification", "Pediatrics"); // 7
		addActionNode(0, 120, "Age classification"); // 8

		connect(2, 5);
		connect(5, 6, "> ", "14");
		connect(5, 7, "??? ", "14");
		connect(6, 8);
		connect(7, 8);
		connect(8, 3, "Pediatrics");
		connect(8, 4, "Adult");

		saveAndSwitchBack(articleHandle);

		checkNoErrorsExist();
	}

	@Test
	public void testKB1() throws IOException {
		changeArticleText(readFile("Step7.txt"));

		reset();
		setAge("21");
		setHeight("1.9");
		setWeight("90");

		assertBMI("24.930747922437675");

		assertEquals("Adult", getDriver().findElements(By.className("answerClicked")).get(0).getText());
		assertEquals("Normal weight", getDriver().findElements(By.className("answerClicked")).get(1).getText());

		reset();
	}

	@Test
	public void testKB2() throws IOException {
		changeArticleText(readFile("Step7.txt"));

		reset();
		setAge("14");
		setHeight("1.9");
		setWeight("90");

		assertBMI("");
		assertEquals("Pediatrics", getDriver().findElements(By.className("answerClicked")).get(0).getText());
		assertEquals(1, getDriver().findElements(By.className("answerClicked")).size());

		reset();
	}

	@Test
	public void testKB3() throws IOException {
		changeArticleText(readFile("Step7.txt"));

		reset();
		setAge("15");
		setHeight("1.9");
		setWeight("200");

		assertBMI("55.4016620498615");

		assertEquals("Adult", getDriver().findElements(By.className("answerClicked")).get(0).getText());
		assertEquals("Severe overweight", getDriver().findElements(By.className("answerClicked")).get(1).getText());

		reset();
	}

	@Test
	public void testKB4() throws IOException {
		changeArticleText(readFile("Step7.txt"));

		reset();
		setAge("21");
		setHeight("0");

		assertBMI("");

		assertEquals("Adult", getDriver().findElements(By.className("answerClicked")).get(0).getText());
		assertEquals("Illegal arguments", getDriver().findElement(By.cssSelector(".SOLUTION-ESTABLISHED a")).getText());
		// solution highlighted
		assertEquals("color: rgb(150, 110, 120);",
				getDriver().findElement(By.cssSelector(".type_Solution .clickable-term")).getAttribute("style"));

		reset();
	}

	// TODO: FIX AND ENABLE AGAIN
	/*@Test
	public void testTraces() throws IOException, InterruptedException {
		changeArticleText(readFile("Step7.txt"));

		reset();

		showTraces();

		// just checking the amount of highlighted nodes and edges...
		assertActiveNodes("BMI-Main", 2, 0);
		assertActiveEdges("BMI-Main", 1, 0);

		reset();
		setAge("21");
		setHeight("1.9");
		setWeight("95");

		assertActiveNodes("BMI-Main", 2, 4);
		assertActiveEdges("BMI-Main", 1, 5);

		assertActiveNodes("BMI-Anamnesis", 3, 3);
		assertActiveEdges("BMI-Anamnesis", 2, 3);

		assertActiveNodes("BMI-SelectTherapy", 0, 3);
		assertActiveEdges("BMI-SelectTherapy", 0, 2);

		assertActiveNodes("BMI-SelectMode", 0, 5);
		assertActiveEdges("BMI-SelectMode", 0, 4);
	}*/

	@Test
	public void testSpecialChars() throws Exception {
		changeArticleText(readFile("Step8.txt"));

		checkErrorsExist();

		String articleHandle = getDriver().getWindowHandle();

		openVisualEditor(3);

		switchToEditor(articleHandle);

		editActionNode(5, "Therapy ??????", "" + Keys.ARROW_DOWN);
		editActionNode(6, "Therapy ??????", "" + Keys.ARROW_DOWN);
		//editActionNode(6, "Therapy       " + Keys.ARROW_DOWN + Keys.ARROW_DOWN, "" + Keys.ARROW_DOWN + Keys.ARROW_DOWN);

		saveAndSwitchBack(articleHandle);

		checkNoErrorsExist();

		openVisualEditor(3);

		switchToEditor(articleHandle);

		setFlowName("BMI-SelectTherapy ????????$`??/\\=,!{};:_-");

		editStartNode(2, "Mild therapy ????????$`??#/\\\\|=,!{};:_-");
		editStartNode(3, "Rigorous therapy ????????$`??#/\\\\|=,!{};:_-");

		addCommentNode(-400, 300, "Here we test a lot of special characters ????????&%$??`??<#>/\\\\|=,!(){};:_-"); // 11

		new Actions(getDriver()).dragAndDropBy(getDriver().findElement(By.id("#node_3")), -100, 0).perform();
		connect(getDriver().findElement(By.id("#rule_8")).findElement(By.className("rule_selector")), 11);
		connect(11, 6);

		editExitNode(4, "Done ????????$`??#/\\|=,!{};:_-");

		saveAndSwitchBack(articleHandle);

		checkErrorsExist();

		openVisualEditor(1);

		switchToEditor(articleHandle);

		editActionNode(13, "BMI-SelectTh", "" + Keys.ARROW_DOWN);
		editActionNode(14, "BMI-SelectTh", "" + Keys.ARROW_DOWN + Keys.ARROW_DOWN);

		editEdge(20, "Done ??????");
		editEdge(21, "Done ??????");

		saveAndSwitchBack(articleHandle);

		checkNoErrorsExist();
	}

	// TODO: FIX AND ENABLE AGAIN
/*
	@Test
	public void testSpecialCharTraces() throws IOException, InterruptedException {
		changeArticleText(readFile("Step9.txt"));

		reset();

		showTraces();

		// just checking the amount of highlighted nodes and edges...
		assertActiveNodes("BMI-Main", 2, 0);
		assertActiveEdges("BMI-Main", 1, 0);

		reset();
		setAge("21");
		setHeight("1.9");
		setWeight("95");

		assertBMI("26.315789473684212");
		assertEquals("Adult", getDriver().findElements(By.className("answerClicked")).get(0).getText());
		assertEquals("Overweight", getDriver().findElements(By.className("answerClicked")).get(1).getText());

		assertActiveNodes("BMI-Main", 2, 4);
		assertActiveEdges("BMI-Main", 1, 5);

		assertActiveNodes("BMI-Anamnesis", 3, 3);
		assertActiveEdges("BMI-Anamnesis", 2, 3);

		assertActiveNodes("flow_1c072bbf", 0, 4); // BMI-SelectTherapy
		assertActiveEdges("flow_1c072bbf", 0, 3); // BMI-SelectTherapy

		assertActiveNodes("BMI-SelectMode", 0, 5);
		assertActiveEdges("BMI-SelectMode", 0, 4);
	}

	private void showTraces() throws UnsupportedEncodingException {
		if (getDriver().findElements(By.className("traceActive")).isEmpty()) {
			clickTool("type_DiaFlux", 2, "highlights active nodes");
			new WebDriverWait(getDriver(), 10).until(ExpectedConditions.presenceOfElementLocated(By.className("traceActive")));
		}
	}*/

	private void editEdge(int edgeId, String text) {
		WebElement rule = getDriver().findElement(By.id("#rule_" + edgeId));
		rule.findElement(By.className("rule_selector")).click();
		Select ruleSelect = new Select(rule.findElement(By.tagName("select")));
		try {
			ruleSelect.selectByVisibleText(text);
		}
		catch (NoSuchElementException e) {
			// selecting by text fails with chrome and special chars... try to match as good as possible
			List<WebElement> options = ruleSelect.getOptions();
			for (WebElement option : options) {
				if (option.getText().startsWith(text)) {
					String value = option.getAttribute("value");
					ruleSelect.selectByValue(value);
					break;
				}
			}
		}
	}

	private void assertActiveNodes(String flow, int expectedActive, int expectedSnap) {
		assertEquals(expectedActive, getDriver().findElements(By.cssSelector("#" + flow + " .Node.traceActive"))
				.size());
		assertEquals(expectedSnap, getDriver().findElements(By.cssSelector("#" + flow + " .Node.traceSnap")).size());
	}

	private void assertActiveEdges(String flow, int expectedActive, int expectedSnap) {
		int actualActive = 0;
		int actualSnap = 0;
		for (WebElement rule : getDriver().findElements(By.cssSelector("#" + flow + " .Rule"))) {
			if (!rule.findElements(By.className("traceSnap")).isEmpty()) actualSnap++;
			if (!rule.findElements(By.className("traceActive")).isEmpty()) actualActive++;
		}
		assertEquals(expectedActive, actualActive);
		assertEquals(expectedSnap, actualSnap);
	}

	private void reset() {
		String currentStatus = UITestUtils.getCurrentStatus(getDriver());
		getDriver().findElement(By.className("reset")).click();
		UITestUtils.awaitStatusChange(getDriver(), currentStatus);
	}

	private void assertBMI(String expected) {
		assertEquals(expected, getDriver().findElements(By.className("numinput")).get(3).getAttribute("value"));
	}

	private void setWeight(String value) {
		setValue(value, 2);
	}

	private void setHeight(String value) {
		setValue(value, 1);
	}

	private void setAge(String value) {
		setValue(value, 0);
	}

	private void setValue(String value, int index) {
		String currentStatus = UITestUtils.getCurrentStatus(getDriver());
		getDriver().findElements(By.className("numinput")).get(index).sendKeys(value + Keys.ENTER);
		UITestUtils.awaitStatusChange(getDriver(), currentStatus);
	}

	private void connect(int sourceId, int targetId, String... text) throws InterruptedException {
		connect(getDriver().findElement(By.id("#node_" + sourceId)), targetId, text);
	}

	private void connect(WebElement source, int targetId, String... text) throws InterruptedException {
		source.click();
		WebElement arrowTool = getDriver().findElement(By.className("ArrowTool"));
		WebElement targetNode = getDriver().findElement(By.id("#node_" + targetId));
		(new Actions(getDriver())).dragAndDrop(arrowTool, targetNode).perform();
		if (text.length > 0) {
			Select select = new Select(getDriver().findElement(By.cssSelector(".selectedRule select")));
			if (text[0].equalsIgnoreCase("formula")) {
				select.selectByIndex(10);
				getDriver().findElement(By.cssSelector(".selectedRule textarea")).sendKeys(text[1] + Keys.ENTER);
			}
			else {
				switch (text[0]) {
					case "[  ..  ]":
						select.selectByValue("8");
						break;
					case "[  ..  [":
						select.selectByValue("9");
						break;
					default:
						select.selectByVisibleText(text[0].trim());
						break;
				}
				if (text.length > 1) {
					List<WebElement> inputs = getDriver().findElements(By.cssSelector(".GuardEditor input"));
					inputs.get(0).sendKeys(text[1]);
					if (text.length > 2) {
						inputs.get(1).sendKeys(text[2] + Keys.ENTER);
					}
					else {
						inputs.get(0).sendKeys(Keys.ENTER);
					}
				}
			}
		}
	}

	private void openVisualEditor(int nth) throws Exception {
		int attempt = 0;
		while (attempt < 5 && getDriver().getWindowHandles().size() == 1) {
			attempt++;
			try {
				clickTool("type_DiaFlux", nth, "visual editor");
				Thread.sleep(500);
			}
			catch (Exception e) {
				if (attempt == 4) {
					throw new Exception(e);
				}
			}
		}
	}

	protected void clickTool(String markupClass, int nth, String tooltipContains) throws UnsupportedEncodingException {
		WebElement markup = getDriver().findElements(By.className(markupClass)).get(nth - 1);
		WebElement toolMenu = markup.findElement(By.className("headerMenu"));
		WebElement editTool = markup.findElements(By.cssSelector(".markupMenu a.markupMenuItem"))
				.stream()
				.filter(element -> Strings.containsIgnoreCase(element.getAttribute("title"), tooltipContains))
				.findFirst().get();
		if (getDriver() instanceof JavascriptExecutor) {
			List<WebElement> stickyRows = getDriver().findElements(By.className("sticky"));
			JavascriptExecutor jse = (JavascriptExecutor) getDriver();
			for (WebElement row : stickyRows) {
				jse.executeScript("arguments[0].style.display = 'none';", row);
			}
		}
		new Actions(getDriver()).moveToElement(toolMenu).moveToElement(editTool).click(editTool).perform();
	}

	private void createNextFlow() {
		new WebDriverWait(getDriver(), 5)
				.until(ExpectedConditions.presenceOfElementLocated(By.linkText("Click here to create one.")))
				.click();
	}

	private void saveAndSwitchBack(String winHandleBefore) {
		getDriver().findElement(By.id("saveClose")).click();
		getDriver().switchTo().window(winHandleBefore);
		String pageContentSelector = getTemplate() instanceof HaddockTemplate ? ".page-content" : "#pagecontent";
		UITestUtils.awaitRerender(getDriver(), By.cssSelector(pageContentSelector));
	}

	private void addActionNode(int xOffset, int yOffset, String... text) throws InterruptedException {
		addNode(xOffset, yOffset, By.id("decision_prototype"), By.cssSelector(".NodeEditor .ObjectSelect *"), text);
	}

	private void addStartNode(int xOffset, int yOffset, String... text) throws InterruptedException {
		addNode(xOffset, yOffset, By.id("start_prototype"), By.cssSelector(".NodeEditor .startPane input"), text);
	}

	private void addSnapshotNode(int xOffset, int yOffset, String... text) throws InterruptedException {
		addNode(xOffset, yOffset, By.id("snapshot_prototype"), By.cssSelector(".NodeEditor .snapshotPane input"), text);
	}

	private void addCommentNode(int xOffset, int yOffset, String... text) throws InterruptedException {
		addNode(xOffset, yOffset, By.id("comment_prototype"), By.cssSelector(".NodeEditor .commentPane textarea"), text);
	}

	private void addExitNode(int xOffset, int yOffset, String... text) throws InterruptedException {
		addNode(xOffset, yOffset, By.id("exit_prototype"), By.cssSelector(".NodeEditor .exitPane input"), text);
	}

	private void addNode(int xOffset, int yOffset, By prototypeSelector, By textSelector, String... text) throws InterruptedException {
		WebElement start = getDriver().findElement(prototypeSelector);
		new Actions(getDriver()).dragAndDropBy(start, xOffset, yOffset).perform();
		Thread.sleep(200);
		if (text.length > 0) {
			String selector = prototypeSelector.toString();
			if (selector.contains("start") || selector.contains("snapshot") || selector.contains("exit")) { // for decision nodes, editor opens automatically
				List<WebElement> nodes = getDriver().findElements(By.cssSelector(".Flowchart > .Node"));
				WebElement newNode = nodes.get(nodes.size() - 1);
				new Actions(getDriver()).doubleClick(newNode).perform();
			}
			setNodeAttributes(textSelector, text);
		}

	}

	private void editStartNode(int nodeId, String... text) throws InterruptedException {
		editNode(nodeId, By.cssSelector(".NodeEditor .startPane input"), text);
	}

	private void editExitNode(int nodeId, String... text) throws InterruptedException {
		editNode(nodeId, By.cssSelector(".NodeEditor .exitPane input"), text);
	}

	private void editActionNode(int nodeId, String... text) throws InterruptedException {
		editNode(nodeId, By.cssSelector(".NodeEditor .ObjectSelect *"), text);
	}

	private void editNode(int nodeId, By textSelector, String... text) throws InterruptedException {
		new Actions(getDriver()).doubleClick(getDriver().findElement(By.id("#node_" + nodeId))).perform();
		setNodeAttributes(textSelector, text);
	}

	private void setNodeAttributes(By textSelector, String... text) throws InterruptedException {
		getDriver().findElement(textSelector).click();
		getDriver().findElement(textSelector).clear();
		getDriver().findElement(textSelector).sendKeys(text[0]);
		Thread.sleep(200);
		getDriver().findElement(textSelector).sendKeys(Keys.ENTER);
		if (text.length > 1) {
			Select actionSelect = new Select(getDriver().findElement(By.cssSelector(".ActionEditor select")));
			if (text[1].equalsIgnoreCase("formula")) {
				actionSelect.selectByIndex(1);
				//actionSelect.findElement(By.xpath("//option[@value='" + 1 + "']")).click();
				//actionSelect.findElements(By.tagName("option")).get(1).click();
				getDriver().findElement(By.cssSelector(".ActionEditor textarea")).sendKeys(text[2] + Keys.ENTER);
			}
			else if (text[1].startsWith("" + Keys.ARROW_DOWN)) {
				actionSelect.selectByIndex(text[1].length() - 1);
				//actionSelect.sendKeys(text[1] + Keys.ENTER);
			}
			else {
				actionSelect.selectByVisibleText(text[1]);
				//actionSelect.findElement(By.xpath("//option[text()='" + text[1] + "']")).click();
			}
		}

		List<WebElement> okButtons = getDriver().findElements(By.cssSelector(".NodeEditor .ok"));
		if (okButtons.size() == 1) okButtons.get(0).click();
	}

	private void setFlowName(String flowName) {
		getDriver().findElement(By.id("properties.editName")).clear();
		getDriver().findElement(By.id("properties.editName")).sendKeys(flowName);
	}

	protected void switchToEditor(String articleHandle) throws InterruptedException {
		new WebDriverWait(getDriver(), 10).until((WebDriver driver) -> driver.getWindowHandles().size() == 2);
		Set<String> windowHandles = new HashSet<>(getDriver().getWindowHandles());
		windowHandles.remove(articleHandle);
		getDriver().switchTo().window(windowHandles.iterator().next());
		new WebDriverWait(getDriver(), 10).until(ExpectedConditions.presenceOfElementLocated(By.id("start_prototype")));
	}

}
