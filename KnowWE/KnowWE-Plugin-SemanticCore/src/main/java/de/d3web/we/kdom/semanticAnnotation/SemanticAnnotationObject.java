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

/**
 * 
 */
package de.d3web.we.kdom.semanticAnnotation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryException;

import de.d3web.we.core.semantic.IntermediateOwlObject;
import de.d3web.we.core.semantic.OwlHelper;
import de.d3web.we.core.semantic.OwlSubtreeHandler;
import de.d3web.we.core.semantic.PropertyManager;
import de.d3web.we.core.semantic.SemanticCoreDelegator;
import de.d3web.we.core.semantic.UpperOntology;
import de.knowwe.core.contexts.ContextManager;
import de.knowwe.core.contexts.DefaultSubjectContext;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.KnowWEArticle;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.kdom.sectionFinder.AllBeforeTypeSectionFinder;

/**
 * @author kazamatzuri
 * 
 */
public class SemanticAnnotationObject extends AbstractType {

	@Override
	public void init() {
		SemanticAnnotationProperty propType = new SemanticAnnotationProperty();
		SemanticAnnotationSubject subject = new SemanticAnnotationSubject();
		subject.setSectionFinder(new AllBeforeTypeSectionFinder(propType));
		this.childrenTypes.add(propType);
		this.childrenTypes.add(subject);
		this.childrenTypes.add(new SimpleAnnotation());
		this.sectionFinder = new AllTextFinderTrimmed();
		this.addSubtreeHandler(new SemanticAnnotationObjectSubTreeHandler());
	}

	@Override
	public String getName() {
		return this.getClass().getName();
	}

	private class SemanticAnnotationObjectSubTreeHandler extends
			OwlSubtreeHandler<SemanticAnnotationObject> {

		@Override
		public Collection<Message> create(KnowWEArticle article, Section s) {
			List<Message> msgs = new ArrayList<Message>();
			UpperOntology uo = UpperOntology.getInstance();
			IntermediateOwlObject io = new IntermediateOwlObject();
			List<Section> childs = s.getChildren();
			URI prop = null;
			URI stringa = null;
			URI soluri = null;
			boolean erronousproperty = false;
			String badprop = "";
			for (Section cur : childs) {
				if (cur.get().getClass().equals(
						SemanticAnnotationProperty.class)) {
					IntermediateOwlObject tempio = (IntermediateOwlObject) KnowWEUtils
							.getStoredObject(article, cur, OwlHelper.IOO);
					prop = tempio.getLiterals().get(0);
					erronousproperty = !tempio.getValidPropFlag();
					if (erronousproperty) {
						badprop = tempio.getBadAttribute();
					}
				}
				else if (cur.get().getClass().equals(
						SemanticAnnotationSubject.class)) {
					String subj = cur.getText().trim();
					soluri = uo.getHelper().createlocalURI(subj);
				}
				else if (cur.get().getClass().equals(
						SimpleAnnotation.class)) {
					IntermediateOwlObject tempio = (IntermediateOwlObject) KnowWEUtils
							.getStoredObject(article, cur, OwlHelper.IOO);
					if (tempio.getValidPropFlag()) {
						stringa = tempio.getLiterals().get(0);
					}
					else {
						badprop = tempio.getBadAttribute();
					}
				}

			}

			boolean validprop = false;
			if (erronousproperty) {
				io.setBadAttribute(badprop);
				io.setValidPropFlag(false);
			}
			else if (prop != null) {
				validprop = PropertyManager.getInstance().isValid(prop);
				io.setBadAttribute(prop.getLocalName());
			}

			io.setValidPropFlag(validprop);
			if (!validprop) {
				Logger.getLogger(this.getClass().getName()).log(Level.WARNING,
						"invalid property: " + s.getText());
			}

			if (prop != null && validprop && stringa != null) {
				DefaultSubjectContext sol = (DefaultSubjectContext) ContextManager
						.getInstance().getContext(s, DefaultSubjectContext.CID);
				if (soluri == null) {
					String soluriString = sol.getSubject();
					soluri = uo.getHelper().createlocalURI(soluriString);
				}
				Statement stmnt = null;
				try {
					if (PropertyManager.getInstance().isRDFS(prop)) {
						stmnt = uo.getHelper().createStatement(soluri, prop,
								stringa);
						io.addStatement(stmnt);
						io.merge(uo.getHelper().createStatementSrc(soluri,
								prop, stringa, s.getFather().getFather(),
								OwlHelper.ANNOTATION));
					}
					else if (PropertyManager.getInstance().isRDF(prop)) {
						stmnt = uo.getHelper().createStatement(soluri, prop,
								stringa);
						io.addStatement(stmnt);
						io.merge(uo.getHelper().createStatementSrc(soluri,
								prop, stringa, s.getFather().getFather(),
								OwlHelper.ANNOTATION));
					}
					else if (PropertyManager.getInstance().isNary(prop)) {
						IntermediateOwlObject tempio = UpperOntology
								.getInstance().getHelper()
								.createAnnotationProperty(soluri, prop,
										stringa, s.getFather().getFather());
						io.merge(tempio);

					}
					else {
						stmnt = uo.getHelper().createStatement(soluri, prop,
								stringa);
						io.addStatement(stmnt);
						io.merge(uo.getHelper().createStatementSrc(soluri,
								prop, stringa, s.getFather().getFather(),
								OwlHelper.ANNOTATION));
					}

				}
				catch (RepositoryException e) {
					msgs.add(Messages.error(e.getMessage()));
				}
			}
			SemanticCoreDelegator.getInstance().addStatements(io, s);
			return msgs;
		}

	}

}
