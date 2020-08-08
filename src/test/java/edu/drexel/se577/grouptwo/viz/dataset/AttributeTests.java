package edu.drexel.se577.grouptwo.viz.dataset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class AttributeTests {

    @Test
    void testEnumerated() {
        Attribute.Enumerated first =
            new Attribute.Enumerated("test", "red", "green", "blue");
        Attribute.Enumerated second =
            new Attribute.Enumerated("test", "blue", "green", "red");
        Attribute.Enumerated third =
            new Attribute.Enumerated("test", "blue", "green", "yellow");
        assertEquals(first, second);
        assertTrue(!first.equals(third));
        assertTrue(!second.equals(third));
    }
}
