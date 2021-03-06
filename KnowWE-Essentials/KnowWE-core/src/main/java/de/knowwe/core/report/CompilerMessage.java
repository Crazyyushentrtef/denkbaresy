/*
 * Copyright (C) 2014 University Wuerzburg, Computer Science VI
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
package de.knowwe.core.report;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import de.knowwe.core.compile.CompileScript;
import de.knowwe.core.kdom.parsing.Section;

/**
 * This is a throwable {@link Message}. It can be used to add a {@link Message} to a {@link Section} while compiling
 * without having to creating and registering it manually. It will be caught by the compiler and registered correctly.
 * <p>
 * <b>Attention:</b> {@link CompilerMessage}s will only be correctly added to
 * the right {@link Section}, if they are thrown from within a {@link CompileScript}.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 09.01.2014
 */
public final class CompilerMessage extends Exception {

	@Serial
	private static final long serialVersionUID = 6181153543632706782L;

	private final Collection<Message> messages;

	/**
	 * Creates a new CompilerMessage exception based on the specified message instances.
	 */
	public CompilerMessage(Collection<Message> messages) {
		this.messages = messages;
	}

	/**
	 * Creates a new CompilerMessage exception based on the specified message instances.
	 */
	public CompilerMessage(Message... messages) {
		this(Arrays.asList(messages));
	}

	private CompilerMessage(Message.Type type, String... messages) {
		this(toMessages(type, messages));
	}

	/**
	 * Throws a newly created CompilerMessage for the specified messages if there is at least one message specified.
	 * Otherwise the method does nothing.
	 *
	 * @param messages the messages to be thrown
	 */
	public static void throwIfPresent(Message... messages) throws CompilerMessage {
		if (messages == null || messages.length == 0) return;
		throw new CompilerMessage(messages);
	}

	/**
	 * Throws a newly created CompilerMessage for the specified messages if there is at least one message specified.
	 * Otherwise (if the collection is empty of null) the method does nothing.
	 *
	 * @param messages the messages to be thrown
	 */
	public static void throwIfPresent(Collection<Message> messages) throws CompilerMessage {
		if (messages == null || messages.isEmpty()) return;
		throw new CompilerMessage(messages);
	}

	private static Collection<Message> toMessages(Message.Type type, String... messageTexts) {
		Collection<Message> messages = new ArrayList<>(messageTexts.length);
		for (String message : messageTexts) {
			messages.add(new Message(type, message));
		}
		return messages;
	}

	/**
	 * Creates a compiler message instance for the specified error message(s)
	 *
	 * @param errors the error message(s) to create the compiler message for
	 * @return the created compiler message
	 * @created 18.01.2014
	 */
	public static CompilerMessage error(String... errors) {
		return new CompilerMessage(Message.Type.ERROR, errors);
	}

	/**
	 * Creates a compiler message instance for the specified warning message(s)
	 *
	 * @param warnings the warning message(s) to create the compiler message for
	 * @return the created compiler message
	 * @created 18.01.2014
	 */
	public static CompilerMessage warning(String... warnings) {
		return new CompilerMessage(Message.Type.WARNING, warnings);
	}

	/**
	 * Creates a compiler message instance for the specified info message(s)
	 *
	 * @param infos the info message(s) to create the compiler message for
	 * @return the created compiler message
	 * @created 18.01.2014
	 */
	public static CompilerMessage info(String... infos) {
		return new CompilerMessage(Message.Type.INFO, infos);
	}

	public Collection<Message> getMessages() {
		return this.messages;
	}

	@Override
	public String getMessage() {
		return getMessages().stream().map(Message::getVerbalization).collect(Collectors.joining(", "));
	}
}
