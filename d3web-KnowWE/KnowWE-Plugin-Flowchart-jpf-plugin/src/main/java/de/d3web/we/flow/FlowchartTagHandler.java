package de.d3web.we.flow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.d3web.core.session.CaseObjectSource;
import de.d3web.core.session.Session;
import de.d3web.diaFlux.flow.DiaFluxCaseObject;
import de.d3web.diaFlux.flow.EdgeSupport;
import de.d3web.diaFlux.flow.FlowSet;
import de.d3web.diaFlux.flow.IEdge;
import de.d3web.diaFlux.flow.INode;
import de.d3web.diaFlux.flow.INodeData;
import de.d3web.diaFlux.flow.ISupport;
import de.d3web.diaFlux.inference.DiaFluxUtils;
import de.d3web.diaFlux.inference.Entry;
import de.d3web.diaFlux.inference.Path;
import de.d3web.we.core.KnowWEArticleManager;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.flow.type.FlowchartType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.xml.AbstractXMLObjectType;
import de.d3web.we.logging.Logging;
import de.d3web.we.taghandler.AbstractTagHandler;
import de.d3web.we.utils.D3webUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;

/**
 * [{KnowWEPlugin Flowchart}]
 * 
 * @author Florian Ziegler
 */
public class FlowchartTagHandler extends AbstractTagHandler {

	public FlowchartTagHandler() {
		super("flowchart");
		Logging.getInstance().addHandlerToLogger(
				Logging.getInstance().getLogger(), "flowchartTagHandler.txt");
	}

	@Override
	public String render(String topic, KnowWEUserContext user,
			Map<String, String> values, String web) {
		
		Session theCase = D3webUtils.getSession(topic, user, web);

		if (!DiaFluxUtils.isFlowCase(theCase)) {
			return "No Flowchart found.";
		}

		KnowWEArticleManager artManager = KnowWEEnvironment.getInstance().getArticleManager(web);
		KnowWEArticle article = artManager.getArticle(topic);

		List<Section<FlowchartType>> flows = new ArrayList<Section<FlowchartType>>();

		article.getSection().findSuccessorsOfType(FlowchartType.class, flows);

		StringBuilder builder = new StringBuilder();

		if (flows.isEmpty()) {
			builder.append("No Flowcharts found in KB.");
		}

		// Debug
		if (isDebug(user.getUrlParameterMap())) builder.append(getPathendText(theCase));
		//

		for (Section<FlowchartType> section : flows) {

			Map<String, String> attributeMap = AbstractXMLObjectType.getAttributeMapFor(section);
			String name = attributeMap.get("name");

			builder.append("<div>");
			builder.append("<h3>");
			builder.append("Diagnostic Flow '");
			builder.append(name);
			builder.append("'");
			builder.append("</h3>");

			if (isActive(section, theCase)) {
				builder.append(createPreviewWithHighlightedPath(section, theCase));
			}

			builder.append("</div>");
			builder.append("<p/><p/>");

		}

		return builder.toString();
	}

	private boolean isActive(Section section, Session theCase) {

		// TODO
		// String flowID =
		// AbstractXMLObjectType.getAttributeMapFor(section).get("id");
		//		
		// CaseObjectSource flowSet = FluxSolver.getFlowSet(theCase);
		//		
		// DiaFluxCaseObject caseObject = (DiaFluxCaseObject)
		// theCase.getCaseObject(flowSet);
		//       
		return true;
	}

	private String getPathendText(Session theCase) {

		if (theCase == null) return "";

		FlowSet set = DiaFluxUtils.getFlowSet(theCase);

		DiaFluxCaseObject caseObject = (DiaFluxCaseObject) theCase.getCaseObject(set);
		Collection<Path> pathes = caseObject.getPathes();

		StringBuilder builder = new StringBuilder();

		builder.append("<b>Current Pathes:</b>");
		builder.append("<br/>");
		builder.append(pathes);
		builder.append("<br/>");
		builder.append("<br/>");

		int i = 0;
		
		for (Path path : pathes) {
			
			Iterator<? extends Entry> it = path.iterator();
			builder.append(++i + ". Path:<br/>");
			
			while (it.hasNext()) {
				Entry entry = it.next();
				builder.append(entry);
				builder.append("<br/>");
				
			}
			builder.append("<br/>");
			builder.append("<br/>");
			
		}


		builder.append("<br/>");

		return builder.toString();
	}

	private String createPreviewWithHighlightedPath(Section section, Session session) {
		
		String preview = FlowchartUtils.extractPreview(section);
		
		if (session == null)
			return preview;
		
		String flowID = AbstractXMLObjectType.getAttributeMapFor(section).get("fcid");
		
		CaseObjectSource flowSet = DiaFluxUtils.getFlowSet(session);
		
		DiaFluxCaseObject caseObject = (DiaFluxCaseObject) session.getCaseObject(flowSet);
       
        
		for (Path path : caseObject.getPathes()) {

			preview = highlightPath(preview, flowID, path, session);

		}

		return FlowchartUtils.createPreview(preview);
	}

	private String highlightPath(String preview, String flowID, Path path, Session session) {
		// get all the nodes
		String[] nodes = preview.split("<DIV class=\"Node\" id=\"");
		String[] edges = preview.split("<DIV class=\"Rule\" id=\"");

		Iterator<? extends Entry> it = path.iterator();
		
		while (it.hasNext()) {
			
			Entry pathEntry = it.next();

			INode node = pathEntry.getNode();

			//TODO 
			if (!node.getFlow().getId().equals(flowID)) return preview;

			String nodeId = node.getID();
			for (int i = 1; i < nodes.length; i++) {
				if (nodes[i].contains(nodeId + "\"")) {
					preview = colorNode(nodes[i], preview);
				}
			}

			INodeData nodeData = DiaFluxUtils.getNodeData(node, session);
			
			List<ISupport> supports = nodeData.getSupports();
			for (ISupport support : supports) {
				
				if ((support instanceof EdgeSupport)) {
					
					IEdge edge = ((EdgeSupport) support).getEdge();
					String edgeId = edge.getID();
					
					for (int i = 0; i < edges.length; i++) {
						if (edges[i].contains(edgeId + "\"")) {
							preview = colorEdge(edges[i], preview);
						}
					}
				}
			}

		}
		return preview;
	}

	private String colorNode(String node, String preview) {

		// is node in current flowchart?
		// TODO as FC change along PathEntries, the node might not be in the
		// current FC
		int nodeIndex = preview.indexOf(node);

		if (nodeIndex == -1) return preview;

		// if yes, add the additional class
		String inputHelper1 = preview.substring(0, nodeIndex - 6);
		String inputHelper2 = preview.substring(preview.indexOf(node));
		preview = inputHelper1 + " added" + "\" id=\"" + inputHelper2;

		return preview;
	}

	private String colorEdge(String edge, String preview) {
		// set the additional class of the yet to be colored nodes
		String alteration = "added";

		String temp = preview;

		String[] parts = edge.split("<DIV class=\"");
		for (String s : parts) {
			String type = s.substring(0, s.indexOf("\""));

			// for simple lines
			if (type.equals("h_line") || type.equals("v_line") || type.equals("no_arrow")) {
				String inputHelper1 = temp.substring(0, temp.indexOf(s));
				String inputHelper2 = temp.substring(temp.indexOf(s));
				temp = inputHelper1 + alteration + "\" id=\"" + inputHelper2;

				// for arrows
			}
			else if (type.equals("arrow_up") || type.equals("arrow_down")
					|| type.equals("arrow_left") || type.equals("arrow_right")) {
				int size = type.length();
				String arrowAlteration = "_" + alteration;
				String inputHelper1 = temp.substring(0, temp.indexOf(s) + size);
				String inputHelper2 = temp.substring(temp.indexOf(s));
				temp = inputHelper1 + arrowAlteration + "\" id=\"" + inputHelper2;

				// for the rest
			}
			else if (type.equals("GuardPane") || type.equals("value")) {
				// Logging.getInstance().log(Level.INFO, "type: " + type);
				String inputHelper1 = temp.substring(0, temp.indexOf(s));
				String inputHelper2 = temp.substring(temp.indexOf(s));
				temp = inputHelper1 + alteration + "Text\" id=\"" + inputHelper2;
			}
		}

		return temp;
	}

	private boolean isDebug(Map<String, String> urlParameterMap) {
		String debug = urlParameterMap.get("debug");
		return debug != null && debug.equals("true");
	}

}
