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

package de.d3web.we.kdom.packaging;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

import de.d3web.report.Message;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.packaging.KnowWEPackageManager;
import de.d3web.we.core.packaging.PackageReference;
import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Priority;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.defaultMarkup.DefaultMarkup;
import de.d3web.we.kdom.defaultMarkup.DefaultMarkupType;
import de.d3web.we.kdom.rendering.DelegateRenderer;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.report.KDOMError;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.report.KDOMWarning;
import de.d3web.we.kdom.sectionFinder.AllTextSectionFinder;
import de.d3web.we.kdom.sectionFinder.RegexSectionFinder;
import de.d3web.we.kdom.store.SectionStore;
import de.d3web.we.kdom.subtreeHandler.SubtreeHandler;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class CompileFlag extends DefaultMarkupType {

	private static DefaultMarkup m = null;

	// private static String PRIO_MAP_KEY = "prio_map_key";

	private static String PACKAGEDEFS_SNAPSHOT_KEY = "packagedefs_snapshot_key";

	static {
		m = new DefaultMarkup("Compile");
		m.addContentType(new PackageReferenceType());

	}

	public CompileFlag() {
		super(m);
		this.setCustomRenderer(new CompileFlagRenderer());
	}

	static class CompileFlagRenderer extends KnowWEDomRenderer<CompileFlag> {

		@Override
		@SuppressWarnings("unchecked")
		public void render(KnowWEArticle article,
				Section<CompileFlag> sec,
				KnowWEUserContext user,
				StringBuilder string) {

			List<Section<SinglePackageReference>> packageReferences = new LinkedList<Section<SinglePackageReference>>();
			sec.findSuccessorsOfType(SinglePackageReference.class, packageReferences);
			if (packageReferences.isEmpty()) {
				DelegateRenderer.getInstance().render(article, sec, user, string);
				return;
			}
			string.append(KnowWEUtils.maskHTML("<div id=\"knowledge-panel\" class=\"panel\">"));
			string.append(KnowWEUtils.maskHTML("<h3>" + "Compile: " + sec.getOriginalText() +
					"</h3><div>"));
			for (Section<?> child : packageReferences) {
				if (child.get() instanceof SinglePackageReference) {
					((SinglePackageReferenceRenderer) child.get().getRenderer()).render(article,
							(Section<SinglePackageReference>) child, user, string);
				}
			}
			string.append(KnowWEUtils.maskHTML("</div></div>"));
		}

	}

	static class PackageReferenceType extends DefaultAbstractKnowWEObjectType implements PackageReference {

		public PackageReferenceType() {
			this.sectionFinder = new AllTextSectionFinder();
			this.addSubtreeHandler(Priority.PRECOMPILE, new CompileFlagCreateHandler());
			this.addSubtreeHandler(Priority.POSTCOMPILE, new CompileFlagDestroyHandler());
			this.childrenTypes.add(new SinglePackageReference());
		}

		@Override
		public List<String> getPackagesToReferTo(Section<? extends PackageReference> s) {
			List<String> includes = new LinkedList<String>();
			for (Section<?> child : s.getChildren()) {
				if (child.get() instanceof SinglePackageReference) {
					includes.add(child.getOriginalText());
				}
			}
			return includes;
		}
	}

	static class SinglePackageReference extends DefaultAbstractKnowWEObjectType {

		public SinglePackageReference() {
			this.sectionFinder = new RegexSectionFinder("[\\w-_]+");
			this.setCustomRenderer(new SinglePackageReferenceRenderer());
		}
	}

	static class SinglePackageReferenceRenderer extends KnowWEDomRenderer<SinglePackageReference> {

		@Override
		public void render(KnowWEArticle article,
				Section<SinglePackageReference> sec,
				KnowWEUserContext user,
				StringBuilder string) {

			String packageName = sec.getOriginalText();

			KnowWEPackageManager packageManager = KnowWEEnvironment.getInstance().getPackageManager(
					article.getWeb());

			List<Section<?>> packageDefinitions = packageManager.getPackageDefinitions(packageName);

			Collection<Message> messagesErrors = new LinkedList<Message>();
			Collection<Message> messagesWarnings = new LinkedList<Message>();
			Collection<KDOMError> kdomErrors = new LinkedList<KDOMError>();
			Collection<KDOMWarning> kdomWarnings = new LinkedList<KDOMWarning>();

			for (Section<?> packageDef : packageDefinitions) {
				for (Message m : KnowWEUtils.getMessagesFromSubtree(article,
						packageDef, Message.class)) {
					if (m.getMessageType().equals(Message.ERROR)) {
						messagesErrors.add(m);
					}
					else if (m.getMessageType().equals(Message.WARNING)) {
						messagesWarnings.add(m);
					}
				}
				kdomErrors.addAll(KnowWEUtils.getMessagesFromSubtree(article,
						packageDef, KDOMError.class));
				kdomWarnings.addAll(KnowWEUtils.getMessagesFromSubtree(article,
						packageDef, KDOMWarning.class));
			}

			int errorsCount = messagesErrors.size() + kdomErrors.size();
			int warningsCount = messagesWarnings.size() + kdomWarnings.size();
			String headerErrorsCount = errorsCount > 0 ? "Errors: " + errorsCount : "";
			String headerWarningsCount = warningsCount > 0 ? "Warnings: " + warningsCount : "";

			String errorsAndWarnings = errorsCount > 0 || warningsCount > 0 ? headerErrorsCount
					+ (errorsCount > 0 && warningsCount > 0 ? ", " : "") + headerWarningsCount : "";

			String sectionsCount = "Sections: " + packageDefinitions.size();

			String headerSuffix = packageName.equals(article.getTitle()) ? "" : " ("
					+ sectionsCount
					+ (errorsAndWarnings.length() > 0 ? ", " : "") +
					errorsAndWarnings + ")";

			string.append("%%collapsebox-closed \n");
			string.append("! " + "Compiled package: " + packageName + headerSuffix + "\n");

			if (errorsCount > 0) {
				string.append(KnowWEUtils.maskHTML("<strong>Errors:</strong><p/>\n"));
				for (Message error : messagesErrors) {
					string.append(KnowWEUtils.maskHTML(error.getMessageText() + "<br/>\n"));
				}
				for (KDOMError error : kdomErrors) {
					string.append(KnowWEUtils.maskHTML(error.getVerbalization() + "<br/>\n"));
				}
				string.append(KnowWEUtils.maskHTML("<p/>"));
			}
			if (warningsCount > 0) {
				string.append(KnowWEUtils.maskHTML("<strong>Warnings:</strong><p/>\n"));
				for (Message warning : messagesWarnings) {
					string.append(KnowWEUtils.maskHTML(warning.getMessageText() + "<br/>\n"));
				}
				for (KDOMWarning warning : kdomWarnings) {
					string.append(KnowWEUtils.maskHTML(warning.getVerbalization() + "<br/>\n"));
				}
			}
			string.append("/%\n");
		}



	}


	static class CompileFlagCreateHandler extends SubtreeHandler<PackageReferenceType> {

		public CompileFlagCreateHandler() {
			super(true);
		}

		@Override
		public boolean needsToCreate(KnowWEArticle article, Section<PackageReferenceType> s) {
			return true;
		}

		@Override
		public Collection<KDOMReportMessage> create(KnowWEArticle article, Section<PackageReferenceType> s) {

			KnowWEPackageManager packageMng = KnowWEEnvironment.getInstance().getPackageManager(
					article.getWeb());

			if (article.isFullParse() || !s.isReusedBy(article.getTitle())) {
				packageMng.registerPackageReference(article, s);
			}

			List<Section<?>> packageDefinitions = new LinkedList<Section<?>>();
			for (String referedPackages : s.get().getPackagesToReferTo(s)) {
				if (referedPackages.equals(article.getTitle())) continue;
				List<Section<?>> tempPackageDefs = packageMng.getPackageDefinitions(referedPackages);
				for (Section<?> packageDef : tempPackageDefs) {
					if (!packageDef.getTitle().equals(
							article.getTitle())) {
						packageDefinitions.add(packageDef);
					}
				}
			}

			KnowWEUtils.storeObject(article, s, PACKAGEDEFS_SNAPSHOT_KEY,
					packageDefinitions);

			List<Section<?>> includedNamespaces = new ArrayList<Section<?>>();

			for (Section<?> packDef : packageDefinitions) {
				List<Section<?>> nodes = new LinkedList<Section<?>>();
				packDef.getAllNodesPostOrder(nodes);
				includedNamespaces.addAll(nodes);
			}

			TreeMap<Priority, List<Section<? extends KnowWEObjectType>>> prioMap =
					Priority.createPrioritySortedList(includedNamespaces);

			for (Priority priority : prioMap.descendingKeySet()) {
				List<Section<? extends KnowWEObjectType>> prioList = prioMap.get(priority);
				for (Section<? extends KnowWEObjectType> section : prioList) {
					section.letSubtreeHandlersCreate(article, priority);
				}
			}
			for (Section<?> namespaceDef : packageDefinitions) {
				namespaceDef.setReusedStateRecursively(article.getTitle(), true);
			}

			return null;
		}

	}

	static class CompileFlagDestroyHandler extends SubtreeHandler<PackageReferenceType> {

		public CompileFlagDestroyHandler() {
			super(true);
		}

		@Override
		public Collection<KDOMReportMessage> create(KnowWEArticle article, Section<PackageReferenceType> s) {
			return null;
		}

		@Override
		public boolean needsToDestroy(KnowWEArticle article, Section<PackageReferenceType> s) {
			return true;
		}

		@Override
		@SuppressWarnings("unchecked")
		public void destroy(KnowWEArticle article, Section<PackageReferenceType> s) {

			if (!s.isReusedBy(article.getTitle())) article.setFullParse(this);

			if (!article.isFullParse()) {

				List<Section<?>> storedNamespaceDefinitions = (List<Section<?>>) KnowWEUtils.getObjectFromLastVersion(
						article, s, PACKAGEDEFS_SNAPSHOT_KEY);

				List<Section<?>> nodes = new LinkedList<Section<?>>();
				for (Section<?> nsDef : storedNamespaceDefinitions) {
					nsDef.getAllNodesPostOrder(nodes);
				}
				for (Section<?> node : nodes) {
					if (node.isReusedBy(article.getTitle())) {
						SectionStore lastStore = KnowWEEnvironment.getInstance().getArticleManager(
								article.getWeb()).getTypeStore().getLastSectionStore(
								article.getTitle(),
								node.getID());
						if (lastStore != null) {
							// reuse last section store
							KnowWEEnvironment.getInstance().getArticleManager(
									article.getWeb()).getTypeStore().putSectionStore(
											article.getTitle(), node.getID(),
									lastStore);
						}
					}
				}

				TreeMap<Priority, List<Section<? extends KnowWEObjectType>>> prioMap =
						Priority.createPrioritySortedList(nodes);

				for (Priority priority : prioMap.descendingKeySet()) {
					List<Section<? extends KnowWEObjectType>> prioList = prioMap.get(priority);
					for (Section<? extends KnowWEObjectType> section : prioList) {
						section.letSubtreeHandlersDestroy(article, priority);
					}
				}

			}


		}

	}
}
