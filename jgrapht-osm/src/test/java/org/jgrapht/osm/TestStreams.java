/*
 * (C) Copyright 2026-2026, by Shai Eilat and Contributors.
 *
 * JGraphT : a free Java graph-theory library
 *
 * See the CONTRIBUTORS.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the
 * GNU Lesser General Public License v2.1 or later
 * which is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1-standalone.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR LGPL-2.1-or-later
 */
package org.jgrapht.osm;

import java.io.*;
import java.nio.charset.*;
import java.util.zip.*;

/**
 * Small test-scope helpers for the OSM reader unit tests.
 *
 * @author Shai Eilat
 */
final class TestStreams
{
    private TestStreams()
    {
    }

    /**
     * Returns a gzip-compressed {@link InputStream} containing the UTF-8 bytes of
     * {@code body}. Used by the reader unit tests to feed synthetic CSVs through the
     * gzip path without committing binary fixtures.
     *
     * @param body the plain text body
     * @return a fresh input stream over the gzipped bytes
     * @throws IOException if gzip encoding fails (in practice, never)
     */
    static InputStream gzipOf(String body) throws IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (GZIPOutputStream out = new GZIPOutputStream(baos)) {
            out.write(body.getBytes(StandardCharsets.UTF_8));
        }
        return new ByteArrayInputStream(baos.toByteArray());
    }
}
