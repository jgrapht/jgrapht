package org.jgrapht.io;

import static org.junit.Assert.*;

import org.junit.Test;

public class IntegerComponentNameProviderTest
{

    @Test
    public void testConstructors()
    {
        IntegerComponentNameProvider<Object> provider = new IntegerComponentNameProvider<>();
        String id1 = provider.getName(new Object());
        assertEquals("1", id1);
        String id2 = provider.getName(new Object());
        assertEquals("2", id2);

        provider = new IntegerComponentNameProvider<>(0);
        id1 = provider.getName(new Object());
        assertEquals("0", id1);
        id2 = provider.getName(new Object());
        assertEquals("1", id2);
    
    }

    @Test
    public void testClear()
    {
        IntegerComponentNameProvider<Object> provider = new IntegerComponentNameProvider<>();
        String id1 = provider.getName(new Object());
        assertEquals("1", id1);
        String id2 = provider.getName(new Object());
        assertEquals("2", id2);
        provider.clear();
        id1 = provider.getName(new Object());
        assertEquals("1", id1);
        id2 = provider.getName(new Object());
        assertEquals("2", id2);
    }
}
