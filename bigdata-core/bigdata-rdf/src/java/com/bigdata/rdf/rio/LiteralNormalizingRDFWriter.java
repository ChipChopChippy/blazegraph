package com.bigdata.rdf.rio;

import java.util.Collection;

import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.RioSetting;
import org.eclipse.rdf4j.rio.WriterConfig;

/**
 * Wraps an {@link RDFWriter} so that Blazegraph plain and language-tagged
 * literals, whose {@link Literal#getDatatype()} is <code>null</code>, are
 * presented to RDF4J writers as standard literals (RDF4J 4+ requires a
 * non-null datatype).
 */
public class LiteralNormalizingRDFWriter implements RDFWriter {

    private static final ValueFactory vf = SimpleValueFactory.getInstance();

    private final RDFWriter delegate;

    public LiteralNormalizingRDFWriter(final RDFWriter delegate) {
        this.delegate = delegate;
    }

    public static RDFWriter wrap(final RDFWriter writer) {
        return writer instanceof LiteralNormalizingRDFWriter ? writer
                : new LiteralNormalizingRDFWriter(writer);
    }

    @Override
    public void handleStatement(final Statement st) throws RDFHandlerException {
        final Value o = st.getObject();
        if (o instanceof Literal && ((Literal) o).getDatatype() == null) {
            final Literal l = (Literal) o;
            final Literal n = l.getLanguage().isPresent()
                    ? vf.createLiteral(l.getLabel(), l.getLanguage().get())
                    : vf.createLiteral(l.getLabel());
            delegate.handleStatement(st.getContext() == null
                    ? vf.createStatement(st.getSubject(), st.getPredicate(), n)
                    : vf.createStatement(st.getSubject(), st.getPredicate(), n, st.getContext()));
        } else {
            delegate.handleStatement(st);
        }
    }

    @Override
    public void startRDF() throws RDFHandlerException {
        delegate.startRDF();
    }

    @Override
    public void endRDF() throws RDFHandlerException {
        delegate.endRDF();
    }

    @Override
    public void handleNamespace(final String prefix, final String uri) throws RDFHandlerException {
        delegate.handleNamespace(prefix, uri);
    }

    @Override
    public void handleComment(final String comment) throws RDFHandlerException {
        delegate.handleComment(comment);
    }

    @Override
    public RDFFormat getRDFFormat() {
        return delegate.getRDFFormat();
    }

    @Override
    public RDFWriter setWriterConfig(final WriterConfig config) {
        delegate.setWriterConfig(config);
        return this;
    }

    @Override
    public WriterConfig getWriterConfig() {
        return delegate.getWriterConfig();
    }

    @Override
    public Collection<RioSetting<?>> getSupportedSettings() {
        return delegate.getSupportedSettings();
    }

    @Override
    public <T> RDFWriter set(final RioSetting<T> setting, final T value) {
        delegate.set(setting, value);
        return this;
    }
}
