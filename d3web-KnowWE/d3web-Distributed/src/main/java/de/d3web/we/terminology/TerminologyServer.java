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

package de.d3web.we.terminology;

import java.util.Collection;

import de.d3web.utilities.ISetMap;
import de.d3web.we.basic.IdentifiableInstance;
import de.d3web.we.basic.Information;
import de.d3web.we.basic.InformationType;
import de.d3web.we.basic.TerminologyType;
import de.d3web.we.terminology.global.GlobalTerminology;
import de.d3web.we.terminology.local.LocalTerminologyStorage;

public class TerminologyServer {

	public TerminologyServer() {
		super();
		storage = new LocalTerminologyStorage();
		broker = new TerminologyBroker();
	}

	private LocalTerminologyStorage storage;
	private TerminologyBroker broker;

	public void removeTerminology(String idString, TerminologyType type) {
		storage.signoff(idString, type);
	}

	public GlobalTerminology getGlobalTerminology(TerminologyType type) {
		return broker.getGlobalTerminology(type);
	}

	public Collection<Information> getAlignedInformation(Information info) {
		ISetMap<IdentifiableInstance, IdentifiableInstance> map = broker.getAlignmentMap(info);
		InformationType infoType = info.getInformationType();
		if (InformationType.OriginalUserInformation.equals(infoType)) {
			infoType = InformationType.AlignedUserInformation;
		}
		return Information.toInformation(map, info, infoType);
	}

	public TerminologyBroker getBroker() {
		return broker;
	}

	public LocalTerminologyStorage getStorage() {
		return storage;
	}

}
