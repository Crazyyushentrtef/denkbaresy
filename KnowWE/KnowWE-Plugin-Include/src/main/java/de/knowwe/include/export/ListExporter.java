/*
 * Copyright (C) 2014 denkbares GmbH
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
package de.knowwe.include.export;

import java.math.BigInteger;
import java.util.List;

import org.apache.poi.xwpf.usermodel.XWPFParagraph;

import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.include.export.DocumentBuilder.Style;
import de.knowwe.jspwiki.types.ListType;
import de.knowwe.jspwiki.types.OrderedListType;
import de.knowwe.kdom.dashtree.DashTreeElement;
import de.knowwe.kdom.dashtree.DashTreeElementContent;

/**
 * 
 * @author Volker Belli (denkbares GmbH)
 * @created 07.02.2014
 */
public class ListExporter implements Exporter<Type> {

	// TODO: find abstract ids by list style names
	private static final BigInteger ABSTRACT_ID_ORDERED = BigInteger.valueOf(15);
	private static final BigInteger ABSTRACT_ID_UNORDERED = BigInteger.valueOf(14);

	@Override
	public boolean canExport(Section<Type> section) {
		return section.get() instanceof OrderedListType
				|| section.get() instanceof ListType;
	}

	@Override
	public Class<Type> getSectionType() {
		return Type.class;
	}

	@Override
	public void export(Section<Type> section, DocumentBuilder manager) throws ExportException {
		// TODO start a new list here (number shall not been continued)
		BigInteger abstractID = section.get() instanceof OrderedListType
				? ABSTRACT_ID_ORDERED : ABSTRACT_ID_UNORDERED;

		// TODO: avoid using global variable here!
		BigInteger numID = manager.getDocument().getNumbering().addNum(abstractID);
		List<Section<DashTreeElement>> items = Sections.successors(section, DashTreeElement.class);
		for (Section<DashTreeElement> item : items) {
			exportItem(item, numID, manager);
			manager.closeParagraph();
		}
	}

	public void exportItem(Section<DashTreeElement> section, BigInteger numID, DocumentBuilder manager) throws ExportException {
		String text = section.getText().trim();
		int depth = 0;
		while (depth < text.length() && "#*".indexOf(text.charAt(depth)) >= 0) {
			depth++;
		}

		XWPFParagraph paragraph = manager.getNewParagraph(Style.list);
		paragraph.setNumID(numID);
		paragraph.getCTP().getPPr().getNumPr().addNewIlvl().setVal(BigInteger.valueOf(depth - 1));
		manager.exportSection(Sections.successor(section, DashTreeElementContent.class));
		manager.closeParagraph();
	}
}
