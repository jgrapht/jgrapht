/*
 * (C) Copyright 2018, by Mariusz Smykula and Contributors.
 *
 * JGraphT : a free Java graph-theory library
 *
 * This program and the accompanying materials are dual-licensed under
 * either
 *
 * (a) the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation, or (at your option) any
 * later version.
 *
 * or (per the licensee's choosing)
 *
 * (b) the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation.
 */
package org.jgrapht.io;

import java.io.IOException;
import java.io.Writer;

import org.apache.commons.lang3.text.translate.CharSequenceTranslator;

/**
 * Unqoute string if it was quoted (\"foobar\" -> foobar).
 *
 */
class StringUnquoter extends CharSequenceTranslator {


	private static final char DOUBLE_QUOTE = '\"';

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int translate(final CharSequence input, final int index, final Writer out) throws IOException {
		if (index == 0 && isDoubleQuoted(input)) {
			return 1;
		}

		if (index == input.length() - 1 && isDoubleQuoted(input)) {
			return 1;
		}

		out.write(input.charAt(index));
		return 1;
	}

	private boolean isDoubleQuoted(CharSequence input) {
		return input.charAt(0) == DOUBLE_QUOTE && input.charAt(input.length() - 1) == DOUBLE_QUOTE;
	}

}
