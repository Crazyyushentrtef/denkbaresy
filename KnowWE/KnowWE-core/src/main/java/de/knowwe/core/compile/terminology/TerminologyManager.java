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

package de.knowwe.core.compile.terminology;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import de.d3web.plugin.Extension;
import de.d3web.plugin.PluginManager;
import de.d3web.strings.Identifier;
import de.knowwe.core.compile.AbstractPackageCompiler;
import de.knowwe.core.compile.Compiler;
import de.knowwe.core.kdom.objects.TermReference;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.plugin.Plugins;

/**
 * This class manages the definition and usage of terms. A term represents some
 * kind of object. For each term that is defined in the wiki (and registered
 * here) it stores the location where it has been defined. Further, for any
 * reference also the locations are stored. The service of this manager is, that
 * for a given term the definition and the references can be asked for.
 * Obviously, this only works if the terms are registered here.
 * 
 * TODO: Prevent ConcurrentModification for Collections returned in getters.
 * 
 * @author Albrecht Striffler (denkbares GmbH)
 * 
 */
public class TerminologyManager {

	private final Set<Compiler> compilers = new HashSet<Compiler>(4);

	public final static String HANDLER_KEY = TerminologyManager.class.getSimpleName();

	private static final Set<Identifier> occupiedTerms = new HashSet<Identifier>();

	private static boolean initializedOccupiedTerms = false;

	private TermLogManager termLogManager = new TermLogManager();

	public TerminologyManager() {

		if (!initializedOccupiedTerms) {
			// extension point for plugins defining predefined terminology
			Extension[] exts = PluginManager.getInstance().getExtensions(
					Plugins.EXTENDED_PLUGIN_ID,
					Plugins.EXTENDED_POINT_Terminology);
			for (Extension extension : exts) {
				Object o = extension.getSingleton();
				if (o instanceof TerminologyExtension) {
					registerOccupiedTerm(((TerminologyExtension) o));
				}
			}
		}
	}

	private void registerOccupiedTerm(TerminologyExtension terminologyExtension) {
		for (String occupiedTermInExternalForm : terminologyExtension.getTermNames()) {
			occupiedTerms.add(Identifier.fromExternalForm(occupiedTermInExternalForm));
		}
	}

	/**
	 * Allows to register a new term.
	 * 
	 * @param compiler TODO
	 * @param termDefinition is the term section defining the term.
	 * @param termIdentifier is the term for which the section is registered
	 * 
	 * @returns true if the sections was registered as the defining section for
	 *          this term. false else.
	 */
	public synchronized void registerTermDefinition(
			Compiler compiler,
			Section<?> termDefinition,
			Class<?> termClass, Identifier termIdentifier) {

		if (occupiedTerms.contains(termIdentifier)) {
			Message msg = Messages.error("The term '"
					+ termIdentifier.toString()
					+ "' is reserved by the system.");
			Messages.storeMessage(compiler instanceof AbstractPackageCompiler
					? (AbstractPackageCompiler) compiler : null,
					termDefinition, this.getClass(), msg);
			return;
		}

		compilers.add(compiler);
		TermLog termRefLog = termLogManager.getLog(termIdentifier);
		if (termRefLog == null) {
			termRefLog = new TermLog();
			termLogManager.putLog(termIdentifier, termRefLog);
		}

		termRefLog.addTermDefinition(compiler, termDefinition, termClass, termIdentifier);
		Messages.clearMessages(compiler instanceof AbstractPackageCompiler
				? (AbstractPackageCompiler) compiler : null,
				termDefinition, this.getClass());
	}

	/**
	 * Terms in KnowWE are case insensitive.<br/>
	 * If the same term is defined with different cases, all different versions
	 * are returned. If the term is undefined, an empty Collection is returned.
	 * 
	 * 
	 * @created 28.07.2012
	 * @param termIdentifier an {@link Identifier} with arbitrarily case for a
	 *        term for which you want potential other versions with different
	 *        cases
	 * @return the different versions of {@link Identifier}s or an empty
	 *         Collection, if the term is undefined
	 */
	public synchronized Collection<Identifier> getAllTermsEqualIgnoreCase(Identifier termIdentifier) {
		TermLog termLog = termLogManager.getLog(termIdentifier);
		Collection<Identifier> termIdentifiers;
		if (termLog == null) {
			termIdentifiers = Collections.emptyList();
		}
		else {
			termIdentifiers = termLog.getTermIdentifiers();
		}
		return Collections.unmodifiableCollection(termIdentifiers);
	}

	public synchronized <TermObject> void registerTermReference(
			Compiler compiler,
			Section<?> termReference,
			Class<?> termClass, Identifier termIdentifier) {

		compilers.add(compiler);
		TermLog termLog = termLogManager.getLog(termIdentifier);
		if (termLog == null) {
			termLog = new TermLog();
			termLogManager.putLog(termIdentifier, termLog);
		}
		termLog.addTermReference(compiler, termReference, termClass, termIdentifier);
	}

	/**
	 * Returns whether a term is defined through a TermDefinition.
	 */
	public synchronized boolean isDefinedTerm(Identifier termIdentifier) {
		TermLog termRef = termLogManager.getLog(termIdentifier);
		if (termRef == null) return false;
		if (termRef.getDefiningSection() == null) return false;
		return true;
	}

	/**
	 * Returns whether there are TermReferences for this Term, but no
	 * TermDefinition
	 */
	public synchronized boolean isUndefinedTerm(Identifier termIdentifier) {
		TermLog termRef = termLogManager.getLog(termIdentifier);
		if (termRef != null) {
			return termRef.getDefiningSection() == null;
		}
		return false;
	}

	/**
	 * For a {@link Identifier} the first defining Section is returned. If the
	 * term is not defined, <tt>null</tt> is returned.
	 * 
	 * @param termIdentifier the {@link Identifier} for the defining Section you
	 *        are looking for
	 * @return the first defining Section for this term or <tt>null</tt> if the
	 *         term is not defined
	 */
	public synchronized Section<?> getTermDefiningSection(Identifier termIdentifier) {
		TermLog refLog = termLogManager.getLog(termIdentifier);
		if (refLog != null) {
			return refLog.getDefiningSection();
		}
		return null;
	}

	/**
	 * For a {@link Identifier} all defining Sections are returned. If the term
	 * is not defined, an empty Collection is returned.
	 * 
	 * @param termIdentifier the {@link Identifier} for the defining Sections
	 *        you are looking for
	 * @return the defining Sections for this term or an empty Collection if the
	 *         term is not defined
	 */
	public synchronized Collection<Section<?>> getTermDefiningSections(Identifier termIdentifier) {
		Collection<Section<?>> definitions = new ArrayList<Section<?>>();
		TermLog refLog = termLogManager.getLog(termIdentifier);
		if (refLog != null) {
			definitions = refLog.getDefinitions();
		}
		return Collections.unmodifiableCollection(definitions);
	}

	/**
	 * For a TermName the redundant TermDefinition are returned.
	 * 
	 * @param <TermObject>
	 * @param s
	 * @return
	 */
	public synchronized Collection<Section<?>> getRedundantTermDefiningSections(Identifier termIdentifier) {
		TermLog refLog = termLogManager.getLog(termIdentifier);
		if (refLog != null) {
			return Collections.unmodifiableSet(refLog.getRedundantDefinitions());
		}
		return Collections.unmodifiableSet(new HashSet<Section<?>>(0));
	}

	public Set<Identifier> getOccupiedTerms() {
		return Collections.unmodifiableSet(occupiedTerms);
	}

	/**
	 * For a {@link KnowWETerm} (provided by the Section) the
	 * {@link TermReference}s are returned.
	 */
	public synchronized <TermObject> Collection<Section<?>> getTermReferenceSections(Identifier termIdentifier) {

		TermLog refLog = termLogManager.getLog(termIdentifier);

		if (refLog != null) {
			return Collections.unmodifiableCollection(refLog.getReferences());
		}

		return Collections.emptyList();
	}

	public synchronized void unregisterTermDefinition(
			Compiler compiler,
			Section<?> termDefinition,
			Class<?> termClass, Identifier termIdentifier) {

		TermLog termRefLog = termLogManager.getLog(termIdentifier);
		if (termRefLog != null) {
			termRefLog.removeTermDefinition(compiler, termDefinition,
					termClass, termIdentifier);
		}
	}

	public synchronized void unregisterTermReference(Compiler compiler, Section<?> termReference, Class<?> termClass, Identifier termIdentifier) {

		TermLog refLog = termLogManager.getLog(termIdentifier);
		if (refLog != null) {
			refLog.removeTermReference(compiler, termReference, termClass, termIdentifier);
		}
	}

	public synchronized void removeTermsOfCompiler(Compiler compiler) {
		// counting the compilers does not help if terms are unregistered
		// normally... since this is just an optimization and will work
		// correctly any way, we don't change it for now
		compilers.remove(compiler);
		if (compilers.isEmpty()) {
			termLogManager = new TermLogManager();
		}
		else {
			Iterator<Entry<Identifier, TermLog>> iterator = termLogManager.entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<Identifier, TermLog> entry = iterator.next();
				entry.getValue().removeEntriesOfCompiler(compiler);
				if (entry.getValue().isEmpty()) {
					iterator.remove();
				}
			}
		}
	}

	/**
	 * Returns all local terms of the given class (e.g. Question, String,...),
	 * that are compiled in the article with the given title.
	 * 
	 * @created 03.11.2010
	 */
	public synchronized Collection<Identifier> getAllDefinedTermsOfType(Class<?> termClass) {
		return getAllDefinedTerms(termClass);
	}

	/**
	 * Returns all local terms that are compiled in the article with the given
	 * title.
	 * 
	 * @created 03.11.2010
	 */
	public synchronized Collection<Identifier> getAllDefinedTerms() {
		return getAllDefinedTerms(null);
	}

	public synchronized Collection<Identifier> getAllDefinedTerms(Class<?> termClass) {
		Collection<TermLog> termLogEntries = getAllDefinedTermLogEntries(termClass);
		Collection<Identifier> terms = new HashSet<Identifier>();
		for (TermLog logEntry : termLogEntries) {
			terms.addAll(logEntry.getTermIdentifiers());
		}
		return terms;
	}

	private synchronized Collection<TermLog> getAllDefinedTermLogEntries(Class<?> termClass) {
		Collection<TermLog> filteredLogEntries = new HashSet<TermLog>();
		for (Entry<Identifier, TermLog> managerEntry : termLogManager.entrySet()) {
			Set<Class<?>> termClasses = managerEntry.getValue().getTermClasses();
			if (termClasses.size() != 1) continue;
			boolean hasTermDefOfType = managerEntry.getValue().getDefiningSection() != null
					&& (termClass == null || termClass.isAssignableFrom(termClasses.iterator().next()));
			if (hasTermDefOfType) {
				filteredLogEntries.add(managerEntry.getValue());
			}
		}
		return filteredLogEntries;
	}

	/**
	 * Returns if a term has been registered with the specified name and if its
	 * class is of the specified class. Otherwise (if no such term exists or it
	 * does not have a compatible class) false is returned.
	 * 
	 * @created 05.03.2012
	 * @param termIdentifier the term to be searched for
	 * @param clazz the class the term must be a subclass of (or of the same
	 *        class)
	 * @return if the term has been registered as required
	 */
	public synchronized boolean hasTermOfClass(Identifier termIdentifier, Class<?> clazz) {
		for (Class<?> termClass : getTermClasses(termIdentifier)) {
			if (clazz.isAssignableFrom(termClass)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns all term classes for a term or an empty Collection, if the term
	 * is undefined.<br/>
	 * A term only has multiple term classes, if the term is defined multiple
	 * times with a matching {@link Identifier} but different term classes.
	 * 
	 * @created 28.07.2012
	 * @param termIdentifier the {@link Identifier} for the term you want the
	 *        term classes from
	 * @return all term classes or an empty Collection, if undefined
	 */
	public synchronized Collection<Class<?>> getTermClasses(Identifier termIdentifier) {
		TermLog refLog = termLogManager.getLog(termIdentifier);
		if (refLog == null) return Collections.emptyList();
		else return Collections.unmodifiableCollection(refLog.getTermClasses());
	}

}
