/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
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

package de.d3web.we.testsuite;

import java.io.Reader;
import java.io.StringReader;
import java.util.Collection;
import java.util.List;

import de.d3web.KnOfficeParser.SingleKBMIDObjectManager;
import de.d3web.core.manage.IDObjectManagement;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.report.Message;
import de.d3web.report.Report;
import de.d3web.we.core.KnowWEParseResult;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.kopic.AbstractKopicSection;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.terminology.D3webSubtreeHandler;
import de.d3web.we.utils.KnowWEUtils;

/**
 * Testsuite Section
 * 
 * @author Sebastian Furth
 * @see TestsuiteBuilder
 */
public class TestsuiteSection extends AbstractKopicSection {

	public static final String TAG = "Testsuite-section";
	public static final String TESTSUITEKEY = "TestsuiteSection_Testsuite";

	public TestsuiteSection() {
		super(TAG);
	}

	@Override
	protected void init() {
		childrenTypes.add(new TestsuiteContent());
		this.addSubtreeHandler(new TestsuiteSectionSubTreeHandler());
		setCustomRenderer(new TestsuiteSectionRenderer());
	}

	// @Override
	// public KnowWEDomRenderer getRenderer() {
	// return new TestsuiteSectionRenderer();
	// }

	private class TestsuiteSectionSubTreeHandler extends D3webSubtreeHandler {

		@Override
		public Collection<KDOMReportMessage> create(KnowWEArticle article, Section s) {

			KnowledgeBaseManagement kbm = getKBM(article);

			if (kbm != null) {

				Section content = ((AbstractKopicSection) s.getObjectType()).getContentChild(s);

				if (content != null) {

					Reader r = new StringReader(content.getOriginalText());
					IDObjectManagement idom = new SingleKBMIDObjectManager(kbm);
					TestsuiteBuilder builder = new TestsuiteBuilder("", idom);

					// Parsing
					List<de.d3web.report.Message> messages = builder.addKnowledge(r, idom, null);

					// Reporting
					storeMessages(article, s, this.getClass(), messages);
					Report testsuiteRep = new Report();

					for (Message messageKnOffice : messages) {
						testsuiteRep.add(messageKnOffice);
					}

					KnowWEParseResult result =
							new KnowWEParseResult(testsuiteRep, s.getTitle(), s.getOriginalText());

					s.getArticle().getReport().addReport(result);

					// Store Testsuite
					KnowWEUtils.storeSectionInfo(s.getArticle().getWeb(), s.getTitle(), s.getID(),
							TestsuiteSection.TESTSUITEKEY, builder.getTestsuite());
				}
			}

			return null;
		}

	}

}
