/*
 * Copyright (C) 2010 denkbares GmbH
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
package de.knowwe.d3web.property;

import java.util.Collection;
import java.util.Locale;

import de.d3web.core.knowledge.InfoStore;
import de.d3web.core.knowledge.terminology.NamedObject;
import de.d3web.core.knowledge.terminology.info.Property;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.Sections;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.report.message.NoSuchObjectError;
import de.d3web.we.object.ContentDefinition;
import de.d3web.we.object.IDObjectReference;
import de.d3web.we.object.LocaleDefinition;
import de.d3web.we.object.PropertyReference;
import de.d3web.we.reviseHandler.D3webSubtreeHandler;
import de.d3web.we.utils.MessageUtils;

/**
 * Parses a line defining a property
 * 
 * Syntax of the line: IDOBjectName.Property(.Language(.Country)?)? = content
 * 
 * @author Markus Friedrich (denkbares GmbH)
 * @created 10.11.2010
 */
public class PropertyReviseSubtreeHandler extends D3webSubtreeHandler<PropertyType> {

	@Override
	public Collection<KDOMReportMessage> create(KnowWEArticle article, Section<PropertyType> s) {
		Section<IDObjectReference> idobjectSection = Sections.findSuccessor(s,
				IDObjectReference.class);
		Section<PropertyReference> propertySection = Sections.findSuccessor(s,
				PropertyReference.class);
		if (idobjectSection == null) return null;
		NamedObject object = idobjectSection.get().getTermObject(article, idobjectSection);
		if (object == null) {
			return MessageUtils.asList(new NoSuchObjectError(idobjectSection.get().getTermName(
					idobjectSection)));
		}
		if (propertySection == null) return null;
		Property<?> property = propertySection.get().getTermObject(article, propertySection);
		if (property == null) return null;
		Section<LocaleDefinition> localeSection = Sections.findSuccessor(s, LocaleDefinition.class);
		Section<ContentDefinition> contentSection = Sections.findSuccessor(s,
				ContentDefinition.class);
		if (contentSection == null) return MessageUtils.syntaxErrorAsList("Property value is missing for property "
				+ property);
		String content = contentSection.getOriginalText();
		if (content == null || content.trim().isEmpty()) {
			return MessageUtils.syntaxErrorAsList("Property value is missing for property "
					+ property);
		}
		if (content.startsWith(PropertyType.ESCAPE) && content.endsWith(PropertyType.ESCAPE)) {
			content = content.substring(PropertyType.ESCAPE.length(), content.length()
					- PropertyType.ESCAPE.length());
		}
		else if (content.startsWith("\"") && content.endsWith("\"")) {
			content = content.replaceAll("\\\\\"", "\"");
		}

		Locale locale = InfoStore.NO_LANGUAGE;
		if (localeSection != null) {
			locale = localeSection.get().getTermObject(article, localeSection);
		}
		Object value;
		try {
			value = property.parseValue(content);
		}
		catch (NoSuchMethodException e) {
			return MessageUtils.syntaxErrorAsList("The property " + property
						+ " is not supported by the %%Propery markup.");
		}
		catch (IllegalArgumentException e) {
			return MessageUtils.syntaxErrorAsList("The property value \"" + content
						+ "\" is not compatible with the property " + property);
		}
		try {
			object.getInfoStore().addValue(property, locale, value);
		}
		catch (IllegalArgumentException e) {
			return MessageUtils.syntaxErrorAsList("The property " + property +
						" cannot be localized.");
		}
		return null;
	}

	@Override
	public void destroy(KnowWEArticle article, Section<PropertyType> s) {
		Section<IDObjectReference> idobjectSection = Sections.findSuccessor(s,
				IDObjectReference.class);
		Section<PropertyReference> propertySection = Sections.findSuccessor(s,
				PropertyReference.class);
		if (idobjectSection == null) return;
		NamedObject object = idobjectSection.get().getTermObject(article, idobjectSection);
		if (object == null) return;
		if (propertySection == null) return;
		Property<?> property = propertySection.get().getTermObject(article, propertySection);
		if (property == null) return;
		Section<LocaleDefinition> localeSection = Sections.findSuccessor(s, LocaleDefinition.class);
		Section<ContentDefinition> contentSection = Sections.findSuccessor(s,
				ContentDefinition.class);
		if (contentSection == null) return;
		String content = contentSection.getOriginalText();
		if (content == null || content.trim().isEmpty()) {
			return;
		}
		Locale locale = InfoStore.NO_LANGUAGE;
		if (localeSection != null) {
			locale = localeSection.get().getTermObject(article, localeSection);
		}
		try {
			object.getInfoStore().remove(property, locale);
		}
		catch (IllegalArgumentException e) {
			return;
		}
		return;
	}
}
