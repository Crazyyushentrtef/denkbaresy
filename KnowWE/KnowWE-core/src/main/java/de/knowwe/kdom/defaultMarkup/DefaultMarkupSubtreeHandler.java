/*
 * Copyright (C) 2010 Chair of Artificial Intelligence and Applied Informatics
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

package de.knowwe.kdom.defaultMarkup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import de.knowwe.core.KnowWEEnvironment;
import de.knowwe.core.compile.packaging.KnowWEPackageManager;
import de.knowwe.core.kdom.KnowWEArticle;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.subtreeHandler.SubtreeHandler;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup.Annotation;

public class DefaultMarkupSubtreeHandler extends SubtreeHandler<DefaultMarkupType> {

	private final DefaultMarkup markup;

	public DefaultMarkupSubtreeHandler(DefaultMarkup markup) {
		super(true);
		this.markup = markup;
	}

	@Override
	public Collection<Message> create(KnowWEArticle article, Section<DefaultMarkupType> markupSection) {

		List<Message> msgs = new ArrayList<Message>();

		// check defined annotations
		for (Annotation annotation : this.markup.getAnnotations()) {
			String name = annotation.getName();
			Section<? extends AnnotationContentType> annotationSection =
					DefaultMarkupType.getAnnotationContentSection(markupSection, name);

			// check existence of mandatory annotation
			if (annotationSection == null && annotation.isMandatory()) {
				Message message = Messages.error("The annotation @" + name
						+ " is mandatory, but missing. Please specify that annotation.");
				msgs.add(message);
			}
		}

		// register section in the package manager
		if (!markupSection.get().isIgnoringPackageCompile()) {
			String value = null;
			Annotation packageAnno = this.markup.getAnnotation(KnowWEPackageManager.PACKAGE_ATTRIBUTE_NAME);
			Section<? extends AnnotationContentType> annotationContent = null;
			if (packageAnno != null) {
				annotationContent = DefaultMarkupType.getAnnotationContentSection(markupSection,
								packageAnno.getName());
				if (annotationContent != null) {
					value = annotationContent.getText();
				}
			}
			KnowWEEnvironment.getInstance().getPackageManager(
					article.getWeb()).addSectionToPackage(
					markupSection, value);
//			Set<String> packageNames = markupSection.getPackageNames();
//			boolean multi = packageNames.size() > 1;
//			Messages.warning("This section is registered to the package" + (multi ? "s" : "") + " "
//					+ (multi ? packageNames.toString() : "'" + packageNames.iterator().next()));
		}

		// check unrecognized annotations
		List<Section<UnknownAnnotationType>> unknownSections = Sections.findChildrenOfType(
				markupSection, UnknownAnnotationType.class);
		for (Section<UnknownAnnotationType> annotationSection : unknownSections) {
			String name = UnknownAnnotationType.getName(annotationSection);
			Message message = Messages.error("The annotation @" + name
					+ " is not known to KnowWE. It will be ignored.");
			msgs.add(message);
		}

		// check annotated sections
		List<Section<AnnotationContentType>> subSections = Sections.findChildrenOfType(
				markupSection,
				AnnotationContentType.class);
		for (Section<AnnotationContentType> annotationSection : subSections) {
			// check annotations pattern
			Annotation annotation = annotationSection.get().getAnnotation();
			String text = annotationSection.getText();
			if (!annotation.matches(text)) {
				String name = annotation.getName();
				Message message = Messages.error("The value of annotation @"
						+ name
						+ " is invalid: " + text);
				msgs.add(message);
			}
		}
		return msgs;
	}

	@Override
	public void destroy(KnowWEArticle article, Section<DefaultMarkupType> markupSection) {
		// unregister section in the package manager
		// TODO: refactor this to somewhere else
		if (!markupSection.get().isIgnoringPackageCompile()) {
			KnowWEEnvironment.getInstance().getPackageManager(article.getWeb()).removeSectionFromAllPackages(
						markupSection);
		}
	}
}
