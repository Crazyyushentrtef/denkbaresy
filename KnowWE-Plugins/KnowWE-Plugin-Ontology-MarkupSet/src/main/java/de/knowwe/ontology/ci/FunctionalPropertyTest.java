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
package de.knowwe.ontology.ci;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;

import de.d3web.testing.AbstractTest;
import de.d3web.testing.Message;
import de.d3web.testing.Message.Type;
import de.d3web.utils.Log;
import de.knowwe.ontology.compile.OntologyCompiler;
import de.knowwe.rdf2go.Rdf2GoCore;
import de.knowwe.rdf2go.sparql.utils.SparqlQuery;

/**
 * @author Jochen Reutelshöfer
 * @created 10.01.2014
 */
public class FunctionalPropertyTest extends AbstractTest<OntologyCompiler> {

	@Override
	public Message execute(OntologyCompiler testObject, String[] args, String[]... ignores) throws InterruptedException {
		Rdf2GoCore rdf2GoCore = testObject.getRdf2GoCore();

		String propVariableName = "?funcProp";
		SparqlQuery query = new SparqlQuery().SELECT(propVariableName).WHERE(
				propVariableName + " rdf:type owl:FunctionalProperty");

		List<Value> functionalProperties = new ArrayList<>();
		Rdf2GoCore.QueryRowListResultTable sparqlSelect = rdf2GoCore.sparqlSelect(query);
		Iterator<BindingSet> iterator = sparqlSelect.iterator();
		while (iterator.hasNext()) {
			BindingSet row = iterator.next();
			Value value = row.getValue(propVariableName.substring(1));
			functionalProperties.add(value);
		}

		Map<Value, Map<Value, Set<Value>>> conflicts = new HashMap<Value, Map<Value, Set<Value>>>();
		for (Value prop : functionalProperties) {
			Map<Value, Set<Value>> conflictsForProp = checkFunctionalProperty(prop, rdf2GoCore);
			if (conflictsForProp.size() > 0) {
				conflicts.put(prop, conflictsForProp);
			}
		}

		if (conflicts.size() > 0) {

			StringBuffer message = new StringBuffer();
			message.append("There are violations for functional properties:\n");
			Set<Value> propConflicts = conflicts.keySet();
			for (Value prop : propConflicts) {
				message.append("\n* " + prop.toString() + ":");
				Map<Value, Set<Value>> map = conflicts.get(prop);
				Set<Value> subjectConflictSet = map.keySet();
				for (Value subject : subjectConflictSet) {
					message.append("\n** Subject: " + subject.toString() + " Objects: ");
					Set<Value> objects = map.get(subject);
					for (Value object : objects) {
						message.append(object.toString() + ", ");
					}

				}

			}

			return new Message(Type.FAILURE, message.toString());
		}

		/*
		 * no conflicts, therefore test successful
		 */
		return Message.SUCCESS;
	}

	private Map<Value, Set<Value>> checkFunctionalProperty(Value prop, Rdf2GoCore core) {
		String subjectVariableName = "?subject";
		String objectVariableName = "?object";
		SparqlQuery queryFunctionalPropertyAssertions = new SparqlQuery().SELECT(
				subjectVariableName + " " + objectVariableName).WHERE(
				subjectVariableName + " <" + prop.stringValue() + "> " + objectVariableName);
		Rdf2GoCore.QueryRowListResultTable assertions = null;
		try {
			assertions = core.sparqlSelect(queryFunctionalPropertyAssertions);
		}
		catch (Exception e) {
			Log.severe("Exception while executing sparql:\n" + queryFunctionalPropertyAssertions);
			throw e;
		}
		Iterator<BindingSet> iterator = assertions.iterator();
		Map<Value, Set<Value>> assertionMap = new HashMap<Value, Set<Value>>();
		while (iterator.hasNext()) {
			BindingSet row = iterator.next();
			Value subject = row.getValue(subjectVariableName.substring(1));
			Value object = row.getValue(objectVariableName.substring(1));
			if (assertionMap.containsKey(subject)) {
				assertionMap.get(subject).add(object);
			}
			else {
				Set<Value> objects = new HashSet<Value>();
				objects.add(object);
				assertionMap.put(subject, objects);
			}
		}
		Map<Value, Set<Value>> conflicts = new HashMap<Value, Set<Value>>();
		Set<Value> keySet = assertionMap.keySet();
		for (Value subject : keySet) {
			Set<Value> conflictSet = checkUnity(assertionMap.get(subject), core);
			if (conflictSet != null && conflictSet.size() > 1) {
				conflicts.put(subject, conflictSet);
			}
		}

		return conflicts;

	}

	/**
	 * Determine a set of object URIs where none is owl:sameAs any other
	 *
	 * @param set
	 * @param core
	 * @return
	 * @created 09.01.2014
	 */
	private Set<Value> checkUnity(Set<Value> set, Rdf2GoCore core) {
		if (set.size() == 1) {
			// unity asserted ;-)
			return set;
		}
		Set<Value> conflictSet = new HashSet<Value>();
		for (Value newValue : set) {
			boolean isIn = false;
			for (Value unifiedValue : conflictSet) {
				boolean isSame = core.sparqlAsk("ASK { <" + newValue.stringValue() + "> owl:sameAs <"
						+ unifiedValue.stringValue() + ">. }");
				if (isSame) {
					isIn = true;
					break;
				}

			}
			if (!isIn) {
				conflictSet.add(newValue);
			}
		}

		return conflictSet;
	}

	@Override
	public Class<OntologyCompiler> getTestObjectClass() {
		return OntologyCompiler.class;
	}

	@Override
	public String getDescription() {
		return "Checks for all existing functional property whether they are indeed used as functional, i.e., for each subject node only one object exists.";
	}

}
