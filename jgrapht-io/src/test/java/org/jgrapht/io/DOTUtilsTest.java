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

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DOTUtilsTest {

	@Test
	public void shouldAcceptIdWithDigits() {
		String idWithDigit = "id3";
		boolean isValid = DOTUtils.isValidID(idWithDigit);
		assertTrue(isValid);
	}

	@Test
	public void shouldRejectIdThatStartsWithDigit() {
		String idThatStartsWithDigit = "3id";
		boolean isValid = DOTUtils.isValidID(idThatStartsWithDigit);
		assertFalse(isValid);
	}

	@Test
	public void shouldAcceptIdThatStartWithUnderscore() {
		String idThatStartsWithUnderscore = "_id3";
		boolean isValid = DOTUtils.isValidID(idThatStartsWithUnderscore);
		assertTrue(isValid);
	}
}