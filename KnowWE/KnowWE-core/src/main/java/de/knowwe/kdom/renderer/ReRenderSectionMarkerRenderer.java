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
package de.knowwe.kdom.renderer;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;

/**
 * @author Johannes Dienst
 * 
 *         Renders a marker as <b>div</b> around the CoveringList, so the
 *         content is automatically updated when answers are set.
 * 
 */
public class ReRenderSectionMarkerRenderer implements Renderer {

	/**
	 * Holds the renderer of the inner text.
	 */
	private final Renderer renderer;

	public ReRenderSectionMarkerRenderer(Renderer renderer) {
		this.renderer = renderer;
	}

	@Override
	public void render(Section<?> sec, UserContext user, RenderResult string) {
		Boolean ajaxAction = user.getParameters().containsKey("action");
		if (!ajaxAction) {
			string.appendHtml("<span class=\"ReRenderSectionMarker\" style=\"display: inline;\" rel=\"{id:'"
					+ sec.getID()
					+ "'}\">");
		}
		KnowWEUtils.renderAnchor(sec, string);
		renderer.render(sec, user, string);
		if (!ajaxAction) {
			string.appendHtml("</span>");
		}
	}

}
