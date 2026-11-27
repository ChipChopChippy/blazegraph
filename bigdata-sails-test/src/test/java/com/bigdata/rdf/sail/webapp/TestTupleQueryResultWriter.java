package com.bigdata.rdf.sail.webapp;

import java.io.ByteArrayOutputStream;

import junit.framework.TestCase;

public class TestTupleQueryResultWriter extends TestCase {

    public void testRegisteredJsonWriter() {
        assertNotNull(BigdataRDFContext.createTupleQueryResultWriter(
                "application/sparql-results+json", new ByteArrayOutputStream()));
    }

    public void testMissingWriterReportsMimeType() {
        final String mimeType = "application/x-unknown-sparql-results";
        try {
            BigdataRDFContext.createTupleQueryResultWriter(mimeType, new ByteArrayOutputStream());
            fail("Expected an error for an unregistered MIME type");
        } catch (IllegalStateException ex) {
            assertEquals("No tuple result writer registered for MIME type: " + mimeType,
                    ex.getMessage());
        }
    }
}
