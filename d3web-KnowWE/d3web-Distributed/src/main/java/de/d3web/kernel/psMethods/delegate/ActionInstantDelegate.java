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

package de.d3web.kernel.psMethods.delegate;

import java.util.ArrayList;

import de.d3web.core.inference.RuleAction;
import de.d3web.core.terminology.NamedObject;

public class ActionInstantDelegate extends AbstractActionDelegate {
	
	private static final long serialVersionUID = 7810881477953980445L;

	@Override
	public RuleAction copy() {
		ActionInstantDelegate result = new ActionInstantDelegate();
		result.setNamedObjects(new ArrayList<NamedObject>(getNamedObjects()));
		result.setTargetNamespace(new String(getTargetNamespace()));
		return result;
	}
	
	
	
	public int hashCode() {
		if(getNamedObjects() != null)
			return (getNamedObjects().hashCode()) + 37 * getTargetNamespace().hashCode();
		return 0;
	}
	
	public boolean equals(Object o) {
		if (o==this) {
			return true;
		}
		if (o instanceof ActionInstantDelegate) {
			ActionInstantDelegate a = (ActionInstantDelegate)o;
			return isSame(a.getNamedObjects(), getNamedObjects()) 
				&& getTargetNamespace().equals(a.getTargetNamespace()) 
				&& (isTemporary() == a.isTemporary());
		} else {
			return false;
		}
	}
}
