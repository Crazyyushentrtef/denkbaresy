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

package de.d3web.diaFlux.inference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.d3web.core.inference.KnowledgeSlice;
import de.d3web.core.inference.MethodKind;
import de.d3web.core.inference.PSMethod;
import de.d3web.core.inference.PropagationEntry;
import de.d3web.core.inference.Rule;
import de.d3web.core.inference.RuleSet;
import de.d3web.core.inference.condition.NoAnswerException;
import de.d3web.core.inference.condition.UnknownAnswerException;
import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.TerminologyObject;
import de.d3web.core.knowledge.terminology.NamedObject;
import de.d3web.core.session.Session;
import de.d3web.core.session.blackboard.Fact;
import de.d3web.core.session.blackboard.Facts;
import de.d3web.diaFlux.flow.DiaFluxCaseObject;
import de.d3web.diaFlux.flow.EdgeSupport;
import de.d3web.diaFlux.flow.Flow;
import de.d3web.diaFlux.flow.FlowData;
import de.d3web.diaFlux.flow.FlowSet;
import de.d3web.diaFlux.flow.IEdge;
import de.d3web.diaFlux.flow.INode;
import de.d3web.diaFlux.flow.INodeData;
import de.d3web.diaFlux.flow.ISupport;
import de.d3web.diaFlux.flow.RuleSupport;
import de.d3web.diaFlux.flow.SnapshotNode;
import de.d3web.diaFlux.flow.StartNode;

/**
 * 
 * @author Reinhard Hatko Created: 10.09.2009
 * 
 */
public class FluxSolver implements PSMethod {

	public static final MethodKind DIAFLUX = new MethodKind("DIAFLUX");

	
	private static FluxSolver instance;
	
	public FluxSolver() {
		instance = this;
	}

	@Override
	public void init(Session session) {

		if (!isFlowCase(session)) return;

		log("Initing FluxSolver with case: " + session);

	}

	public static void indicateFlow(Rule rule, INode startNode, Session session) {

		addPathEntryForNode(session, null, startNode, new RuleSupport(rule));
	}

	public static boolean isFlowCase(Session session) {

		FlowSet flowSet = getFlowSet(session);

		return flowSet != null && !flowSet.getFlows().isEmpty();
	}

	public static DiaFluxCaseObject getFlowData(Session session) {

		FlowSet flowSet = getFlowSet(session);

		return (DiaFluxCaseObject) session.getCaseObject(flowSet);
	}

	public static FlowSet getFlowSet(Session session) {

		return getFlowSet(session.getKnowledgeBase());

	}

	public static void addFlow(Flow flow, KnowledgeBase base, String title) {
		// TODO
		// FlowSet flowSet = (FlowSet) base.getKnowledge(FluxSolver.class,
		// FluxSolver.DIAFLUX);
		//
		// if (flowSet == null) {
		// flowSet = new FlowSet(title);
		// base.addKnowledge(FluxSolver.class, flowSet, FluxSolver.DIAFLUX);
		//
		// }
		//
		// flowSet.put(flow);

		List ks = (List) base.getKnowledge(FluxSolver.class, FluxSolver.DIAFLUX);

		FlowSet flowSet;
		if (ks == null) {
			flowSet = new FlowSet(title);
			base.addKnowledge(FluxSolver.class, flowSet, FluxSolver.DIAFLUX);

		}
		else {
			flowSet = (FlowSet) ks.get(0);
		}

		flowSet.put(flow);

	}

	public static FlowSet getFlowSet(KnowledgeBase knowledgeBase) {

		List knowledge = (List) knowledgeBase.getKnowledge(FluxSolver.class,
				FluxSolver.DIAFLUX);

		if (knowledge == null) return null;

		return (FlowSet) knowledge.get(0);
		// TODO
		// return (FlowSet) knowledgeBase.getKnowledge(FluxSolver.class,
		// FluxSolver.DIAFLUX);

	}

	/**
	 * Adds a path entry for the current node. Predecessor's entry is removed by
	 * this method.
	 * 
	 * @param session
	 * @param currentEntry
	 * @param nextNode
	 * @param currentNode
	 * @return the new {@link PathEntry} for node
	 */
	private static PathEntry addPathEntryForNode(Session session, PathEntry currentEntry, INode nextNode, ISupport support) {

		INode currentNode;

		if (currentEntry != null) currentNode = currentEntry.getNode();
		else currentNode = null;

		log("Adding PathEntry for Node' " + nextNode + "' as successor of '"
				+ currentNode + "'.");

		// both are null if a new flow is started (at first start, after
		// snapshot, (after fork?))
		PathEntry stack;
		PathEntry predecessor;

		if (currentNode instanceof SnapshotNode) {
			// starts new flow -> stack = null, pred = null
			stack = null;
			predecessor = null;

		}
		else { // continue old flow

			// if (currentNode == null)
			// log("IllegalState in addPathEntryForNode", Level.SEVERE);

			predecessor = currentEntry;

			if (nextNode instanceof StartNode) { // node is composed node -> new
				// stack
				stack = currentEntry;
			}
			else { // normal node
				stack = currentEntry.getStack();
			}
		}

		INodeData nodeData = getNodeData(nextNode, session);

		PathEntry newEntry = new PathEntry(predecessor, stack, nodeData, support);

		if (currentEntry != null) { // entry for this flow already in pathends
			replacePathEnd(session, currentEntry, newEntry);
		}
		else { // new flow
			addPathEnd(session, newEntry);
		}

		return newEntry;

	}

	private static void addPathEnd(Session session, PathEntry newEntry) {
		log("Adding new PathEnd for node: " + newEntry.getNode());
		DiaFluxCaseObject caseObject = getFlowData(session);

		List<PathEntry> pathEnds = caseObject.getPathEnds();
		pathEnds.add(newEntry);
		log("PathEnds after: " + pathEnds);

		newEntry.getNodeData().addSupport(newEntry);
	}

	private static void replacePathEnd(Session session, PathEntry currentEntry, PathEntry newEntry) {

		log("Replacing PathEnd '" + currentEntry + "' by '" + newEntry + "'.");

		List<PathEntry> pathEnds = getFlowData(session).getPathEnds();

		log("PathEnds before: " + pathEnds);

		// Exception: fork? (would fail after first successor)
		boolean remove = pathEnds.remove(currentEntry);

		if (!remove) throw new IllegalStateException("Predecessor '" + currentEntry
				+ "' not found in PathEnds: " + pathEnds);

		if (newEntry != null) {
			addPathEnd(session, newEntry);
		}
		else {
			System.out.println("+++++TODO in FluxSolver.replacePathEnd()");
		}

	}

	/**
	 * Continues the flow from the supplied entry on. At first the next node is
	 * selected by {@link #selectNextEdge(INode)}. If the next node is the
	 * current node flow execution stalls. Otherwise: If the next node is not
	 * yet active in the case {@link INodeData#isActive()}, it is activated.
	 * Otherwise flow execution stalls.
	 * 
	 * @param session
	 * @param startEntry
	 */
	private void flow(Session session, PathEntry startEntry) {

		log("Start flowing from node: " + startEntry.getNode());

		PathEntry currentEntry = startEntry;

		while (true) {

			IEdge edge = selectNextEdge(session, currentEntry);

			if (edge == null) { // no edge to take
				log("Staying in Node: " + currentEntry.getNode());
				return;
			}

			INodeData nextNodeData = getNodeData(edge.getEndNode(), session);

			if (nextNodeData.isActive()) {
				// what's next??? just stall?
				log("Stop flowing. Node is already active: " + currentEntry.getNode());
				return;
			}

			currentEntry = followEdge(session, currentEntry, edge);

		}

	}

	/**
	 * Activates the given node coming from the given {@link PathEntry}. Steps:
	 * 1. Sets the node to active. 2. Conducts its action 3. Add
	 * {@link PathEntry} for node.
	 * 
	 * @param session
	 * @param currentNode
	 * @param entry the pathentry from where to activate the node
	 * @param edge the egde to take
	 * @return nextNode
	 */
	private PathEntry followEdge(Session session, PathEntry entry, IEdge edge) {

		INode nextNode = edge.getEndNode();
		INodeData nextNodeData = getNodeData(nextNode, session);

		log("Following edge '" + edge + "'.");

		// Assert.assertFalse(nextNodeData.isActive());
		if (nextNodeData.isActive()) {
			log("Node is already active: " + nextNode, Level.SEVERE);
			return null; // TODO correct?
		}

		doAction(session, nextNode);

		PathEntry newPathEntry = addPathEntryForNode(session, entry, nextNode,
				new EdgeSupport(edge));

		return newPathEntry;

	}

	private static INodeData getNodeData(INode nextNode, Session session) {
		DiaFluxCaseObject caseObject = getFlowData(session);

		FlowData flowData = caseObject.getFlowDataFor(nextNode.getFlow().getId());

		INodeData dataForNode = flowData.getDataForNode(nextNode);
		return dataForNode;
	}

	private void doAction(Session session, INode nextNode) {
		log("Starting action: " + nextNode.getAction());
		nextNode.getAction().doIt(session, nextNode, this);
	}

	/**
	 * Selects appropriate successor of {@code node} according to the current
	 * state of the case.
	 * 
	 * @param session
	 * @param currentEntry
	 * 
	 * @return
	 */
	private IEdge selectNextEdge(Session session, PathEntry currentEntry) {

		INode node = currentEntry.getNode();
		// INodeData nodeData = getNodeData(node, session);
		// nodeData.setActive(true);

		Iterator<IEdge> edges = node.getOutgoingEdges().iterator();

		while (edges.hasNext()) {

			IEdge edge = edges.next();

			try {
				if (edge.getCondition().eval(session)) {

					// ausser fork
					// disable for debugging
					// assertOtherEdgesFalse(session, edges); //all other edges'
					// predicates must be false

					return edge;

				}
			}
			catch (NoAnswerException e) {
			}
			catch (UnknownAnswerException e) {
			}
		}

		// throw new UnsupportedOperationException("can not stay in node '" +
		// nodeDeclaration + "'");

		log("No edge to take from node:" + node);
		return null;

	}

	// private void assertOtherEdgesFalse(Session session, Iterator<IEdge>
	// edges) throws NoAnswerException, UnknownAnswerException {
	// while (edges.hasNext()) {
	// if (edges.next().getCondition().eval(session)) {
	// throw new IllegalStateException("");
	// }
	//			
	// }
	// }

	@Override
	public void propagate(Session session, Collection<PropagationEntry> changes) {

		if (!isFlowCase(session)) return;

		log("Start propagating: " + changes);

		checkFCIndications(changes, session);

		DiaFluxCaseObject caseObject = getFlowData(session);
		caseObject.setContinueFlowing(true);

		// repeat until no new PathEnds are inserted. (As iteration happens over
		// new lists)
		while (caseObject.isContinueFlowing()) {
			caseObject.setContinueFlowing(false);

			for (PathEntry entry : new ArrayList<PathEntry>(caseObject.getPathEnds())) {

				maintainTruth(session, entry, changes);

			}

			for (PathEntry entry : new ArrayList<PathEntry>(caseObject.getPathEnds())) {

				flow(session, entry);

			}

		}
		log("Finished propagating.");

	}

	private static void checkFCIndications(Collection<PropagationEntry> changes, Session session) {

		for (PropagationEntry entry : changes) {

			TerminologyObject object = entry.getObject();
			KnowledgeSlice knowledge = ((NamedObject) object).getKnowledge(FluxSolver.class,
					MethodKind.FORWARD);
			if (knowledge != null) {
				RuleSet rs = (RuleSet) knowledge;
				for (Rule rule : rs.getRules()) {
					rule.check(session);
				}
			}
		}
	}

	private void maintainTruth(Session session, PathEntry startEntry, Collection<PropagationEntry> changes) {

		log("Start maintaining truth.");

		PathEntry earliestWrongPathEntry = null;
		PathEntry entry = startEntry;

		while (entry != null) {

			ISupport support = entry.getSupport();
			boolean eval = support.isValid(session);

			// TODO !!!check support from other edges
			// something like !eval && entry.getNodeData().getReferenceCounter()
			// == 0
			// if in between there are subpathes that have support by other
			// nodes
			// will they be collapsed too???
			// TODO ? search for and collapse only completely wrong subpathes??
			if (!eval) {

				log("Support is no longer valid: " + support);
				earliestWrongPathEntry = entry;
			}

			entry = entry.getPath();
		}

		if (earliestWrongPathEntry != null) {
			// incoming edge to earliest wrong entry is now false, so collapse
			// back to its predecessor
			collapsePathUntilEntry(session, startEntry, earliestWrongPathEntry.getPath());
		}
		else log("No TMS necessary");

		log("Finished maintaining truth.");

	}

	private void collapsePathUntilEntry(Session session, PathEntry startEntry,
			PathEntry endEntry) {

		log("Collapsing path from '" + startEntry + "' back to " + endEntry);

		int counter = 0;
		PathEntry currentEntry = startEntry;

		while (currentEntry != endEntry) {

			INodeData data = currentEntry.getNodeData();
			data.removeSupport(currentEntry);

			undoAction(session, data.getNode());

			currentEntry = currentEntry.getPath();

			if (counter++ > 250) log("Endloss loop in collapsePath? Trying to reach '" + endEntry
					+ "' starting from '" + startEntry + "'. Now being at '"
					+ currentEntry + "'.");

		}
		replacePathEnd(session, startEntry, endEntry);

	}

	private void undoAction(Session session, INode node) {
		log("Undoing action: " + node);
		node.getAction().undo(session, node, this);
	}

	private static void log(String message) {
		log(message, Level.INFO);
	}

	private static void log(String message, Level level) {
		Logger.getLogger(FluxSolver.class.getName()).log(level, message);
	}

	@Override
	public boolean isContributingToResult() {
		return false;
	}

	@Override
	public Fact mergeFacts(Fact[] facts) {
		// diaflux does not derive own facts
		return Facts.mergeError(facts);
	}

}
