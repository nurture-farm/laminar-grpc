package farm.nurture.laminar.generator;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class UtilitiesTest {

    @Test
    void getTopLinesFromStackTrace() {
        Exception exception = new Exception();
        String trace = Utilities.getTopLinesFromStackTrace(exception, 1);
        assertEquals("farm.nurture.laminar.generator.UtilitiesTest.getTopLinesFromStackTrace(UtilitiesTest.java:11)\n",trace);
    }
}