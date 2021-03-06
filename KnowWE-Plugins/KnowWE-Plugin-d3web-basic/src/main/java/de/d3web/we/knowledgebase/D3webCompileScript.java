/*
 * Copyright (C) 2013 University Wuerzburg, Computer Science VI
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
package de.d3web.we.knowledgebase;

import de.knowwe.core.compile.OptInIncrementalCompileScript;
import de.knowwe.core.compile.packaging.PackageCompileScript;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;

/**
 * CompileScript for the D3webCompiler.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 17.11.2013
 */
@FunctionalInterface
public interface D3webCompileScript<T extends Type> extends PackageCompileScript<D3webCompiler, T>, OptInIncrementalCompileScript<T> {

	@Override
	default Class<D3webCompiler> getCompilerClass() {
		return D3webCompiler.class;
	}

	@Override
	default void destroy(D3webCompiler compiler, Section<T> section) {
		// nothing to do for now...
	}


}
