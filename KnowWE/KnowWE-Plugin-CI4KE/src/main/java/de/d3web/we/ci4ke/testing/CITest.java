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

package de.d3web.we.ci4ke.testing;

import java.util.List;
import java.util.concurrent.Callable;

import de.d3web.we.ci4ke.handling.CIConfig;

/**
 * Any implementing class has to implement the call() method which results a
 * TestResult.
 * 
 * @author Marc
 * 
 */
public interface CITest extends Callable<CITestResult> {

	/**
	 * This is the old init() method which should get removed soon!
	 */
	public void init(CIConfig config);

	/**
	 * This new setParameters method sets the List of Parameters for a specific
	 * test directly
	 * 
	 * @created 14.11.2010
	 * @param parameters
	 */
	public void setParameters(List<String> parameters);

}
