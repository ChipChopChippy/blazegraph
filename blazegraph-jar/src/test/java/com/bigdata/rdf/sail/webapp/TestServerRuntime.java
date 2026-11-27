package com.bigdata.rdf.sail.webapp;

import junit.framework.TestCase;

public class TestServerRuntime extends TestCase {

    public void testSupportedVersions() {
        ServerRuntime.requireVersion(27);
        ServerRuntime.requireVersion(28);
    }

    public void testOlderVersionIsRejected() {
        try {
            ServerRuntime.requireVersion(26);
            fail("Expected Java 26 to be rejected");
        } catch (IllegalStateException ex) {
            assertEquals("Blazegraph server requires Java 27 or later; running Java 26",
                    ex.getMessage());
        }
    }

    public void testWebApplicationRejectsUnsupportedRuntime() {
        if (Runtime.version().feature() >= 27)
            return;
        try {
            new BigdataRDFServletContextListener().contextInitialized(null);
            fail("Expected the web application to reject the runtime");
        } catch (IllegalStateException ex) {
            assertTrue(ex.getMessage().contains("requires Java 27 or later"));
        }
    }
}
