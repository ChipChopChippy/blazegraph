package com.bigdata.rdf.rio;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;

import org.eclipse.rdf4j.common.lang.FileFormat;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParserRegistry;
import org.eclipse.rdf4j.rio.RDFWriterRegistry;

public final class RDF4JFormatUtil {

    private RDF4JFormatUtil() {
    }

    public static RDFFormat forFileName(final String fileName) {
        return forFileName(fileName, null);
    }

    public static RDFFormat forFileName(final String fileName, final RDFFormat fallback) {
        return FileFormat.matchFileName(fileName, RDFParserRegistry.getInstance().getKeys())
                .orElse(fallback);
    }

    public static RDFFormat forMIMEType(final String mimeType) {
        return forMIMEType(mimeType, null);
    }

    public static RDFFormat forMIMEType(final String mimeType, final RDFFormat fallback) {
        return FileFormat.matchMIMEType(mimeType, RDFParserRegistry.getInstance().getKeys())
                .orElse(fallback);
    }

    public static RDFFormat valueOf(final String name) {
        for (Field field : RDFFormat.class.getFields()) {
            if (Modifier.isStatic(field.getModifiers()) && field.getType() == RDFFormat.class
                    && field.getName().equals(name)) {
                try {
                    return (RDFFormat) field.get(null);
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException("Cannot access RDF format " + name, e);
                }
            }
        }
        final Collection<RDFFormat> formats = RDFParserRegistry.getInstance().getKeys();
        for (RDFFormat format : formats) {
            if (format.getName().equalsIgnoreCase(name)
                    || format.getDefaultMIMEType().equalsIgnoreCase(name)) {
                return format;
            }
        }
        for (RDFFormat format : RDFWriterRegistry.getInstance().getKeys()) {
            if (format.getName().equalsIgnoreCase(name)
                    || format.getDefaultMIMEType().equalsIgnoreCase(name)) {
                return format;
            }
        }
        throw new IllegalArgumentException("Unknown RDF format: " + name);
    }
}
