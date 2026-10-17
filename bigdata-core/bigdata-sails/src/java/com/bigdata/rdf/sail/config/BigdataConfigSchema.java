package com.bigdata.rdf.sail.config;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

/**
 * Defines constants for the schema which is used by
 * {@link BigdataSailFactory} and {@link BigdataRepositoryFactory}.
 */
public class BigdataConfigSchema {

	/** The bigdata schema namespace 
     * (<tt>http://www.bigdata.com/config/sail/bigdata#</tt>). 
     */
	public static final String NAMESPACE = 
            "http://www.bigdata.com/config/sail/bigdata#";

	/** <tt>http://www.bigdata.com/config/sail/bigdata#properties</tt> */
	public final static IRI PROPERTIES;

	static {
		ValueFactory factory = SimpleValueFactory.getInstance();
		PROPERTIES = factory.createIRI(NAMESPACE, "properties");
	}
}
