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

package de.d3web.we.kdom.dashTree.solutions;

import java.io.StringReader;
import java.util.List;

import de.d3web.KnOfficeParser.SingleKBMIDObjectManager;
import de.d3web.KnOfficeParser.dashtree.SolutionsBuilder;
import de.d3web.kernel.domainModel.KnowledgeBaseManagement;
import de.d3web.report.Message;
import de.d3web.report.Report;
import de.d3web.we.core.KnowWEParseResult;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.kopic.AbstractKopicSection;
import de.d3web.we.terminology.D3webReviseSubTreeHandler;

public class SolutionsSection extends AbstractKopicSection {

	public static final String TAG = "Solutions-section";
	
	public SolutionsSection() {
		super(TAG);
	}
	
	@Override
	protected void init() {
		childrenTypes.add(new SolutionsContent());
		subtreeHandler.add(new SolutionsSubTreeHandler());
	}
	
	private class SolutionsSubTreeHandler extends D3webReviseSubTreeHandler {
	
		@Override
		public void reviseSubtree(Section s) {
	
			KnowledgeBaseManagement kbm = getKBM(s);
			
			if (kbm != null) {
	
				Section content = ((AbstractKopicSection) s.getObjectType()).getContentChild(s);
	
				if (content != null) {
	
					List<de.d3web.report.Message> messages = SolutionsBuilder
							.parse(new StringReader(removeIncludedFromTags(content.getOriginalText())), kbm, new SingleKBMIDObjectManager(kbm));
	
					
					storeMessages(s,messages);
					
					
					Report ruleRep = new Report();
					for (Message messageKnOffice : messages) {
						ruleRep.add(messageKnOffice);
					}
					KnowWEParseResult result = new KnowWEParseResult(ruleRep, s
							.getTitle(), s.getOriginalText());
					s.getArticle().getReport().addReport(result);
				}
			}
		}
	}
	
//	public void reviseSubtreeOld(Section s, KnowledgeRepresentationManager tm, String web,
//			KnowWEDomParseReport rep) {
//
//		KnowledgeRepresentationHandler handler = tm.getHandler("d3web");
//		if (handler instanceof D3webTerminologyHandler) {
//			KnowledgeBaseManagement kbm = ((D3webTerminologyHandler) handler)
//					.getKBM(s.getTopic());
//			Reader r = new StringReader(removeIncludedFromTags(s.getOriginalText()));
//			de.d3web.textParser.cocor.diagnosisHierarchyParser.Scanner dhs = new de.d3web.textParser.cocor.diagnosisHierarchyParser.Scanner(
//					r);
//			de.d3web.textParser.cocor.diagnosisHierarchyParser.Parser dhp = new de.d3web.textParser.cocor.diagnosisHierarchyParser.Parser(
//					dhs, kbm.getKnowledgeBase(), false);
//			dhp.Parse();
//			List<Message> l = dhp.getErrorMessages();
//
//			rep.addReport(new KnowWEParseResult(new Report(l), s.getTopic(), 
//					removeIncludedFromTags(s.getOriginalText())));
//
//		}
//	}


}
