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

package de.d3web.we.taghandler;

import java.util.ArrayList;
import java.util.Map;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.TaggingMangler;
import de.d3web.we.core.KnowWEAttributes;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class TagEditPanel extends AbstractTagHandler {

	public TagEditPanel() {
		super("tageditpanel");
	}

	@Override
	public String render(String topic, KnowWEUserContext user,
			Map<String, String> values, String web) {
		TaggingMangler tm = TaggingMangler.getInstance();
		ArrayList<String> tags = tm.getPageTags(topic);
		String output = "<p>";
		output += "current tags(click to edit):";
		output += "<span id=\"tagspan\">";
		for (String cur : tags) {
			output += cur + " ";
		}
		if (tags.size()==0){
			output+="none";
		}
		output += "</span>";
		output += "<script type=\"text/javascript\">";
		output += "var myIPE=new SilverIPE('tagspan','KnowWE.jsp',{parameterName:'tagtag',highlightColor: '#ffff77',"
				+ "additionalParameters:{tagaction:\"set\",action:\"TagHandlingAction\","
				+ KnowWEAttributes.TOPIC + ":\"" + topic + "\"} });";
		output += "</script>";
		output += "</p>";
		return KnowWEEnvironment.maskHTML(output);
	}

}
