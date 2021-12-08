package de.d3web.we.flow.kbinfo;

import java.util.LinkedList;
import java.util.List;

import de.d3web.kernel.domainModel.DerivationType;
import de.d3web.kernel.domainModel.Diagnosis;
import de.d3web.kernel.domainModel.IDObject;
import de.d3web.kernel.domainModel.KnowledgeBase;
import de.d3web.kernel.domainModel.NamedObject;
import de.d3web.kernel.domainModel.answers.AnswerChoice;
import de.d3web.kernel.domainModel.qasets.QContainer;
import de.d3web.kernel.domainModel.qasets.Question;
import de.d3web.kernel.domainModel.qasets.QuestionChoice;
import de.d3web.kernel.domainModel.qasets.QuestionDate;
import de.d3web.kernel.domainModel.qasets.QuestionMC;
import de.d3web.kernel.domainModel.qasets.QuestionNum;
import de.d3web.kernel.domainModel.qasets.QuestionOC;
import de.d3web.kernel.domainModel.qasets.QuestionText;
import de.d3web.kernel.domainModel.qasets.QuestionYN;
import de.d3web.we.action.KnowWEAction;
import de.d3web.we.core.DPSEnvironment;
import de.d3web.we.core.KnowWEParameterMap;
import de.d3web.we.core.knowledgeService.D3webKnowledgeService;
import de.d3web.we.core.knowledgeService.KnowledgeService;
import de.d3web.we.d3webModule.DPSEnvironmentManager;
import de.d3web.we.flow.FlowchartSection;
import de.d3web.we.kdom.Section;

public class GetInfoObjects implements KnowWEAction {

	public GetInfoObjects() {
	}
	
	@Override
	public String perform(KnowWEParameterMap parameterMap) {
		/** 
		 * KnowWEEnvironment:
		 * Allgemeines Semantic Wiki auf OWL als Repr�sentation KDOM Engine.
		 * Verwaltet alle technischen Aspekte der Umgebung (z.B. Plugins).
		 * Dieser hat noch nichts mit d3web zu tun.
		 * 
		 * ArticeManager:
		 * Verwaltung der Wiki-Artikel. F�r jedes Web kann es einen 
		 * Article-Manager geben. Dieser kann vom KnowWEEnvironment
		 * angefragt werden. Dieser hat noch nichts mit d3web zu tun.
		 * Ein Article besteht im wesentlichen aus dem KDOM-Baum
		 * 
		 * KDOM:
		 * Baumstruktur des Wiki-Textes ohne Semantik
		 * 
		 * WebEnvironmentManager:
		 * Verwaltet die DSPEnvironments, eines f�r jedes Web des Wiki.
		 * 
		 * DSPEnvironment:
		 * Verwaltung f�r diagnostischen Probleml�sungsservices, f�r alle 
		 * Wissensbasen dieses Webs (z.B. unter anderem d3web-Services 
		 * (D3webKnowledgeService)).  Ein Service entspricht einer Wissensbasis 
		 * der jeweiligen Engine (z.B. unter anderem d3web).
		 * 
		 * KnowledgeService 
		 * Service f�r eine Wissensbasis f�r eine Wiki-Seite. Der Zugriff erfolgt
		 * �ber das DSPEnvironment�ber eine Wissensbasis-ID, die aktuell aber 
		 * eindeutig aus dem Wiki-Seiten-Namen erzeugt wird). Das bedeutet, f�r jede 
		 * Wiki-Seite gibt es aktuell genau (maximal) eine Wissenbasis f�r genau 
		 * (maximal) eine Engine.
		 * 
		 * D3webKnowledgeService:
		 * Implementierung des KnowledgeServices f�r d3web. Hier�ber erh�lt man
		 * Zugriff auf die d3web Wissensbasis der jeweiligen Wiki-Seite.
		 */

		// prepare parameters
		String ids = parameterMap.get("ids");
		if (ids == null || ids.isEmpty()) {
			return "<kbinfo></kbinfo>";
		}
		
		// prepare the buffer for the result
		StringBuffer buffer = new StringBuffer();
		appendHeader(parameterMap, buffer);
		
		// iterate through the requested Objects
		String[] idArray = ids.split(",");
		for (int i=0; i<idArray.length; i++) {
			String id = idArray[i];
			appendInfoObject(parameterMap, id, buffer);
		}

		// finish result
		appendFooter(parameterMap, buffer);
		return buffer.toString();		
	}
	
	public void appendHeader(KnowWEParameterMap parameterMap, StringBuffer buffer) {
		buffer.append("<kbinfo>\n");		
	}
	
	public void appendFooter(KnowWEParameterMap parameterMap, StringBuffer buffer) {
		buffer.append("</kbinfo>");		
	}
	
	public void appendInfoObject(KnowWEParameterMap parameterMap, String id, StringBuffer buffer) {
		int pos = id.lastIndexOf("/");
		String serviceID = (pos == -1) ? id : id.substring(0, pos);
		String objectID = (pos == -1) ? null : id.substring(pos + 1);

		String web = parameterMap.getWeb();
		DPSEnvironment env = DPSEnvironmentManager.getInstance().getEnvironments(web);
		KnowledgeService service = env.getService(serviceID);
		
		if (service == null) {
			// it is possible (and allowed) that an article has no KnowledgeService
			// but (!): we do only list those having articles!
			// TODO: log a warning here
		}
		else if (objectID == null) {
			// we want to have the article itself
			appendInfoObject(web, service, buffer);
		}
		else if (service instanceof D3webKnowledgeService) {
			// look for an object inside the knowledgebase
			appendInfoObject(web, (D3webKnowledgeService) service, objectID, buffer);
		}
	}
	
	private void appendInfoObject(String web, KnowledgeService service, StringBuffer buffer) {
		String id = service.getId();
		String name = id.substring(0, id.indexOf(".."));
		buffer.append("\t<article");
		buffer.append(" id='").append(id).append("'");
		buffer.append(" name='").append(name).append("'");
		buffer.append(">");
		// children of an article are all Solutions of P000 and all QSets of Q000
		// as well as the flowcharts
		if (service instanceof D3webKnowledgeService) {
			D3webKnowledgeService d3Service = (D3webKnowledgeService)service;
			KnowledgeBase base = d3Service.getBase();
			List<NamedObject> qsets = new LinkedList<NamedObject>();
			for (NamedObject object : base.getRootQASet().getChildren()) {
				// avoid top level questions ==> implicit imports
				if (!(object instanceof Question)) {
					qsets.add(object);
				}
			}
			appendChilds(web, d3Service, qsets, buffer);
			appendChilds(web, d3Service, base.getRootDiagnosis(), buffer);
			// TODO: append flowcharts out of knowledge base here
			List<Section> flowcharts = ManagerUtils.getFlowcharts(web, d3Service);
			for (Section flowchart : flowcharts) {
				FlowchartSection type = (FlowchartSection) flowchart.getObjectType();
				buffer.append("\t\t<child>");
				buffer.append(service.getId() + "/" + type.getFlowchartID(flowchart));
				buffer.append("</child>\n");
			}
		}
		buffer.append("\t</article>\n");
	}
	
	private void appendInfoObject(String web, D3webKnowledgeService service, String objectID, StringBuffer buffer) {
		KnowledgeBase base = service.getBase();
		IDObject object = base.search(objectID);
		
		if (object instanceof Diagnosis) {
			appendInfoObject(web, service, (Diagnosis) object, buffer);
		}
		else if (object instanceof Question) {
			appendInfoObject(web, service, (Question) object, buffer);
		}
		else if (object instanceof QContainer) {
			appendInfoObject(web, service, (QContainer) object, buffer);
		}
		else {
			// if not inside knowledge base
			// look for a flowchart th the article
			List<Section> flowcharts = ManagerUtils.getFlowcharts(web, service);
			for (Section flowchart : flowcharts) {
				FlowchartSection type = (FlowchartSection) flowchart.getObjectType();
				String id = type.getFlowchartID(flowchart);
				if (id.equalsIgnoreCase(objectID)) {
					appendInfoObject(web, service, flowchart, buffer);
					return;
				}
			}
			buffer.append("<unknown id='"+objectID+"'></unknown>");
		}
	}

	private void appendInfoObject(String web, D3webKnowledgeService service, Diagnosis object, StringBuffer buffer) {
		buffer.append("\t<solution");
		buffer.append(" id='").append(service.getId()).append("/").append(object.getId()).append("'");
		buffer.append(" name='").append(encodeXML(object.getText())).append("'");
		buffer.append(">\n");
		appendChilds(web, service, object, buffer);
		buffer.append("\t</solution>\n");
	}
	
	private void appendInfoObject(String web, D3webKnowledgeService service, Question object, StringBuffer buffer) {
		buffer.append("\t<question");
		buffer.append(" id='").append(service.getId()).append("/").append(object.getId()).append("'");
		buffer.append(" name='").append(encodeXML(object.getText())).append("'");
		if (object.getDerivationType() == DerivationType.DERIVED) {
			buffer.append(" abstract='true'");
		}
		buffer.append(" type='");
		buffer.append(
				(object instanceof QuestionYN) ? "bool" :
					(object instanceof QuestionOC) ? "oc" :
						(object instanceof QuestionMC) ? "mc" :
							(object instanceof QuestionDate) ? "date" :
								(object instanceof QuestionNum) ? "num" :
									(object instanceof QuestionText) ? "text" :
										"???"
										);
		buffer.append("'");
		buffer.append(">\n");
		appendChilds(web, service, object, buffer);
		if (object instanceof QuestionChoice) {
			// TODO: choices are not considered in JS implementation of bool questions
			for (AnswerChoice answer : ((QuestionChoice) object).getAllAlternatives()) {
				buffer.append("\t\t<choice>").append(encodeXML(answer.getText())).append("</choice>\n");
			}
		}
		else if (object instanceof QuestionNum) {
			// TODO: access range and append "<range min='???' max='???'></range>"
		}
		buffer.append("\t</question>\n");
	}
	
	private void appendInfoObject(String web, D3webKnowledgeService service, QContainer object, StringBuffer buffer) {
		buffer.append("\t<qset");
		buffer.append(" id='").append(service.getId()).append("/").append(object.getId()).append("'");
		buffer.append(" name='").append(encodeXML(object.getText())).append("'");
		buffer.append(">\n");
		appendChilds(web, service, object, buffer);
		buffer.append("\t</qset>\n");
	}

	
	private void appendInfoObject(String web, D3webKnowledgeService service, Section flowchart, StringBuffer buffer) {
		FlowchartSection type = (FlowchartSection) flowchart.getObjectType();
		String name = type.getFlowchartName(flowchart);
		String id = type.getFlowchartID(flowchart);
		String[] startNames = type.getStartNames(flowchart);
		String[] exitNames = type.getExitNames(flowchart);

		buffer.append("\t<flowchart");
		buffer.append(" id='").append(service.getId()).append("/").append(id).append("'");
		buffer.append(" name='").append(encodeXML(name)).append("'");
		buffer.append(">\n");
		for (int i=0; i<startNames.length; i++) {
			buffer.append("\t\t<start>").append(encodeXML(startNames[i])).append("</start>\n");
		}
		for (int i=0; i<exitNames.length; i++) {
			buffer.append("\t\t<exit>").append(encodeXML(exitNames[i])).append("</exit>\n");
		}
		buffer.append("\t</flowchart>\n");
	}
	
	private void appendChilds(String web, D3webKnowledgeService service, NamedObject object, StringBuffer buffer) {
		appendChilds(web, service, object.getChildren(), buffer);
	}
	
	private void appendChilds(String web, D3webKnowledgeService service, List<? extends NamedObject> childs, StringBuffer buffer) {
		for (NamedObject child : childs) {
			buffer.append("\t\t<child>");
			buffer.append(service.getId() + "/" + child.getId());
			buffer.append("</child>\n");
		}
	}
	
	private String encodeXML(String text) {
		StringBuffer buffer = new StringBuffer();
		for (int i=0; i<text.length(); i++) {
			char c = text.charAt(i);
			if (c == ' ' || Character.isLetterOrDigit(c)) {
				buffer.append(c);
			}
			else {
				int code = (int) c;
				buffer.append("&#").append(code).append(";");
			}
		}
		return buffer.toString();
	}
}
