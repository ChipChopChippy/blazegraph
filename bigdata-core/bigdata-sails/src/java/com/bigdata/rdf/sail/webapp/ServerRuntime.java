package com.bigdata.rdf.sail.webapp;

final class ServerRuntime {

    private static final int MINIMUM_JAVA_VERSION = 27;

    private ServerRuntime() {
    }

    static void requireSupported() {
        requireVersion(Runtime.version().feature());
    }

    static void requireVersion(int version) {
        if (version < MINIMUM_JAVA_VERSION)
            throw new IllegalStateException("Blazegraph server requires Java "
                    + MINIMUM_JAVA_VERSION + " or later; running Java " + version);
    }
}
