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

import org.apache.commons.lang3.text.translate.AggregateTranslator;
import org.apache.commons.lang3.text.translate.CharSequenceTranslator;
import org.apache.commons.lang3.text.translate.LookupTranslator;

class DOTIDEscaper {

	private static final String[][] DOUBLE_QUOTE_UNESCAPE = {
			{"\"", "\\\""}
	};
	private static String[][] DOUBLE_QUOTE_UNESCAPE() { return DOUBLE_QUOTE_UNESCAPE.clone(); }
	private static final CharSequenceTranslator ESCAPE_DOT_ID =
			new AggregateTranslator(
					new StringUnquoter(),
					new LookupTranslator(DOUBLE_QUOTE_UNESCAPE())
			);

	static String escapeDotId(String input) {
		return ESCAPE_DOT_ID.translate(input);
	}


}
