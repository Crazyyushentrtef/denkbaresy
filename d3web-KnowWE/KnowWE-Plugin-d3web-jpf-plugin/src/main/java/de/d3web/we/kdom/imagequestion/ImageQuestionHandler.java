/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package de.d3web.we.kdom.imagequestion;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import de.d3web.core.io.progress.DummyProgressListener;
import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionChoice;
import de.d3web.core.knowledge.terminology.QuestionMC;
import de.d3web.core.knowledge.terminology.QuestionOC;
import de.d3web.core.knowledge.terminology.info.Property;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.core.session.Session;
import de.d3web.core.session.Value;
import de.d3web.core.session.values.Choice;
import de.d3web.core.session.values.ChoiceValue;
import de.d3web.core.session.values.MultipleChoiceValue;
import de.d3web.core.session.values.UndefinedValue;
import de.d3web.core.session.values.Unknown;
import de.d3web.multimedia.io.ImageQuestionPersistenceHandler;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.knowledgeService.D3webKnowledgeService;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.taghandler.AbstractTagHandler;
import de.d3web.we.utils.D3webUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;

/**
 * Handling ImageQuestions with AnswerRegions.
 * 
 * @author Johannes Dienst
 *
 */
public class ImageQuestionHandler extends AbstractTagHandler {

	private static final String config_knowledgebase_path=
		"kbResources/RheumaDemoPPRheumaDemo_KB/";
	private static final String config_knowledgebase_imagequestion_path=
		"C:/Users/ManiaC/knowwe_newBranch/d3web-KnowWE/KnowWE/target/KnowWE-0.2-SNAPSHOT"+
		"/kbResources/RheumaDemoPPRheumaDemo_KB/imagequestion/picturequestions.xml";
	private static final String kbjarpath =
		"C:/Users/ManiaC/knowwe_newBranch/d3web-KnowWE/KnowWE/target/" +
		"KnowWE-0.2-SNAPSHOT/WEB-INF/resources/knowledgebases/RheumaDemoPPRheumaDemo_KB.jar";
	
	public ImageQuestionHandler() {
		super("imagequestionhandler");
	}

	@Override
	public String getDescription(KnowWEUserContext user) {
		return D3webModule.getKwikiBundle_d3web(user).
			getString("KnowWE.ImageQuestionHandler.Description");
	}
	
	@Override
	public String render(String topic, KnowWEUserContext user,
			Map<String, String> values, String web) {
		return this.render(topic, user, values, web, true);
	}
	
	/**
	 * Used to Rerender the ImageQuestion. So Checkboxes
	 * can be set, when a AnswerRegion was clicked.
	 * 
	 * @param topic
	 * @param user
	 * @param values
	 * @param web
	 * @return
	 */
	public String renderForRerenderAction(String topic, KnowWEUserContext user,
			Map<String, String> values, String web) {
		return this.render(topic, user, values, web, false);
	}
	
	@SuppressWarnings("unchecked")
	public String render(String topic, KnowWEUserContext user,
			Map<String, String> values, String web,  boolean renderDIV) {

		D3webKnowledgeService service = D3webModule.getAD3webKnowledgeServiceInTopic(web, topic);
		KnowledgeBase kb = service.getBase();		

		// TODO: This should be loaded from persistence right away!
		try {
			String path = KnowWEEnvironment.getInstance().getKnowWEExtensionPath()
				+ ImageQuestionHandler.config_knowledgebase_path
				+ "imagequestion/picturequestions.xml";
			path = path.replaceAll("KnowWEExtension", "/");
			FileInputStream stream = new FileInputStream(
					new File(path));
			BufferedInputStream stream2 = new BufferedInputStream(stream);
			ImageQuestionPersistenceHandler handler = new ImageQuestionPersistenceHandler();
			handler.read(kb, stream2, new DummyProgressListener());
		} catch (IOException e1) {
			// TODO Welchen Logger benutzen
		}
		
		// Read out the properties
		String questionID = values.get("question");
		KnowledgeBaseManagement kbm = KnowledgeBaseManagement.createInstance(kb);
		Question q = kbm.findQuestion(questionID);
		List props = (List) q.getProperties().getProperty(Property.IMAGE_QUESTION_INFO);
		String imageName = (String)props.get(0);
		
		List<AnswerRegion> answerRegions = 
			this.buildAnswerRegions((List)props.get(1));
		
		StringBuffer renderedImage = new StringBuffer();
		StringBuffer renderedCheckBoxes = new StringBuffer();
		try {
			this.renderQuestionImage(renderedImage, imageName, q, answerRegions, kb,
					topic, user.getUsername(), web);
			this.renderQuestionChoiceColumns(
					renderedCheckBoxes, D3webUtils.getSession(topic, user, web),
					q, answerRegions, topic, user.getUsername(), web);			
		} catch (IOException e) {
			Logger.getLogger(ImageQuestionHandler.class.getName())
				.warning(
					"Error in rendering method of ImageQuestionHandler : "
						+ e.getMessage());
		}

		// Render DIV only if it is not RerenderingAction
		StringBuffer buffi = new StringBuffer();
		if (renderDIV) {
			buffi.append("<div id='imagequestion_"
					+ q.getId()
					+ "' class='panel'>"
					+ "<h3> ImageQuestion </h3>");
		}
		buffi.append( 
			"<table id=\"imagetable_" + q.getId() + "\"><tr><td>"
			+ renderedImage.toString()
			+ "</td>"
			+ "<td>"
			+ renderedCheckBoxes.toString()
			+ "</td>"
			+ "</tr></table>");
		if (renderDIV)
			buffi.append("</div>");
		
		return buffi.toString();
	}

	/**
	 * Builds a list with AnswerRegions from the Attributes.
	 * 
	 * @param answerRegions
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private List<AnswerRegion> buildAnswerRegions(List answerRegions) {
		List<AnswerRegion> buildAR = new ArrayList<AnswerRegion>();
		List<String> attributes = null;
		for (int i = 0; i < answerRegions.size(); i++) {
			attributes = (List<String>)answerRegions.get(i);
			String answerID = attributes.get(0);
			int xStart = Integer.parseInt(attributes.get(1));
			int xEnd = Integer.parseInt(attributes.get(2));
			int yStart = Integer.parseInt(attributes.get(3));
			int yEnd = Integer.parseInt(attributes.get(4));
			AnswerRegion reg = new AnswerRegion(answerID, xStart, xEnd, yStart, yEnd);
			buildAR.add(reg);
		}
		return buildAR;
	}
	
	/**
	 * Renders a DIV with the question image of a given
	 * ImageQuestion. It also includes the AnswerRegions.
	 * 
	 * TODO: The Image should be loaded from the jar as Resource.
	 * 
	 * @param buffi
	 * @param questionImage
	 * @param q
	 * @param answerRegs
	 * @param kb
	 * @param topic
	 * @param user
	 * @param web
	 * @throws IOException
	 */
	private void renderQuestionImage(StringBuffer buffi,
			String questionImage, Question q, List<AnswerRegion> answerRegs,
			KnowledgeBase kb, String topic, String user, String web)
			throws IOException {
		
		buffi.append("<div id=\"" + q.getId() + "\" class=\"questionImage\"");
		
		String path = KnowWEEnvironment.getInstance().getKnowWEExtensionPath() +
		ImageQuestionHandler.config_knowledgebase_path + "multimedia/" + questionImage;
		path = path.replaceAll("KnowWEExtension", "");
		
		File imgFile = new File(path);
		
		// TODO: Does not work, because the image is not loaded in the
		// knowledge as a resource
//		FileInputStream stream = new FileInputStream(ImageQuestionHandler.kbjarpath);
//		List<Resource> res = kb.getResources();
//		String pathName = kb.getResource(questionImage).getPathName();
//		File bla = new File(pathName);

		BufferedImage img = ImageIO.read(imgFile.toURI().toURL());
		
		StringBuffer buffer = new StringBuffer();
		buffer.append("position: relative;");
		buffer.append("width: " + img.getWidth() + "px;");
		buffer.append("height: " + img.getHeight() + "px;");
		
		buffer.append("margin-left: auto;");
		buffer.append("margin-right: auto;");
			
		String relImagePath = ImageQuestionHandler.config_knowledgebase_path
							  + "multimedia/"
								+ questionImage ;
		buffer.append("background-image:url(" + relImagePath + ");");
		buffi.append(" style=\"" + buffer.toString() + "\">");

		// render AnswerRegions
		for (AnswerRegion region : answerRegs) {
			String t = renderAnswerRegion(region, kb, q, topic, user, web);
			buffi.append(t);
		}
		buffi.append("</div>");
	}
	

	/**
	 * Renders the Answer Regions for a picture.
	 * The Answer Regions are clickable and visible if
	 * if the checkbox for the answerRegion is checked.
	 * 
	 * @param answerRegion
	 * @param hover
	 * @param kb
	 * @param q
	 * @return
	 * @throws IOException
	 */
	private String renderAnswerRegion(AnswerRegion answerRegion,
				KnowledgeBase kb, Question q, String topic, String user, String web) {

		// Is the Answer in the KnowledgeBase
		Choice answer = kb.searchAnswerChoice(answerRegion.getAnswerID());
		
		if (answer == null) {
			Logger.getLogger(ImageQuestionHandler.class.getName())
				.warning(
					"The Answer ID was not found in the knowledge base : ");
			return "";
		}
		
		String answerID = answerRegion.getAnswerID();

		// Render the Region
		StringBuffer buffi = new StringBuffer();
		
		buffi.append("<a id=\"region_"+ answerID +"\"");

		buffi.append(" class=\"answerRegion");
		// if region is aleady answered --> insert another styleclass
		if (currentAnswerIsSet(answer.getId(), q, topic, user, web)) {
			buffi.append(" answerSet");
		}
//		buffi.append(" qImageHover");
		buffi.append("\"");

		// write style-Attribute
		StringBuffer styleString = new StringBuffer();
//		styleString.append("position: absolute;");
		styleString.append(" top: " + answerRegion.getYStart() + "px;");
		styleString.append(" left: " + answerRegion.getXStart() + "px;");
		styleString.append(" width: " + answerRegion.getWidth() + "px; height: "
				+ answerRegion.getHeight() + "px;");
		buffi.append(" style=\"" + styleString.toString() + "\"");		
		buffi.append(">");

		// insert a transparent gif (so that Internet Explorer can be supported)
		String imageName = answer.getId();
		buffi.append("<img");
		buffi.append(" class=\"qImageHover\" alt=\"space\"");
		buffi.append(" src=\"images/spacer.gif\"");
		buffi.append(" width=\"" + answerRegion.getWidth() + "\"");
		buffi.append(" height=\"" + answerRegion.getHeight() + "\"");
		buffi.append(" title=\"" + imageName + "\" ");
		buffi.append(" id=\"bild_" + answerID + "\" ");
		
		// Add a Semantic Annotation,
		// so the SetSingleFindingAction can be used
		buffi.append(this.buildRelAttributeString(answerID, web, topic, q.getId()));
		
		buffi.append(">");
		buffi.append("</a>");
		
		return buffi.toString();
	}

	/**
	 * Builds a rel-Attribute. Used by {@link SetSingleFindingAction} to set
	 * AnswerChoices.
	 * 
	 * @param answerID
	 * @param web
	 * @param topic
	 * @param id
	 * @return
	 */
	private String buildRelAttributeString(String answerID, String web,
			String topic, String id) {
		StringBuffer relBuffi = new StringBuffer();
		relBuffi.append("rel=\"{oid: '" + answerID + "',");
		relBuffi.append(" web:'" + web + "',");
		relBuffi.append(" ns:'" + topic + ".." + topic + "_KB',"); // + ".." + topic + "_KB
		relBuffi.append(" qid:'" + id + "'}\"");
		return relBuffi.toString();
	}

	/**
	 * Checks if this AnswerChoice is currently set.
	 * Works for QuestionOC and QuestionMC which are used
	 * in ImageQuestions.
	 * 
	 * @param specAns
	 * @param q
	 * @param theCase
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private boolean currentAnswerIsSet(String answerID,
			Question q, String topic, String user, String web) {
		
		Value value = null;
		if (q instanceof QuestionChoice) {
			for (Choice choice : ((QuestionChoice) q).getAllAlternatives()) {
				if (choice.getId().equals(answerID)) {
					value = new ChoiceValue(choice);
					break;
				}
			}
		}
		
		Session session = D3webUtils.getSession(topic, user, web);
		Value answer = session.getBlackboard().getValue(q);
		boolean contains = false;
		if (!(answer instanceof UndefinedValue)) {
			ArrayList<Value> values = (ArrayList<Value>)answer.getValue();
			for (Value val : values) {
				if (val.equals(value)) {
					contains = true;
				}
			}
		}

		return contains;
	}
	
	private void renderQuestionChoiceColumns( StringBuffer buffi, Session session,
			Question q, List<AnswerRegion> answerRegions,
			String topic, String user, String web) {
		
		// new Table for Checkboxes an labels
		buffi.append("<table><tr>");
		
		// TODO: Not everytime 2 columns
		List<Choice> ans = ((QuestionChoice) q).getAllAlternatives();
		List<String> checkBoxes = null;
		
		if (q instanceof QuestionMC)
			checkBoxes = this.renderCheckBoxes(ans, topic, user, web, q, true, session);

		if (q instanceof QuestionOC)
			checkBoxes = this.renderCheckBoxes(ans, topic, user, web, q, false, session);
		
		// Render column 1
		int i = 0;
		buffi.append("<td>");
		buffi.append("<table>");
		for (;i < (checkBoxes.size()/2);i++) {
			buffi.append(checkBoxes.get(i));
		}
		buffi.append("</table>");
		buffi.append("</td>");
		
		// Render column 2
		buffi.append("<td>");
		buffi.append("<table>");
		for (;i < ans.size();i++) {
			buffi.append(checkBoxes.get(i));
		}
		buffi.append("</table>");
		buffi.append("</td>");
		
//		for (int i = startIndex; i < startIndex + qCount; i++) {
//			// rende unknown answer if i is out of answerlist or render
//			// invisible unknown answer...
//			if (i >= ans.size()
//					|| (i == ans.size() - 1 && !isRenderUnknownAnswer(qChoice))) {
//
//				boolean fastAnswer = layoutDef.getFastAnswer();
//				// disallow always: fast answer for MC
//				if (qChoice instanceof QuestionMC) {
//					fastAnswer = false;
//				}
//				renderUnknownRadioButton(writer, component, theCase, qChoice,
//						DialogUtils.unknownAnswerInValueList(qChoice, theCase),
//						fastAnswer);
//				if (i >= ans.size()) {
//					continue;
//				}
//			}
//			writer.startElement("tr", component);
//			writer.startElement("td", component);
//
//			DialogRenderUtils.renderTable(writer, component);
//			writer.startElement("tr", component);
//
//			Choice specAns = ans.get(i);
//
//			// 1. column : Checkbox
//			writer.startElement("td", component);
//			writer.startElement("input", component);
//			writer.writeAttribute("id", specAns.getId(), "id");
//
//			// if AnswerNo OR QuestionOC render radiobutton
//			if (qChoice instanceof QuestionOC || specAns instanceof AnswerNo) {
//				writer.writeAttribute("type", "radio", "type");
//			}
//			else {
//				writer.writeAttribute("type", "checkbox", "type");
//			}
//			writer.writeAttribute("value", specAns.getId(), "value");
//			writer.writeAttribute("name", qChoice.getId(), "name");
//
//			if (currentAnswerIsSet(new ChoiceValue(specAns), qChoice, theCase)) {
//				writer.writeAttribute("checked", "checked", "checked");
//			}
//
//			boolean badAnswer = false;
//			if (qChoice instanceof QuestionOC) {
//				if (layoutDef.getFastAnswer()) {
//					writer.writeAttribute("onclick", "saveLastClickedAnswer('"
//							+ specAns.getId() + "', '" + theCase.getId()
//							+ "'); doSubmit()", "onclick");
//				}
//			}
//			else if (qChoice instanceof QuestionMC) {
//
//				if (DialogUtils.getDialogSettings()
//						.isMCConstraintsAutoGrayOut()) {
//					// check if Question has MC constraints and disable the
//					// button if another answer is already set and this answer
//					// would result in a restricted combination
//					List<List<String>> badanswersLists = (List<List<String>>) qChoice
//							.getProperties().getProperty(
//							Property.MC_CONSTRAINTS);
//
//					Value mcans = theCase.getBlackboard().getValue(qChoice);
//					List<Choice> alreadySetAnsList = new ArrayList<Choice>();
//
//					if (mcans instanceof MultipleChoiceValue) {
//						alreadySetAnsList = ((MultipleChoiceValue) mcans).asChoiceList();
//					}
//
//					// only check if both lists have entries
//					if (badanswersLists != null && badanswersLists.size() > 0
//							&& alreadySetAnsList.size() > 0) {
//						// add the current answerID and the already set
//						// answerIDs in a list and check if this combination is
//						// bad...
//						List<Object> extendedAnswersIDList = new ArrayList<Object>();
//						extendedAnswersIDList.add(specAns.getId());
//						for (Choice a : alreadySetAnsList) {
//							extendedAnswersIDList.add(a.getId());
//						}
//						for (List<String> badansList : badanswersLists) {
//							boolean combinationIsBad = currentAnswersAreBad(
//									extendedAnswersIDList, badansList);
//							if (combinationIsBad) {
//								badAnswer = true;
//								writer.writeAttribute("disabled", "disabled",
//										"disabled");
//								break;
//							}
//						}
//					}
//				}
//
//				// if it is a radiobutton (-> AnswerNo) ...
//				if (specAns instanceof AnswerNo) {
//					writeJsDeselectAllBut(writer, qChoice.getId(),
//							getAnsNoId(ans), layoutDef.getFastAnswer(), theCase);
//				}
//				// if it is a select-button (no AnswerNo)
//				else {
//					writeJsDeselectUnknownAnd(writer, qChoice, specAns,
//							theCase, getAnsNoId(ans),
//							layoutDef.getFastAnswer(), DialogUtils
//							.getDialogSettings()
//							.isMCConstraintsAutoGrayOut());
//				}
//
//			}
//			writer.endElement("input");
//			writer.endElement("td");
//
//			// 2. column: label and text
//			writer.startElement("td", component);
//			writer.startElement("label", component);
//			writer.writeAttribute("for", specAns.getId(), "for");
//			if (badAnswer) {
//				// kind of grayed out
//				writer.writeAttribute("style", "color: #999;", "style");
//			}
//			writer.writeText(specAns.getName(), "value");
//			writer.endElement("label");
//			writer.endElement("td");
			
			// End the table from beginning
		buffi.append("</tr></table>");

	}

	/**
	 * Renders the HTML for a Checkbox.
	 * It is easier to build the table afterwards.
	 * 
	 * @param ans
	 * @param web
	 * @param topic
	 * @param id
	 * @return
	 */
	private List<String> renderCheckBoxes(List<Choice> ans,
			String topic, String user, String web,
			Question q, boolean isCheckBox, Session session) {
		
		// TODO: Check, if it should be checked
		// TODO: Support QuestionOC
		StringBuffer buffi = new StringBuffer();
		ArrayList<String> rendered = new ArrayList<String>();
		String answerID = "";
		String answerName = "";
		for (int i = 0; i < ans.size(); i++) {
			answerID = ans.get(i).getId();
			buffi.append("<tr>");
			buffi.append("<td>");
			buffi.append("<input id=\"box_"+answerID+"\"" +
					this.buildRelAttributeString(answerID, web, topic, q.getId()));
			if (isCheckBox)
				buffi.append("type=\"checkbox\"");
			else
				buffi.append("type=\"radio\"");
			buffi.append(" class=\"answerRegion2\" ");
			buffi.append(this.buildRelAttributeString(
							answerID, web, topic, q.getId()));
			
			// is answer set
//			if (this.currentAnswerIsSet(answerID, q, topic, user, web))
//				buffi.append(" checked");
			Value value = session.getBlackboard().getValue(q);
			if (isAnsweredinCase(value, new ChoiceValue(ans.get(i))))
				buffi.append(" checked");
			
			buffi.append(">");
			buffi.append("</td>");
			
			// Render Label for Checkbox
			answerName = ans.get(i).getName();
			buffi.append("<td>");
			buffi.append("<label for=\"" + answerID +"\">" +
					answerName + "</label>");
			buffi.append("</td>");
			buffi.append("</tr>");
			rendered.add(buffi.toString());
			buffi = new StringBuffer();
		}
		
		return rendered;
	}
	
	private static boolean isAnsweredinCase(Value sessionValue, Value value) {
		// test for MC values separately
		if (sessionValue instanceof MultipleChoiceValue) {
			return ((MultipleChoiceValue) sessionValue).contains(value);
		}
		else {
			return sessionValue.equals(value);
		}
	}
}
