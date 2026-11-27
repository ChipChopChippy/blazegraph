package com.bigdata.rdf.sail.webapp;

import junit.framework.Test;
import junit.framework.TestSuite;

public class TestAll {
    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(TestVirtualThreadRequestDispatch.class);
        suite.addTestSuite(TestServerRuntime.class);
        return suite;
    }
}
