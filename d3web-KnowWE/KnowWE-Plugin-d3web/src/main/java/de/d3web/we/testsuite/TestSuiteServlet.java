package de.d3web.we.testsuite;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import de.d3web.empiricalTesting.TestSuite;
import de.d3web.empiricalTesting.caseConverter.CaseObjectToKnOffice;
import de.d3web.empiricalTesting.caseConverter.CaseObjectToTestSuiteXML;
import de.d3web.empiricalTesting.caseVisualization.dot.DDBuilder;
import de.d3web.we.action.AbstractAction;
import de.d3web.we.action.ActionContext;
import de.d3web.we.core.KnowWEAttributes;
import de.d3web.we.utils.KnowWEUtils;

public class TestSuiteServlet extends AbstractAction {
	
	private static final long serialVersionUID = 1L;

	@Override
	public void execute(ActionContext context) throws IOException {
		String filename = context.getParameter("filename");;
		String nodeID = context.getParameter("nodeID");
		String topic = context.getParameter(KnowWEAttributes.TOPIC);
		String web = context.getParameter("web");
		String type = context.getParameter("type");
		
		// Load the TestSuite
		TestSuite t = (TestSuite) KnowWEUtils.getStoredObject(web, topic, nodeID, TestsuiteSection.TESTSUITEKEY);
				
		if (type.equals("visualization"))
			generateVisualization(context, t, filename);
		else if (type.equals("case"))
			generateCaseFile(context, t, filename);
		
	}
	

	private void generateVisualization(ActionContext context, TestSuite t, String filename) throws IOException {
			
		if (filename.endsWith(".dot")) {
			
			//Get the file content
			ByteArrayOutputStream bstream = DDBuilder.getInstance().getByteArrayOutputStream(t.getRepository());
			
			//Response
			context.setContentType("text/x-graphviz");
			context.setHeader("Content-Disposition", "attachment;filename=\""+filename+"\"");
			context.setContentLength(bstream.size());
			
			//Write the data from the ByteArray to the ServletOutputStream of the response
			bstream.writeTo(context.getOutputStream());

		} else if (filename.endsWith(".pdf")) {
			
			//Get the file content
			ByteArrayOutputStream bstream = de.d3web.empiricalTesting.caseVisualization.jung.JUNGCaseVisualizer.getInstance().getByteArrayOutputStream(t.getRepository());
			
			//Response
			context.setContentType("application/pdf");
			context.setHeader("Content-Disposition", "attachment;filename=\""+filename+"\"");
			context.setContentLength(bstream.size());
			
			//Write the data from the ByteArray to the ServletOutputStream of the response
			bstream.writeTo(context.getOutputStream());
			
		}
		
	}
	
	
	private void generateCaseFile(ActionContext context, TestSuite t, String filename) throws IOException {
				
		if (filename.endsWith(".txt")) {
			
			//Get the file content
			CaseObjectToKnOffice c = new CaseObjectToKnOffice();
			ByteArrayOutputStream bstream = c.getByteArrayOutputStream(t.getRepository());
			
			//Response
			context.setContentType("text/plain");
			context.setHeader("Content-Disposition", "attachment;filename=\""+filename+"\"");
			context.setContentLength(bstream.size());
			
			//Write the data from the ByteArray to the ServletOutputStream of the response
			bstream.writeTo(context.getOutputStream());

		} else if (filename.endsWith(".xml")) {
			
			//Get the file content
			CaseObjectToTestSuiteXML c = new CaseObjectToTestSuiteXML();
			ByteArrayOutputStream bstream = c.getByteArrayOutputStream(t.getRepository());
			
			//Response
			context.setContentType("text/xml");
			context.setHeader("Content-Disposition", "attachment;filename=\""+filename+"\"");
			context.setContentLength(bstream.size());
			
			//Write the data from the ByteArray to the ServletOutputStream of the response
			bstream.writeTo(context.getOutputStream());
			
		}
		
	}
	
}
