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

package de.d3web.we.kdom.table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;

public class TableUtils {

	/**
	 * Returns a list of sections with only the given type in it.
	 * 
	 * @param setion
	 *             current section
	 * @param classname
	 * @param sections
	 * @return
	 */
	public static List<Section> getCertainSections( Section section, String classname, List<Section> sections ) {
		List<Section> children = section.getChildren();
		for( Section child : children ) {
			try {
				if( Class.forName( classname ).isAssignableFrom( child.getObjectType().getClass()) ) {
					sections.add( child );
				} else {
					getCertainSections( child, classname, sections );
				}
			} catch (ClassNotFoundException e) {
				return null;
			}
		}
		return sections;
	}

	/**
	 * Returns the column of the table in which the current cell occurs.
	 * 
	 * @param section
	 *             current section
	 * @return
	 */
	public static int getColumn( Section<TableCellContent> section )
	{
		Section tableLine = section.findAncestorOfType(TableLine.class);
		List<Section> tmpSections = new ArrayList<Section>();
		getCertainSections( tableLine, TableCellContent.class.getName(), tmpSections );
		
		return tmpSections.indexOf( section ) + 1;
	}

	/**
	 * Returns the row of the table in which the current cell occurs.
	 * 
	 * @param section
	 *             current section
	 * @return
	 */
	public static int getRow( Section<TableCellContent> section )
	{
		Section tableContent = section.findAncestorOfType(Table.class);
		
		List<Section> sections = new ArrayList<Section>();
		getCertainSections( tableContent, TableLine.class.getName(), sections );
		
		int col = getColumn(section)-1;
		for(int i = 0; i < sections.size(); i++)
		{
			List<Section> tmpSections = new ArrayList<Section>();
			getCertainSections( sections.get(i), TableCellContent.class.getName(), tmpSections );
			if(tmpSections.size() > col && tmpSections.get(col).equals( section )) return i + 1;
		}
		return 0;
	}

	/**
	 * Checks if the current cell is editable. Returns<code>TRUE</code> if so,
	 * otherwise <code>FALSE</code>.
	 * 
	 * @param section
	 *             current section
	 * @param rows
	 *             value of the row table attribute
	 * @param cols
	 *            value of the column table attribute
	 * @return
	 */
	public static boolean isEditable( Section<TableCellContent> section, String rows, String cols )
	{
		if( rows == null && cols == null ) return true;
		
		boolean isRowEditable = true, isColEditable = true;
		if( rows != null ) {
			List<String> rowsIndex = Arrays.asList( splitAttribute( rows ) );
			String cellRow = String.valueOf( getRow( section ) );
			isRowEditable = !rowsIndex.contains( cellRow );
		}
		
		if( cols != null ) {
			List<String> colsIndex = Arrays.asList( splitAttribute( cols ) );
			String cellCol = String.valueOf( getColumn( section ) );
			isColEditable = !colsIndex.contains( cellCol );
		}
		return (isColEditable && isRowEditable);
	}

	/**
	 * Quotes some special chars.
	 * @param content
	 * @return
	 */
	public static String quote( String content ) {
		if(!(content.contains("\"") || content.contains("'")))
			return content.trim();
		
		content = content.replace("\"", "\\\"");
		content = content.replace("'", "\\\"");
		return content.trim();
	}

	/**
	 * Split an given attribute into tokens.
	 * 
	 * @param attribute
	 * @return
	 */
	public static String[] splitAttribute(String attribute)
	{
		Pattern p = Pattern.compile("[,|;|:]");
		return p.split( attribute );
	}

	/**
	 * Checks the width attribute of the table tag and returns a HTML string containing
	 * the width as CSS style information.
	 * 
	 * @param input
	 * @return
	 */
	public static String getWidth(String input){
		String pattern = "[+]?[0-9]+\\.?[0-9]+(%|px|em|mm|cm|pt|pc|in)";
		String digit = "[+]?[0-9]+\\.?[0-9]+";
	
		if( input == null ) return "";
		
		if( input.matches( digit )) {
			return "style='width:" + input + "px'";
		}
	    if( input.matches( pattern ) ) {
		    return "style='width:" + input + "'";
	    } else {
		    return "";
	    }
	}

	/**
	 * returns whether the current Section is a table and is sortable
	 * @created 31.07.2010
	 * @param sec
	 * @return
	 */
	public static boolean sortOption(Section sec) {
		Section<Table> tableType = sec.findAncestorOfType(Table.class);
		boolean sortable = false;
		if (tableType != null && tableType.getObjectType() instanceof Table) {
			Table table = tableType.getObjectType();
			sortable = table.isSortable();
		}
		return sortable;
	}

	/**
	 * returns whether the current Section gets a sort button
	 * @created 31.07.2010
	 * @param sec
	 * @return
	 */
	public static boolean sortTest(Section sec) {
		boolean sortable = sortOption(sec);

		Section<TableHeaderLine> headerLine = sec.findAncestorOfType(TableHeaderLine.class);
		boolean isHeaderLine = false;
		if (headerLine != null) {
			isHeaderLine = headerLine.getObjectType() instanceof TableHeaderLine;
		}
		return (sortable && isHeaderLine);
	}

	public static KnowWEObjectType getTableType(Section sec) {
		Section table = sec.findAncestorOfType(Table.class);
		return table != null ? table.getObjectType() : null;
	}

}
