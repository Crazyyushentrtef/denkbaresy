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

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.jspwiki.types.TableOfContentsType;

/**
 * 
 * @author Volker Belli (denkbares GmbH)
 * @created 07.02.2014
 */
public class TOCExporter implements Exporter<TableOfContentsType> {

	@Override
	public boolean canExport(Section<TableOfContentsType> section) {
		return true;
	}

	@Override
	public Class<TableOfContentsType> getSectionType() {
		return TableOfContentsType.class;
	}

	@Override
	public void export(Section<TableOfContentsType> section, DocumentBuilder manager) throws ExportException {
		manager.getDocument().createTOC();
	}

}
