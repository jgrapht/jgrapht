/*
 * (C) Copyright 2021-2021, by Hannes Wellmann and Contributors.
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
package org.jgrapht.util;

import org.jgrapht.graph.*;
import org.jgrapht.util.SupplierUtil.*;
import org.junit.*;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.jgrapht.graph.SerializationTestUtils.*;
import static org.junit.Assert.*;

public class SupplierUtilTest
{

    @Test
    public void testAllPredefinedPublicSupplieres()
        throws Exception
    {
        for (Field publicField : SupplierUtil.class.getFields()) {
            if (publicField.getType() == Supplier.class) {
                Supplier<?> supplier = (Supplier<?>) publicField.get(null);
                try {
                    testSupplier(supplier);
                } catch (Throwable e) { // enhance error message with supplier-field name
                    throw new AssertionError("Test failed for " + publicField.getName(), e);
                }
            }
        }
    }

    @Test
    public void testCreateSupplier()
        throws Exception
    {
        @SuppressWarnings("rawtypes") Supplier<ArrayList> supplier =
            SupplierUtil.createSupplier(ArrayList.class);
        testSupplier(supplier, new ArrayList<>());
    }

    @Test
    public void testCreateSupplier_classWithoutNoArgumentConstructor()
    {
        // SimpleGraph has no no-argument constructor
        @SuppressWarnings("rawtypes") Supplier<SimpleGraph> supplier =
            SupplierUtil.createSupplier(SimpleGraph.class);
        org.junit.Assert.assertThrows(SupplierException.class, () -> supplier.get());
    }

    @Test
    public void testCreateDefaultEdgeSupplier()
        throws Exception
    {
        Supplier<DefaultEdge> supplier = SupplierUtil.createDefaultEdgeSupplier();
        testSupplier(supplier);
    }

    @Test
    public void testCreateDefaultWeightedEdgeSupplier()
        throws Exception
    {
        Supplier<DefaultWeightedEdge> supplier = SupplierUtil.createDefaultWeightedEdgeSupplier();
        testSupplier(supplier);
    }

    @Test
    public void testCreateIntegerSupplier()
        throws Exception
    {
        Supplier<Integer> supplier = SupplierUtil.createIntegerSupplier();
        testSupplier(supplier, 0, 1, 2, 3, 4);
    }

    @Test
    public void testCreateIntegerSupplierInt()
        throws Exception
    {
        Supplier<Integer> supplier = SupplierUtil.createIntegerSupplier(4);
        testSupplier(supplier, 4, 5, 6, 7);
    }

    @Test
    public void testCreateLongSupplier()
        throws Exception
    {
        Supplier<Long> supplier = SupplierUtil.createLongSupplier();
        testSupplier(supplier, 0L, 1L, 2L, 3L, 4L);
    }

    @Test
    public void testCreateLongSupplierLong()
        throws Exception
    {
        Supplier<Long> supplier = SupplierUtil.createLongSupplier(44);
        testSupplier(supplier, 44L, 45L, 46L, 47L);
    }

    @Test
    public void testCreateStringSupplier()
        throws Exception
    {
        Supplier<String> supplier = SupplierUtil.createStringSupplier();
        testSupplier(supplier, "0", "1", "2", "3", "4");
    }

    @Test
    public void testCreateStringSupplierInt()
        throws Exception
    {
        Supplier<String> supplier = SupplierUtil.createStringSupplier(44);
        testSupplier(supplier, "44", "45", "46", "47");
    }

    @Test
    public void testCreateRandomUUIDStringSupplier()
        throws Exception
    {
        Supplier<String> supplier = SupplierUtil.createRandomUUIDStringSupplier();
        testSupplier(supplier);
    }

    @SafeVarargs
    private static <T> void testSupplier(Supplier<T> supplier, T... expectedValues)
        throws Exception
    {
        // Test that the supplier supplies the given sequence of expected values
        Set<T> suppliedValues = new LinkedHashSet<>();
        for (T expectedValue : expectedValues) {
            T value = supplier.get();
            assertThat(value, is(equalTo(expectedValue)));
            assertTrue("Equal value supplied multiple times", suppliedValues.add(value));
            suppliedValues.add(value);
        }

        Supplier<T> deserializeSupplier = serializeAndDeserialize(supplier);

        for (int i = 0; i < TESTED_SUPPLIED_VALUES; i++) {
            T value1 = supplier.get();
            T value2 = deserializeSupplier.get();
            assertThat(value1, is(equalTo(value2)));
        }

    }

    private static final int TESTED_SUPPLIED_VALUES = 5;

    private static <T> void testSupplier(Supplier<T> supplier)
        throws Exception
    {
        // Test that the supplier supplies a sequence of distinct values
        Set<T> suppliedValues = new LinkedHashSet<>();
        while (suppliedValues.size() < TESTED_SUPPLIED_VALUES) {
            T value = supplier.get();
            assertTrue("Equal value supplied multiple times", suppliedValues.add(value));
        }

        Supplier<T> deserializeSupplier = serializeAndDeserialize(supplier);

        for (int i = 0; i < TESTED_SUPPLIED_VALUES; i++) {
            T value1 = supplier.get();
            T value2 = deserializeSupplier.get();
            assertThat(value1, is(not(equalTo(value2))));
        }
    }
}
