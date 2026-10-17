package com.bigdata.rdf.model;

import java.util.UUID;

import junit.framework.TestCase2;

import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.SimpleBNode;
import org.eclipse.rdf4j.model.impl.SimpleLiteral;
import org.eclipse.rdf4j.model.impl.SimpleIRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.XSD;

import com.bigdata.io.SerializerUtil;

/**
 * Test suite for {@link BigdataValueImpl} serialization semantics, including
 * the correct recovery of the {@link BigdataSimpleValueFactory} reference when
 * deserialized.
 * 
 * @see BigdataValueSerializer
 */
public class TestBigdataValueSerialization extends TestCase2 {

	public TestBigdataValueSerialization() {
	}

	public TestBigdataValueSerialization(String name) {
		super(name);
	}

    /**
     * Fixture under test.
     */
    private BigdataValueSerializer<Value> fixture = null;
    
    protected void setUp() throws Exception {
        
        super.setUp();
        
        fixture = new BigdataValueSerializer<Value>(
                SimpleValueFactory.getInstance());
        
    }
    
    protected void tearDown() throws Exception {

        fixture = null;
        
        super.tearDown();
        
    }
    
    /**
     * Performs round trip (de-)serialization using
     * {@link BigdataValueSerializer#serialize()} and
     * {@link BigdataValueSerializer#deserialize(byte[])}.
     * 
     * @param o
     *            The {@link Value}
     * 
     * @return The de-serialized {@link Value}.
     */
    private Value roundTrip_tuned(final Value o) {
        
        return fixture.deserialize(fixture.serialize(o));
        
    }
    
    /**
     * Test round trip of some URIs.
     */
    public void test_URIs() {

        final IRI a = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI("http://www.bigdata.com");
        
        assertEquals(a, roundTrip_tuned(a));
        
    }
    
    /**
     * Test round trip of some plain literals.
     */
    public void test_plainLiterals() {

        final Literal a = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createLiteral("bigdata");
        
        assertEquals(a, roundTrip_tuned(a));
        
    }
    
    /**
     * Test round trip of some language code literals.
     */
    public void test_langCodeLiterals() {

        final Literal a = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createLiteral("bigdata","en");
        
        assertEquals(a, roundTrip_tuned(a));
        
    }

    /**
     * Test round trip of some datatype literals.
     */
    public void test_dataTypeLiterals() {

        final Literal a = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createLiteral("bigdata", XSD.INT);
        
        assertEquals(a, roundTrip_tuned(a));
        
    }

    /*
     * Note: BNode serialization has been disabled since we never write
     * them on the database.
     */
    /**
     * Test round trip of some bnodes.
     */
    public void test_bnodes() {

        final BNode a = SimpleValueFactory.getInstance().createBNode(UUID.randomUUID().toString());
        
        assertEquals(a, roundTrip_tuned(a));
        
    }

	public void test_roundTrip_URI() {

		doRoundTripTest(org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI("http://www.bigdata.com"));
		
	}

	public void test_roundTrip_BNode() {

        doRoundTripTest(SimpleValueFactory.getInstance().createBNode("12"));

        doRoundTripTest(SimpleValueFactory.getInstance().createBNode(UUID.randomUUID().toString()));
		
	}

	public void test_roundTrip_plainLiteral() {

		doRoundTripTest(org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createLiteral("bigdata"));
		
	}

    public void test_roundTrip_langCodeLiterals() {

        doRoundTripTest(org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createLiteral("bigdata", "en"));

    }
	
	public void test_roundTrip_xsd_string() {

		doRoundTripTest(org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createLiteral("bigdata", XSD.STRING));

	}

	public void test_roundTrip_xsd_int() {

		doRoundTripTest(org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createLiteral("12", XSD.INT));

	}

    public void test_roundTrip_veryLargeLiteral() {

        final int len = 1024000;

        final StringBuilder sb = new StringBuilder(len);

        for (int i = 0; i < len; i++) {

            sb.append(Character.toChars('A' + (i % 26)));

        }

        final String s = sb.toString();

        if (log.isInfoEnabled())
            log.info("length(s)=" + s.length());
        
	    doRoundTripTest(org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createLiteral(s));
	    
	}
	
	private void doRoundTripTest(final Value v) {
		
		final String namespace = getName();
		
		final BigdataValueFactory f = BigdataSimpleValueFactory.getInstance(namespace);

		// same reference (singleton pattern).
		assertTrue(f == BigdataSimpleValueFactory.getInstance(namespace));

		// Coerce into a BigdataValue.
		final BigdataValue expected = f.asValue(v);

		assertTrue(f == expected.getValueFactory());

		// test default java serialization.
        final BigdataValue actual1 = doDefaultJavaSerializationTest(expected);

        // same value factory reference on the deserialized term.
        assertTrue(f == actual1.getValueFactory());

        // test BigdataValueSerializer
        final BigdataValue actual2 = doBigdataValueSerializationTest(expected);

        // same value factory reference on the deserialized term.
        assertTrue(f == actual2.getValueFactory());

    }

    /**
     * Test of default Java Serialization (on an ObjectOutputStream).
     */
    private BigdataValue doDefaultJavaSerializationTest(
            final BigdataValue expected) {

        // serialize
        final byte[] data = SerializerUtil.serialize(expected);

        // deserialize
        final BigdataValue actual = (BigdataValue) SerializerUtil
                .deserialize(data);

        // Values compare as equal.
        assertTrue(expected.equals(actual));

        return actual;

    }

    /**
     * Test of {@link BigdataValueSerializer}.
     */
    private BigdataValue doBigdataValueSerializationTest(
            final BigdataValue expected) {

        final BigdataValueSerializer<BigdataValue> ser = expected
                .getValueFactory().getValueSerializer();
        
        // serialize
        final byte[] data = ser.serialize(expected);

        // deserialize
        final BigdataValue actual = ser.deserialize(data);

        // Values compare as equal.
        assertTrue(expected.equals(actual));

        return actual;

    }

}
