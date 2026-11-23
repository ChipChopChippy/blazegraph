package com.bigdata.rdf.rio;

import java.util.Collection;
import java.util.List;

import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.Binding;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryResultHandlerException;
import org.eclipse.rdf4j.query.TupleQueryResultHandlerException;
import org.eclipse.rdf4j.query.impl.MapBindingSet;
import org.eclipse.rdf4j.query.resultio.QueryResultFormat;
import org.eclipse.rdf4j.query.resultio.TupleQueryResultFormat;
import org.eclipse.rdf4j.query.resultio.TupleQueryResultWriter;
import org.eclipse.rdf4j.rio.RioSetting;
import org.eclipse.rdf4j.rio.WriterConfig;

/**
 * Wraps a {@link TupleQueryResultWriter} so that Blazegraph plain and
 * language-tagged literals (null datatype) are presented to RDF4J writers as
 * standard literals.
 */
public class LiteralNormalizingTupleQueryResultWriter implements TupleQueryResultWriter {

    private static final ValueFactory vf = SimpleValueFactory.getInstance();

    private final TupleQueryResultWriter delegate;

    public LiteralNormalizingTupleQueryResultWriter(final TupleQueryResultWriter delegate) {
        this.delegate = delegate;
    }

    private static Value normalize(final Value v) {
        if (v instanceof Literal && ((Literal) v).getDatatype() == null) {
            final Literal l = (Literal) v;
            return l.getLanguage().isPresent()
                    ? vf.createLiteral(l.getLabel(), l.getLanguage().get())
                    : vf.createLiteral(l.getLabel());
        }
        return v;
    }

    @Override
    public void handleSolution(final BindingSet bs) throws TupleQueryResultHandlerException {
        final MapBindingSet out = new MapBindingSet();
        for (Binding b : bs) {
            out.addBinding(b.getName(), normalize(b.getValue()));
        }
        delegate.handleSolution(out);
    }

    @Override
    public void handleBoolean(final boolean value) throws QueryResultHandlerException {
        delegate.handleBoolean(value);
    }

    @Override
    public void handleLinks(final List<String> linkUrls) throws QueryResultHandlerException {
        delegate.handleLinks(linkUrls);
    }

    @Override
    public void startQueryResult(final List<String> bindingNames) throws TupleQueryResultHandlerException {
        delegate.startQueryResult(bindingNames);
    }

    @Override
    public void endQueryResult() throws TupleQueryResultHandlerException {
        delegate.endQueryResult();
    }

    @Override
    public TupleQueryResultFormat getTupleQueryResultFormat() {
        return delegate.getTupleQueryResultFormat();
    }

    @Override
    public QueryResultFormat getQueryResultFormat() {
        return delegate.getQueryResultFormat();
    }

    @Override
    public void handleNamespace(final String prefix, final String uri) throws QueryResultHandlerException {
        delegate.handleNamespace(prefix, uri);
    }

    @Override
    public void startDocument() throws QueryResultHandlerException {
        delegate.startDocument();
    }

    @Override
    public void handleStylesheet(final String stylesheetUrl) throws QueryResultHandlerException {
        delegate.handleStylesheet(stylesheetUrl);
    }

    @Override
    public void startHeader() throws QueryResultHandlerException {
        delegate.startHeader();
    }

    @Override
    public void endHeader() throws QueryResultHandlerException {
        delegate.endHeader();
    }

    @Override
    public void setWriterConfig(final WriterConfig config) {
        delegate.setWriterConfig(config);
    }

    @Override
    public WriterConfig getWriterConfig() {
        return delegate.getWriterConfig();
    }

    @Override
    public Collection<RioSetting<?>> getSupportedSettings() {
        return delegate.getSupportedSettings();
    }
}
