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

package de.d3web.we.ci4ke.build;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;

import de.d3web.we.ci4ke.handling.CITestResult;
import de.d3web.we.ci4ke.handling.CITestResult.TestResultType;
import de.d3web.we.ci4ke.util.CIUtilities;

public class CIBuildPersistenceHandler {
	
	/**
	 * This File is pointing to our build File
	 */	
	private File xmlBuildFile;
	
	/**
	 * The JDOM Document Tree of our build File
	 */
	private Document xmlJDomTree;
	
	/**
	 * The next build number
	 */
	private long nextBuildNumber;
	
	/**
	 * A Date formatter ;-)
	 */
	private static SimpleDateFormat DATE_FORMAT = 
		new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
	
	/**
	 * Creates a new CI-Build Result-Writer for a CIDashboard 
	 * @param dashboardID
	 */
	public CIBuildPersistenceHandler(String dashboardID) {
		try {
			this.xmlBuildFile = initXMLFile(dashboardID);
			this.xmlJDomTree  = new SAXBuilder().build(xmlBuildFile);
			this.nextBuildNumber = getCurrentBuildNumber() + 1;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static File initXMLFile(String dashboardID) throws IOException{
		if(dashboardID==null || dashboardID.isEmpty())
			throw new IllegalArgumentException(
					"Parameter 'dashboardID' is null or empty!");
		
		File buildFile = new File(CIUtilities.getCIBuildDir(), 
				"builds-" + dashboardID + ".xml");
		
		if(!buildFile.exists()) {
			buildFile.createNewFile();
			writeBasicXMLStructure(buildFile);
		}
		return buildFile;
	}
	
	private static void writeBasicXMLStructure(File xmlFile) throws IOException {
		Element root = new Element("builds");
		root.setAttribute("monitoredArticle", "");//stub
		//create the JDOM Tree for the new xml file and print it out
		Document xmlDocument = new Document(root);
		XMLOutputter out = new XMLOutputter( Format.getPrettyFormat() );
		out.output(xmlDocument, new FileWriter(xmlFile));		
	}
	
	private long getCurrentBuildNumber() throws JDOMException {
		
		long longBuildNum = 0;
		//try to parse the most current build NR
		Object o = XPath.selectSingleNode(xmlJDomTree, 
				"/builds/build[last()]/@nr");
		if(o instanceof Attribute){
			Attribute attr = (Attribute)o;
			String attrValue = attr.getValue();
			if(attrValue!=null && !attrValue.isEmpty())
				longBuildNum = Long.parseLong(attrValue);
		}
		return longBuildNum;
	}
	
	/**
	 * Writes a test-resultset to the XML Build-File
	 * @param resultset
	 */
	public void write(CIBuildResultset resultset, String monitoredArticleTitle){
		
		try {
			Document xmlDocument = new SAXBuilder().build(xmlBuildFile);
			xmlDocument.getRootElement().setAttribute(
					"monitoredArticle", monitoredArticleTitle);
			//Start building the new <build>...</build> element
			Element build = new Element("build");
			build.setAttribute("executed",DATE_FORMAT.format(
					resultset.getBuildExecutionDate()));
			build.setAttribute("nr", String.valueOf(nextBuildNumber));
			build.setAttribute("articleVersion",String.
					valueOf(resultset.getArticleVersion()));
			nextBuildNumber++;
			
			//find the "worst" testResult
			//which defines the overall result of this build
			TestResultType overallResult = overallResult(resultset);
			build.setAttribute(CIBuilder.BUILD_RESULT, overallResult.name());
			xmlDocument.getRootElement().setAttribute(CIBuilder.
					ACTUAL_BUILD_STATUS, overallResult.toString());		
			
			//iterate over the testresults contained in the build-resultset
			for(Map.Entry<String, CITestResult> entry : 
					resultset.getResults().entrySet()) {
				String testname = entry.getKey();
				CITestResult testresult = entry.getValue();
				
				Element e = new Element("test");
				e.setAttribute("name", testname);
				e.setAttribute("result", testresult.getResultType().toString());
				
				if(testresult.getTestResultMessage().length()>0)
					e.setAttribute("message",testresult.getTestResultMessage());
				build.addContent(e);
			}
			//add the build-element to the JDOM Tree
			xmlDocument.getRootElement().addContent(build);
			//and print it to file
			XMLOutputter out = new XMLOutputter( Format.getPrettyFormat() );
			out.output(xmlDocument, new FileWriter(xmlBuildFile));		
			
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Selects some elements in this XML Build Tree
	 * @param xpath
	 * @return
	 */
	public List<?> selectNodes(String xpath) {
		List<?> ret = null;
		try {
			ret = XPath.selectNodes(xmlJDomTree, xpath);
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;
	}
	
	/**
	 * Selects one single node (element or attribute) in this XML Build Tree
	 * @param xpath
	 * @return
	 */
	public Object selectSingleNode(String xpath) {
		Object ret = null;
		try {
			ret = XPath.selectSingleNode(xmlJDomTree, xpath);
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;
	}	
	
	private TestResultType overallResult(CIBuildResultset resultset) {
		
		TestResultType overallResult = TestResultType.SUCCESSFUL;
		Collection<CITestResult> results = resultset.getResults().values();
		
		for(CITestResult result : results) {
			if(result.getResultType().compareTo(overallResult) > 0) {
				overallResult = result.getResultType();
			}
		}
		return overallResult;
	}
}
