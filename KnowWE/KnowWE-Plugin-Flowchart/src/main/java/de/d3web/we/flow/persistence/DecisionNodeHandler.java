/*
 * Copyright (C) 2010 University Wuerzburg, Computer Science VI
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
package de.d3web.we.flow.persistence;

import java.util.List;

import de.d3web.core.manage.KnowledgeBaseUtils;
import de.d3web.diaFlux.flow.FlowFactory;
import de.d3web.diaFlux.flow.INode;
import de.d3web.diaFlux.flow.NOOPAction;
import de.d3web.we.flow.type.DecisionType;
import de.d3web.we.flow.type.FlowchartType;
import de.d3web.we.flow.type.NodeType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.report.KDOMReportMessage;

/**
 * 
 * @author Reinhard Hatko
 * @created 28.11.2010
 */
public class DecisionNodeHandler extends AbstractNodeHandler {

	public DecisionNodeHandler() {
		super(DecisionType.getInstance());

	}

	@Override
	public boolean canCreateNode(KnowWEArticle article, KnowledgeBaseUtils kbm,
			Section<NodeType> nodeSection) {
		return getNodeInfo(nodeSection) != null;
	}

	@Override
	public INode createNode(KnowWEArticle article, KnowledgeBaseUtils kbm, Section<NodeType> nodeSection,
			Section<FlowchartType> flowSection, String id, List<KDOMReportMessage> errors) {

		return FlowFactory.getInstance().createActionNode(id, NOOPAction.INSTANCE);

	}

}
