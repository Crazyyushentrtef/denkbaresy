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

package de.d3web.we.hermes.kdom;

import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryException;

import de.d3web.we.hermes.kdom.renderer.TimeEventRenderer;
import de.d3web.we.hermes.util.TimeStringInterpreter;
import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.sectionFinder.RegexSectionFinder;
import de.d3web.we.kdom.semanticAnnotation.AnnotationEndSymbol;
import de.d3web.we.kdom.semanticAnnotation.AnnotationStartSymbol;
import de.d3web.we.module.semantic.owl.IntermediateOwlObject;
import de.d3web.we.module.semantic.owl.UpperOntology;

public class TimeEventType extends DefaultAbstractKnowWEObjectType {

	public static final String START_TAG = "<<";
	public static final String END_TAG = ">>";

	@Override
	protected void init() {
		sectionFinder = new RegexSectionFinder(START_TAG + "[\\w|\\W]*?" + END_TAG);
		this.childrenTypes.add(new AnnotationStartSymbol("<<"));
		this.childrenTypes.add(new AnnotationEndSymbol(">>"));
		this.childrenTypes.add(new TimeEventTitleType());
		this.childrenTypes.add(new TimeEventImportanceType());
		this.childrenTypes.add(new TimeEventDateType());
		this.childrenTypes.add(new TimeEventSourceType());
		this.childrenTypes.add(new TimeEventDescriptionType());
	}

	@Override
	public KnowWEDomRenderer getRenderer() {
		return TimeEventRenderer.getInstance();
	}

	@Override
	public IntermediateOwlObject getOwl(Section section) {
		UpperOntology uo = UpperOntology.getInstance();

		IntermediateOwlObject io = new IntermediateOwlObject();
		try {

			/* Getting all the sections from KDOM */
			Section descriptionSection = section
					.findChildOfType(TimeEventDescriptionType.class);
			Section titleSection = section
					.findChildOfType(TimeEventTitleType.class);
			Section importanceSection = section
					.findChildOfType(TimeEventImportanceType.class);
			Section dateSection = section
					.findChildOfType(TimeEventDateType.class);
			List<Section> sources = new ArrayList<Section>();
			section.findSuccessorsOfType(TimeEventSourceType.class, sources);

			if (descriptionSection == null) {
				return io;
			}
			if (importanceSection == null) {
				return io;
			}
			if (dateSection == null) {
				return io;
			}

			/* Getting all the strings from the sections */
			String description = descriptionSection.getOriginalText();
			String title = titleSection.getOriginalText();
			String importance = importanceSection.getOriginalText();
			String date = dateSection.getOriginalText();
			List<String> sourceStrings = new ArrayList<String>();
			for (Section s : sources) {
				sourceStrings.add(s.getOriginalText());
			}

			/* creating all the URIs for the resources */
			String localID = section.getTitle() + "_" + section.getId();
			URI localURI = uo.getHelper().createlocalURI(localID);
			
			URI timeEventURI = uo.getHelper().createlocalURI("TimeEvent");

			Literal descriptionURI = uo.getHelper().createLiteral(description);
			Literal titleURI = uo.getHelper().createLiteral(title);
			Literal importanceURI = uo.getVf().createLiteral(importance);

			// Literal dateURI = uo.getHelper().createLiteral(date);
			TimeStringInterpreter timeStringInterpreter = new TimeStringInterpreter(
					date.trim());
			Literal dateStartURI = uo.getVf().createLiteral(
					timeStringInterpreter.getStartTime());
			Literal dateEndURI = uo.getVf().createLiteral(
					timeStringInterpreter.getEndTime());
			Literal dateTextURI = uo.getVf().createLiteral(
					timeStringInterpreter.getTimeString());

			List<Literal> sourceURIs = new ArrayList<Literal>();
			for (String source : sourceStrings) {
				sourceURIs.add(uo.getVf().createLiteral(source));
			}

			uo.getHelper().attachTextOrigin(section, io, localURI);

			/* adding all OWL statements to io object */
			io.addStatement(uo.getHelper().createStatement(localURI, RDF.TYPE,
					timeEventURI));

			ArrayList<Statement> slist = new ArrayList<Statement>();
			slist.add(uo.getHelper().createStatement(localURI,
					uo.getHelper().createlocalURI("hasDescription"),
					descriptionURI));
			slist.add(uo.getHelper().createStatement(localURI,
					uo.getHelper().createlocalURI("hasTitle"), titleURI));
			slist.add(uo.getHelper().createStatement(localURI,
					uo.getHelper().createlocalURI("hasImportance"),
					importanceURI));
			slist.add(uo.getHelper()
					.createStatement(localURI,
							uo.getHelper().createlocalURI("hasStartDate"),
							dateStartURI));
			slist.add(uo.getHelper().createStatement(localURI,
					uo.getHelper().createlocalURI("hasEndDate"), dateEndURI));
			slist.add(uo.getHelper().createStatement(localURI,
					uo.getHelper().createlocalURI("hasDateDescription"),
					dateTextURI));
			for (Literal sURI : sourceURIs) {
				slist.add(uo.getHelper().createStatement(localURI,
						uo.getHelper().createlocalURI("hasSource"), sURI));
			}

			io.addAllStatements(slist);

		} catch (RepositoryException e) {
			e.printStackTrace();
		}

		return io;
	}
}
