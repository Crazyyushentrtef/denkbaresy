package de.knowwe.core.action;

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

import java.io.IOException;

import de.d3web.utils.Log;
import de.knowwe.core.Environment;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.DelegateRenderer;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.utils.KnowWEUtils;

/**
 * ReRenderContentPartAction. Renders a given section again. Often used in
 * combination with AJAX request, to refresh a certain section of an article due
 * to user interaction.
 * 
 * @author smark
 */
public class ReRenderContentPartAction extends AbstractAction {

	private String perform(UserActionContext context) {

		String nodeID = context.getParameter("KdomNodeId");

		Section<? extends Type> section = Sections.getSection(nodeID);

		if (section == null) {
			String message = "Section not found: " + nodeID;
			Log.warning(message);
			return message;

		}

		if (!KnowWEUtils.canView(section, context)) {
			return "You are not allowed to view this content";
		}

		RenderResult b = new RenderResult(context);

		Type type = section.get();
		Renderer renderer = type.getRenderer();
		if (renderer != null) {
			renderer.render(section, context, b);
		}
		else {
			DelegateRenderer.getInstance().render(section, context, b);
		}

		// If the node is in <pre> than do not
		// render it through the JSPWikiPipeline
		String inPre = context.getParameter("inPre");
		String pagedata = b.toStringRaw();

		if (inPre == null) pagedata = Environment.getInstance().getWikiConnector()
				.renderWikiSyntax(pagedata, context.getRequest());
		if (inPre != null) if (inPre.equals("false")) pagedata = Environment.getInstance()
				.getWikiConnector().renderWikiSyntax(pagedata, context.getRequest());

		return RenderResult.unmask(pagedata, context);
	}

	@Override
	public void execute(UserActionContext context) throws IOException {
		String result = perform(context);
		if (result != null && context.getWriter() != null) {
			context.setContentType("text/html; charset=UTF-8");
			context.getWriter().write(result);
		}

	}
}
