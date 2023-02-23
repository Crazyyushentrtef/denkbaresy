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

package de.d3web.we.kdom.condition;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryException;

import de.d3web.we.kdom.AbstractKnowWEObjectType;
import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.Annotation.Finding;
import de.d3web.we.kdom.basic.RoundBracedType;
import de.d3web.we.kdom.kopic.rules.ruleActionLine.SolutionValueAssignment;
import de.d3web.we.kdom.sectionFinder.AllTextFinderTrimmed;
import de.d3web.we.kdom.sectionFinder.AllTextSectionFinder;
import de.d3web.we.module.semantic.owl.IntermediateOwlObject;
import de.d3web.we.module.semantic.owl.UpperOntology;

public class Conjunct extends DefaultAbstractKnowWEObjectType {

    @Override
    public void init() {
    	this.childrenTypes.add(new RoundBracedType(this));
    	this.childrenTypes.add(new SolutionValueAssignment());
    	this.childrenTypes.add(new CondKnownType());
    	this.childrenTypes.add(new Finding());
    	this.sectionFinder = new AllTextFinderTrimmed();
    }

    @Override /* (non-Javadoc)
	* @see de.d3web.we.dom.AbstractOWLKnowWEObjectType#getOwl(de.d3web.we.dom.Section)
	*/	
	public IntermediateOwlObject getOwl(Section s) {
	    IntermediateOwlObject io=new IntermediateOwlObject();
	    try {
	    UpperOntology uo=UpperOntology.getInstance();
	    URI compositeexpression=uo.getHelper().createlocalURI(s.getTitle()+".."+s.getId());
	    io.addStatement(uo.getHelper().createStatement(compositeexpression,RDF.TYPE,uo.getHelper().createURI("Conjunction")));	    
	    io.addLiteral(compositeexpression);
	    for (Section current:s.getChildren()){
		if (current.getObjectType() instanceof AbstractKnowWEObjectType) {
		    AbstractKnowWEObjectType handler=(AbstractKnowWEObjectType) current.getObjectType();
		    IntermediateOwlObject iohandler = handler.getOwl(current);		    
		    for (URI curi:iohandler.getLiterals()){
			Statement state=uo.getHelper().createStatement(compositeexpression,uo.getHelper().createURI("hasConjuncts"),curi);
			io.addStatement(state);
			iohandler.removeLiteral(curi);
		    }		    
		    io.merge(iohandler);
		}
	    }} catch (RepositoryException e){
		//TODO error management?
	    }
	    return io;
	}

}
