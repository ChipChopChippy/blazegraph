/*

 Copyright (C) SYSTAP, LLC DBA Blazegraph 2006-2016.  All rights reserved.

 Contact:
 SYSTAP, LLC DBA Blazegraph
 2501 Calvert ST NW #106
 Washington, DC 20008
 licenses@blazegraph.com

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; version 2 of the License.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
/*
 * Created on Apr 21, 2008
 */

package com.bigdata.rdf.model;

import java.util.Date;
import java.math.BigInteger;
import java.math.BigDecimal;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

import javax.xml.datatype.XMLGregorianCalendar;

import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.base.CoreDatatype;
import org.eclipse.rdf4j.model.datatypes.XMLDatatypeUtil;

import com.bigdata.cache.WeakValueCache;
import com.bigdata.rdf.internal.IV;
import com.bigdata.rdf.internal.impl.extensions.DateTimeExtension;
import com.bigdata.rdf.internal.impl.literal.XSDUnsignedByteIV;
import com.bigdata.rdf.internal.impl.literal.XSDUnsignedIntIV;
import com.bigdata.rdf.internal.impl.literal.XSDUnsignedLongIV;
import com.bigdata.rdf.internal.impl.literal.XSDUnsignedShortIV;
import com.bigdata.rdf.lexicon.LexiconRelation;
import com.bigdata.util.concurrent.CanonicalFactory;

/**
 * An implementation using {@link BigdataValue}s and {@link BigdataStatement}s.
 * Values constructed using this factory do NOT have term identifiers assigned.
 * Statements constructed using this factory do NOT have statement identifiers
 * assigned. Those metadata can be resolved against the various indices and then
 * set on the returned values and statements.
 * 
 * @todo Consider a {@link WeakValueCache} on this simple factory to avoid duplicate
 *       values.
 * 
 * @todo Consider a {@link WeakValueCache} to shortcut recently used statements?
 * 
 * @author <a href="mailto:thompsonbry@users.sourceforge.net">Bryan Thompson</a>
 */
public class BigdataSimpleValueFactory implements BigdataValueFactory {

	private final String namespace;
	
	@Override
	public String getNamespace() {
		
	    if (namespace != null) {
	        
	        return namespace;
	        
	    } else {
	        
	        throw new RuntimeException("Headless value factory should not be asked for its namespace");
	        
	    }
		
	}
	
    /**
     * WARNING: Use {@link #getInstance(String)} NOT this constructor.
     * <p>
     * WARNING: This constructor provides 'headless' (not associated with any
     * namespace) instance of the {@link BigdataValueFactory}, which is used for
     * query/update parsing. It SHOULD NOT be used in code working with
     * triple-store.
     * 
     * @see BLZG-1678 (remove "headless" BigdataValueFactory impl class)
     * @see BLZG-1176 (SPARQL Query/Update parser should not use db connection)
     */
    public BigdataSimpleValueFactory() {

        this(null);

    }

	/**
     * WARNING: Use {@link #getInstance(String)} NOT this constructor.
     */
    private BigdataSimpleValueFactory(final String namespace) {

        this.namespace = namespace;

        xsdMap = getXSDMap();
        
        // @see <a href="http://trac.blazegraph.com/ticket/983"> Concurrent insert data with boolean object causes IllegalArgumentException </a>
        // @see <a href="http://trac.blazegraph.com/ticket/980"> Object position query hint is not a Literal </a>
//        /**
//         * Cache the IV on the BigdataValue for these boolean constants.
//         * 
//         * @see <a href="http://trac.blazegraph.com/ticket/983"> Concurrent insert
//         *      data with boolean object causes IllegalArgumentException </a>
//         */
//        TRUE.setIV(XSDBooleanIV.TRUE);
//        FALSE.setIV(XSDBooleanIV.FALSE);
    }
    
	/**
	 * Canonicalizing mapping for {@link BigdataSimpleValueFactory}s based on the
	 * namespace of the {@link LexiconRelation}.
	 * <p>
	 * Note: The backing LRU should be small (and can be zero) since instances
	 * SHOULD be finalized quickly once they are no longer strongly reachable
	 * (which would imply that there was no {@link LexiconRelation} for that
	 * instance and that all {@link BigdataValueImpl}s for that instance had
	 * become weakly reachable or been swept).
	 */
    private static CanonicalFactory<String/* namespace */, BigdataSimpleValueFactory,String/*State*/> cache = new CanonicalFactory<String, BigdataSimpleValueFactory,String>(
            1/* capacity */) {
				@Override
				protected BigdataSimpleValueFactory newInstance(
						final String key, final String namespace) {
						return new BigdataSimpleValueFactory(namespace);
				}
    };
//    private static WeakValueCache<String/* namespace */, BigdataSimpleValueFactory> cache = new WeakValueCache<String, BigdataSimpleValueFactory>(
//            new LRUCache<String, BigdataSimpleValueFactory>(1/* capacity */));

    /**
     * Return the instance associated with the <i>namespace</i>.
     * <p>
     * Note: This canonicalizing mapping for {@link BigdataSimpleValueFactory}s is
     * based on the namespace of the {@link LexiconRelation}. This makes the
     * instances canonical within a JVM instance, which is all that we care
     * about. The actual assignments of term identifiers to {@link BigdataValue}
     * s is performed by the {@link LexiconRelation} itself and is globally
     * consistent for a given lexicon.
     * 
     * @param namespace
     *            The namespace of the {@link LexiconRelation}.
     * 
     *            TODO This method introduces the possibility that two journals
     *            in the same JVM would share the same
     *            {@link BigdataValueFactory} for a kb with the same namespace.
     *            This is doubtless not desired.  A workaround is to use the
     *            {@link UUID} of the Journal as part of the namespace of the
     *            KB, which would serve to make sure that all KB instances have
     *            distinct namespaces.
     */
	public static BigdataValueFactory/* Impl */getInstance(final String namespace) {

		return cache.getInstance(namespace, namespace/*state*/);
		
	}
	
//    /**
//     * Return the instance associated with the <i>namespace</i>.
//     * <p>
//     * Note: This canonicalizing mapping for {@link BigdataSimpleValueFactory}s is
//     * based on the namespace of the {@link LexiconRelation}. This makes the
//     * instances canonical within a JVM instance, which is all that we care
//     * about. The actual assignments of term identifiers to {@link BigdataValue}s
//     * is performed by the {@link LexiconRelation} itself and is globally
//     * consistent for a given lexicon.
//     * 
//     * @param namespace
//     *            The namespace of the {@link LexiconRelation}.
//     */
//    public static BigdataValueFactory/*Impl*/ getInstance(final String namespace) {
//        
//        if (namespace == null)
//            throw new IllegalArgumentException();
//        
//        synchronized(cache) {
//            
//            BigdataSimpleValueFactory a = cache.get(namespace);
//
//            if (a == null) {
//
//                a = new BigdataSimpleValueFactory();
//
//                cache.put(namespace, a, true/* dirty */);
//                
//            }
//            
//            return a;
//            
//        }
//        
//    }
    
    /**
     * Remove a {@link BigdataSimpleValueFactory} from the canonicalizing mapping.
     * <p>
     * Entries in this canonicalizing mapping for a {@link LexiconRelation} MUST
     * be {@link #remove(String)}ed if the {@link LexiconRelation} is destroyed
     * in case a distinct lexicon is subsequently creating with the same
     * namespace. There is no need to discard an entry during abort processing.
     * 
     */
//    * @param namespace
//    *            The namespace of the {@link LexiconRelation}.
    @Override
    public void remove(/*final String namespace*/) {
        
//        if (namespace == null)
//            throw new IllegalArgumentException();
//        
//        synchronized(cache) {
//        
//            cache.remove(namespace);
//            
//        }

    	cache.remove(namespace);
    	
    }

    @Override
    public String toString() {
        return super.toString()+"{namespace="+namespace+"}";
    }
    
    @Override
    public BNodeContextFactory newBNodeContext() {

        return new BNodeContextFactory(this);
        
    }
    
    /**
     * Returns a new blank node with a globally unique blank node ID based on a
     * {@link UUID}.
     * <p>
     * Note: Since the blank node IDs are random, they tend to be uniformly
     * distributed across the index partition(s). More efficient ordered writes
     * may be realized using {@link #newBNodeContext()} to obtain a derived
     * {@link BigdataValueFactory} instance that is specific to a document that
     * is being loaded into the RDF DB. 
     * 
     * @see #newBNodeContext()
     */
    @Override
    public BigdataSimpleBNode createBNode() {
        
        return createBNode(nextID());

    }

    /**
     * Returns a blank node identifier (ID) based on a random {@link UUID}.
     */
    protected String nextID() {

        return "u"+UUID.randomUUID();

    }
    
    @Override
    public BigdataSimpleBNode createBNode(final String id) {

        return new BigdataSimpleBNode(this, id);

    }

    @Override
    public BigdataSimpleBNode createBNode(final BigdataStatement stmt) {

    	// Subject, predicate, object and context should be processed to use the target value factory
    	// See https://jira.blazegraph.com/browse/BLZG-1875
    	final BigdataResource originalS = stmt.getSubject();
    	final BigdataURI originalP = stmt.getPredicate();
    	final BigdataValue originalO = stmt.getObject();
    	final BigdataResource originalC = stmt.getContext();
    	
    	final BigdataResource s = asValue(originalS);
    	final BigdataURI p = asValue(originalP);
    	final BigdataValue o = asValue(originalO);
    	final BigdataResource c = asValue(originalC);

    	final BigdataStatement effectiveStmt;
    	
		if (originalS != s || originalP != p || originalO != o || originalC != c) {

    		effectiveStmt = new BigdataSimpleStatement(s, p, o, c, stmt.getStatementType(), stmt.getUserFlag());

    	} else {

    		effectiveStmt = stmt;
    		
    	}

		return new BigdataSimpleBNode(this, nextID(), effectiveStmt);
    }

    @Override
    public BigdataSimpleLiteral createLiteral(final String label) {

        return new BigdataSimpleLiteral(this, label, null, null);
        
    }

    /*
     * XSD support. 
     */
    public static final transient String NAMESPACE_XSD = "http://www.w3.org/2001/XMLSchema";
    
    public static final transient String xsd = NAMESPACE_XSD + "#";

    private final BigdataSimpleIRI xsd_string = new BigdataSimpleIRI(this, xsd
            + "string");

    private final BigdataSimpleIRI xsd_dateTime = new BigdataSimpleIRI(this,
            xsd + "dateTime");
    
    private final BigdataSimpleIRI xsd_date = new BigdataSimpleIRI(this,
            xsd + "date");
    
    private final BigdataSimpleIRI xsd_long = new BigdataSimpleIRI(this, xsd
            + "long");

    private final BigdataSimpleIRI xsd_int = new BigdataSimpleIRI(this,
            xsd + "int");

    private final BigdataSimpleIRI xsd_byte = new BigdataSimpleIRI(this, xsd
            + "byte");

    private final BigdataSimpleIRI xsd_short = new BigdataSimpleIRI(this, xsd
            + "short");

    private final BigdataSimpleIRI xsd_ulong = new BigdataSimpleIRI(this, xsd
            + "unsignedLong");

    private final BigdataSimpleIRI xsd_uint = new BigdataSimpleIRI(this,
            xsd + "unsignedInt");

    private final BigdataSimpleIRI xsd_ubyte = new BigdataSimpleIRI(this, xsd
            + "unsignedByte");

    private final BigdataSimpleIRI xsd_ushort = new BigdataSimpleIRI(this, xsd
            + "unsignedShort");

    private final BigdataSimpleIRI xsd_double = new BigdataSimpleIRI(this, xsd
            + "double");

    private final BigdataSimpleIRI xsd_float = new BigdataSimpleIRI(this, xsd
            + "float");

    private final BigdataSimpleIRI xsd_boolean = new BigdataSimpleIRI(this, xsd
            + "boolean");

//    private final BigdataSimpleLiteral TRUE = new BigdataSimpleLiteral(this, "true", null,
//            xsd_boolean);
//
//    private final BigdataSimpleLiteral FALSE = new BigdataSimpleLiteral(this, "false", null,
//            xsd_boolean);

	/**
	 * Map for fast resolution of XSD URIs. The keys are the string values of
	 * the URIs. The values are the URIs.
	 */
    private final Map<String,BigdataSimpleIRI> xsdMap;

    /**
     * Populate and return a map for fast resolution of XSD URIs.
     */
	private Map<String, BigdataSimpleIRI> getXSDMap() {

		final Map<String, BigdataSimpleIRI> map = new LinkedHashMap<String, BigdataSimpleIRI>();

		final BigdataSimpleIRI[] a = new BigdataSimpleIRI[] { xsd_string,
				xsd_dateTime, xsd_date, xsd_long, xsd_int, xsd_byte, xsd_short,
				xsd_double, xsd_float, xsd_boolean };

		for (BigdataSimpleIRI x : a) {

			// stringValue of IRI => IRI
			map.put(x.stringValue(), x);

		}

		return map;

    }
    
    /**
     * {@inheritDoc}
     * 
     * @see <a href="http://trac.blazegraph.com/ticket/983"> Concurrent insert data
     *      with boolean object causes IllegalArgumentException </a>
     * @see <a href="http://trac.blazegraph.com/ticket/980"> Object position of
     *      query hint is not a Literal </a>
     */
    @Override
    public BigdataSimpleLiteral createLiteral(final boolean arg0) {

        return (arg0 //
                ? new BigdataSimpleLiteral(this, "true", null, xsd_boolean)
                : new BigdataSimpleLiteral(this, "false", null, xsd_boolean)
                );

    }

    @Override
    public BigdataSimpleLiteral createLiteral(byte arg0) {

        return new BigdataSimpleLiteral(this, "" + arg0, null, xsd_byte);

    }

    @Override
    public BigdataSimpleLiteral createLiteral(byte arg0, final boolean unsigned) {

        return new BigdataSimpleLiteral(this, "" + (unsigned ? XSDUnsignedByteIV.promote(arg0) : arg0), null, unsigned ? xsd_ubyte : xsd_byte);

    }

    @Override
    public BigdataSimpleLiteral createLiteral(short arg0) {

        return new BigdataSimpleLiteral(this, "" + arg0, null, xsd_short);

    }

    @Override
    public BigdataSimpleLiteral createLiteral(short arg0, final boolean unsigned) {

        return new BigdataSimpleLiteral(this, "" + (unsigned ? XSDUnsignedShortIV.promote(arg0) : arg0), null, unsigned ? xsd_ushort :xsd_short);

    }

    @Override
    public BigdataSimpleLiteral createLiteral(int arg0) {

        return new BigdataSimpleLiteral(this, "" + arg0, null, xsd_int);

    }

    @Override
    public BigdataSimpleLiteral createLiteral(int arg0, final boolean unsigned) {

        return new BigdataSimpleLiteral(this, "" +  (unsigned ? XSDUnsignedIntIV.promote(arg0) : arg0), null, unsigned ? xsd_uint :xsd_int);

    }

    @Override
    public BigdataSimpleLiteral createLiteral(long arg0) {

        return new BigdataSimpleLiteral(this, "" + arg0, null, xsd_long);

    }

    @Override
    public BigdataSimpleLiteral createLiteral(final BigInteger arg0) {
        return new BigdataSimpleLiteral(this, arg0.toString(), null,
                createURI("http://www.w3.org/2001/XMLSchema#integer"));
    }

    @Override
    public BigdataSimpleLiteral createLiteral(final BigDecimal arg0) {
        return new BigdataSimpleLiteral(this, arg0.toString(), null,
                createURI("http://www.w3.org/2001/XMLSchema#decimal"));
    }

    @Override
    public BigdataSimpleLiteral createLiteral(long arg0, final boolean unsigned) {

        return new BigdataSimpleLiteral(this, "" + (unsigned ? XSDUnsignedLongIV.promote(arg0) : arg0), null, unsigned ? xsd_ulong : xsd_long);

    }

    @Override
    public BigdataSimpleLiteral createLiteral(float arg0) {

        return new BigdataSimpleLiteral(this, "" + arg0, null, xsd_float);

    }

    @Override
    public BigdataSimpleLiteral createLiteral(double arg0) {

        return new BigdataSimpleLiteral(this, "" + arg0, null, xsd_double);

    }
    
    public BigdataSimpleLiteral createLiteral(final Date date) {
        GregorianCalendar c = new GregorianCalendar();
        c.setTime(date);
        XMLGregorianCalendar xmlGC = 
                DateTimeExtension.datatypeFactorySingleton.newXMLGregorianCalendar(c);
        return createLiteral(xmlGC);
    }

    @Override
    public BigdataSimpleLiteral createLiteral(final XMLGregorianCalendar arg0) {

		/*
		 * Note: QName#toString() does not produce the right representation,
		 * which is why we need to go through XMLDatatypeUtil.
		 * 
		 * @see https://sourceforge.net/apps/trac/bigdata/ticket/117
		 */
        return new BigdataSimpleLiteral(this, arg0.toString(),
                null/* languageCode */, createURI(XMLDatatypeUtil.qnameToURI(
                        arg0.getXMLSchemaType()).stringValue()));
        
    }

    @Override
    public BigdataSimpleLiteral createXSDDateTime(final long timestamp) {
        final TimeZone tz = TimeZone.getDefault()/*getTimeZone("GMT")*/;
        final GregorianCalendar c = new GregorianCalendar(tz);
        c.setGregorianChange(new Date(Long.MIN_VALUE));
        c.setTimeInMillis(timestamp);
        
        final XMLGregorianCalendar xmlGC = 
                DateTimeExtension.datatypeFactorySingleton.newXMLGregorianCalendar(c);
        return createLiteral(xmlGC);
    }


    @Override
    public BigdataSimpleLiteral createLiteral(final String label, final String language) {

        return new BigdataSimpleLiteral(this, label, language, null/* datatype */);

    }

    @Override
    public BigdataSimpleLiteral createLiteral(final String label, IRI datatype) {

        return createLiteral(label, datatype, (String) null);
    }

    @Override
    public BigdataSimpleLiteral createLiteral(String label, IRI datatype, String language) {
        

        /*
         * Note: The datatype parameter may be null per the Sesame API.
         * 
         * See https://sourceforge.net/apps/trac/bigdata/ticket/226
         */
        if (language != null)
            datatype = null;

        if (datatype != null && !(datatype instanceof BigdataSimpleIRI)) {

            datatype = createURI(datatype.stringValue());
            
        }

        return new BigdataSimpleLiteral(this, label, language,
                (BigdataSimpleIRI) datatype);
    }

    @Override
    public BigdataSimpleLiteral createLiteral(final String label, final IRI datatype,
            final CoreDatatype coreDatatype) {
        return createLiteral(label, datatype != null ? datatype : coreDatatype.getIri(),
                (String) null);
    }

    @Override
    public BigdataSimpleLiteral createLiteral(final String label, final CoreDatatype coreDatatype) {
        // RDF4J parsers create plain literals this way (no explicit datatype).
        if (coreDatatype == CoreDatatype.XSD.STRING)
            return createLiteral(label, (String) null);

        return createLiteral(label, coreDatatype.getIri(), (String) null);
    }

    @Override
    public BigdataSimpleIRI createURI(final String uriString) {

		final String str = uriString;
		
//		if (str.startsWith(NAMESPACE_XSD)) {

			final BigdataSimpleIRI tmp = xsdMap.get(str);
			
			if(tmp != null) {

				// found in canonicalizing map.
				return tmp;
				
			}
    		
//    }
    
        return new BigdataSimpleIRI(this, uriString);

    }

    @Override
    public BigdataSimpleIRI createURI(final String namespace, final String localName) {

        return new BigdataSimpleIRI(this, namespace + localName);

    }

    @Override
    public BigdataSimpleStatement createStatement(Resource s, IRI p, Value o) {

        return createStatement(s, p, o, null/* c */, null/* type */);

    }

    @Override
    public BigdataSimpleStatement createStatement(Resource s, IRI p, Value o,
            Resource c) {

        return createStatement(s, p, o, c, null/* type */);

    }

    @Override
    public BigdataSimpleStatement createStatement(Resource s, IRI p, Value o,
            Resource c, StatementEnum type) {
        
        return createStatement(s, p, o, c, type, false/* userFlag */);
        
    }

    @Override
    public BigdataSimpleStatement createStatement(Resource s, IRI p, Value o,
            Resource c, StatementEnum type, final boolean userFlag) {
        
        return new BigdataSimpleStatement(//
                (BigdataResource) asValue(s),//
                (BigdataURI)      asValue(p),//
                (BigdataValue)    asValue(o),//
                (BigdataResource) asValue(c),// optional
                type, // the statement type (optional).
                userFlag // the user flag (optional)
        );

    }

    @Override
    final public BigdataValue asValue(final Value v) {

        if (v == null)
            return null;

        if (v instanceof BigdataValueImpl
                && ((BigdataValueImpl) v).getValueFactory() == this) {

			final BigdataValueImpl v1 = (BigdataValueImpl) v;

			final IV<?, ?> iv = v1.getIV();

			if (iv == null || !iv.isNullIV()) {

				/*
				 * A value from the same value factory whose IV is either
				 * unknown or defined (but not a NullIV or DummyIV).
				 */

				return (BigdataValue) v;

			}

        }

        if (v instanceof Literal
                && org.eclipse.rdf4j.model.vocabulary.XSD.BOOLEAN.equals(((Literal) v).getDatatype())) {
            return createLiteral(((Literal) v).booleanValue());

        } else if (v instanceof IRI) {
        	
            return createURI(((IRI) v).stringValue());
            
        } else if (v instanceof BigdataBNode && ((BigdataBNode)v).isStatementIdentifier()) {

       		return createBNode(((BigdataBNode) v).getStatement());

        } else if (v instanceof BNode) {

            return createBNode(((BNode) v).stringValue());

        } else if (v instanceof Literal) {

            final Literal tmp = ((Literal) v);

            final String label = tmp.getLabel();

            final String language = tmp.getLanguage().orElse(null);

            final IRI datatype = tmp.getDatatype();

            return new BigdataSimpleLiteral(//
                    this,// Note: Passing in this factory!
                    label,//
                    language,//
                    (language != null || datatype == null
                            || (!(v instanceof BigdataValue) && org.eclipse.rdf4j.model.vocabulary.XSD.STRING.stringValue().equals(datatype.stringValue()))) // foreign RDF 1.1 xsd:string is plain
                            ? null : (BigdataURI)asValue(datatype)//
                    );

        } else {

            throw new AssertionError();

        }

    }
    
    /**
     * (De-)serializer paired with this {@link BigdataSimpleValueFactory}.
     */
    private final transient BigdataValueSerializer<BigdataValue> valueSer = new BigdataValueSerializer<BigdataValue>(
            this);

    @Override
    public BigdataValueSerializer<BigdataValue> getValueSerializer() {

        return valueSer;

    }

    @Override
    public BigdataResource asValue(Resource v) {

        return (BigdataResource) asValue((Value) v);
        
    }

    @Override
    public BigdataURI asValue(IRI v) {
        
        return (BigdataURI)asValue((Value)v);
        
    }

    @Override
    public BigdataLiteral asValue(Literal v) {
        
        return (BigdataLiteral)asValue((Value)v);
        
    }

    @Override
    public BigdataBNode asValue(BNode v) {

        return (BigdataBNode)asValue((Value)v);
        
    }
    
}
