/**

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
 * Created on Oct 13, 2011
 */

package com.bigdata.rdf.sail.webapp.client;

import junit.framework.TestCase2;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleIRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;

/**
 * Test suite for utility class to encode and decode RDF Values for interchange
 * via the REST API.
 * 
 * @see EncodeDecodeValue
 * 
 * @author <a href="mailto:thompsonbry@users.sourceforge.net">Bryan Thompson</a>
 */
public class TestEncodeDecodeValue extends TestCase2 {

   private static final Logger log = LogManager.getLogger(TestEncodeDecodeValue.class);
   
    /**
     * 
     */
    public TestEncodeDecodeValue() {
    }

    /**
     * @param name
     */
    public TestEncodeDecodeValue(String name) {
        super(name);
    }

//    public void test_encodeDecode_escapeCodeSequence() {
//
//        assertEquals("\"",EncodeDecodeValue.decodeEscapeSequences("\\u))
//        // doEscapeCodeSequenceTest("","");
//        fail("write tests");
//        
//    }
    
    public void test_encodeDecode_URI() {
        
        doTest("<http://www.bigdata.com>",
                org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI("http://www.bigdata.com"));

        doTest("<" + RDF.TYPE.stringValue() + ">", RDF.TYPE);

        doTest("<http://xmlns.com/foaf/0.1/Person>", org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(
                "http://xmlns.com/foaf/0.1/Person"));
        
    }

    public void test_encodeDecode_Literal() {
        
        doTest("\"abc\"", org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createLiteral("abc"));

        doTest("\"\"", org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createLiteral(""));

        assertEquals(org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createLiteral("ab\"c"),
                EncodeDecodeValue.decodeValue("\"ab\"c\""));

        assertEquals(org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createLiteral("ab\"c"),
                EncodeDecodeValue.decodeValue("\"ab\"c\""));

    }
    
    /**
     * Unit tests for a literal which uses single quotes in its encoded form.
     * For example <code>'abc'</code>.
     */
    public void test_encodeDecode_Literal_singleQuotes() {
        
        assertEquals(org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createLiteral("abc"),
                EncodeDecodeValue.decodeValue("'abc'"));

        assertEquals(org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createLiteral(""),
                EncodeDecodeValue.decodeValue("''"));

        Literal l3 = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createLiteral("ab\"c");
        Value v3 = EncodeDecodeValue.decodeValue("'ab\"c'");
        assertEquals(l3, v3);

        assertEquals(org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createLiteral("ab'c"),
                EncodeDecodeValue.decodeValue("'ab\'c'"));

        assertEquals(org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createLiteral("ab'c"),
                EncodeDecodeValue.decodeValue("'ab'c'"));

        doTest("\"'ab'c'\"", org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createLiteral("'ab'c'"));
    }

    public void test_encodeDecode_Literal_languageCode() {
        
        doTest("\"abc\"@en", org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createLiteral("abc","en"));
        
        doTest("\"'ab'c'\"@en", org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createLiteral("'ab'c'","en"));

        doTest("\"\"@en", org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createLiteral("","en"));

        try {
            EncodeDecodeValue.decodeValue("\"\"@");
            fail("An empty language tag must be rejected");
        } catch (IllegalArgumentException expected) {
            // Empty language tags are not valid RDF 1.1 language tags.
        }

    }

    public void test_encodeDecode_Literal_datatype() {
        
        doTest("\"abc\"^^<http://www.bigdata.com/dt1>", org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createLiteral("abc",
                org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance()
                        .createIRI("http://www.bigdata.com/dt1")));

        doTest("\"\"^^<http://www.bigdata.com/dt1>", org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createLiteral("",
                org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance()
                        .createIRI("http://www.bigdata.com/dt1")));

        doTest("\"'ab'c'\"^^<http://www.bigdata.com/dt1>", org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createLiteral("'ab\'c'",
                org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance()
                        .createIRI("http://www.bigdata.com/dt1")));

    }

    /**
     * This demonstrates how things could go wrong.
     */
    public void test_encodeDecode_URI_escapeCodeSequence() {
        
        doTest("<http://www.bigdata.com/<>/foo>",
                org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI("http://www.bigdata.com/<>/foo"));
        
         
    }

    /**
     * This demonstrates how things could go wrong. This is a pretty likely
     * scenario and definitely needs to be fixed. The code point for the double
     * quote is 0x0022. The code point for the single quote is 0x0027. The code
     * point for &gt; is 0x003E. The code point for &lt; is 0x003C.
     */
    public void test_encodeDecode_Literal_escapeCodeSequence() {
        
        doTest("\"ab\"c\"", org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createLiteral("ab\"c"));
        doTest("\"ab'c\"", org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createLiteral("ab'c"));
               
        doTest("\"ab&c<>\"", org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createLiteral("ab&c<>"));
                
    }

    /**
     * Round trip encode/decode test.
     * 
     * @param expectedString
     *            The expected external form.
     * @param expectedValue
     *            The given RDF {@link Value}.
     */
    private void doTest(final String expectedString, final Value expectedValue) {

        final String actualString = EncodeDecodeValue
                .encodeValue(expectedValue);

        assertEquals(expectedString, actualString);

        final Value decodedValue = EncodeDecodeValue
                .decodeValue(expectedString);
        
        assertEquals(expectedValue, decodedValue);

        if (log.isInfoEnabled()) {
            log.info("expected=" + expectedString);
            log.info("encoded =" + actualString);
        }

    }

   /*
    * Test suite for encode/decode of openrdf style contexts. 
    */
    
    /**
    * Test case for <code>foo(s,p,o,(Resource[]) null)</code>. This is case is
    * disallowed.
    * 
    * @see com.bigdata.rdf.store.BD#NULL_GRAPH
    * @see <a href="http://trac.bigdata.com/ticket/1177"> Resource... contexts
    *      not encoded/decoded according to openrdf semantics (REST API) </a>
    */
    public void test_encodeDecodeContexts_quads_context_null_array() {

       // rejected by encode.
       try {
          EncodeDecodeValue.encodeContexts(null);
          fail("Expecting " + IllegalArgumentException.class);
       } catch (IllegalArgumentException ex) {
          if (log.isInfoEnabled())
             log.info("Ignoring expected exception: " + ex);
       }

       // rejected by decode.
       try {
          EncodeDecodeValue.decodeContexts(null);
          fail("Expecting " + IllegalArgumentException.class);
       } catch (IllegalArgumentException ex) {
          if (log.isInfoEnabled())
             log.info("Ignoring expected exception: " + ex);
       }

   }

   /**
    * Test case for <code>foo(s,p,o)</code>. This is case is equivalent to
    * <code>foo(s,p,o,new Resource[0]). It is allowed and refers
    * to all named graphs.
    * 
    * @see com.bigdata.rdf.store.BD#NULL_GRAPH
    * @see <a href="http://trac.bigdata.com/ticket/1177"> Resource... contexts
    *      not encoded/decoded according to openrdf semantics (REST API) </a>
    */
   public void test_encodeDecodeContexts_quads_context_not_specified() {

      final String[] encoded = EncodeDecodeValue
            .encodeContexts(new Resource[0]);

      assertEquals(new String[0], encoded);

      final Resource[] decoded = EncodeDecodeValue.decodeContexts(encoded);

      assertEquals(new Resource[0], decoded);

   }

   /**
    * Test case for <code>foo(s,p,o,(Resource)null)</code>. This is case is
    * equivalent to
    * <code>foo(s,p,o,new Resource[]{null}). It is allowed and refers
    * to the "null" graph.
    * 
    * @see com.bigdata.rdf.store.BD#NULL_GRAPH
    * @see <a href="http://trac.bigdata.com/ticket/1177"> Resource... contexts
    *      not encoded/decoded according to openrdf semantics (REST API) </a>
    */
   public void test_encodeDecodeContexts_quads_context_nullGraph() {

      final String[] encoded = EncodeDecodeValue
            .encodeContexts(new Resource[] { null });

      assertEquals(new String[] { null }, encoded);

      final Resource[] decoded = EncodeDecodeValue.decodeContexts(encoded);

      assertEquals(new Resource[] { null }, decoded);

   }

   /**
    * Test case for <code>foo(s,p,o,(Resource)null)</code>. This is case is
    * equivalent to
    * <code>foo(s,p,o,new Resource[]{null}). It is allowed and refers
    * to the "null" graph.
    * <p>
    * Note: When a <code>null</code> is encoded into an HTTP parameter it will
    * be represented as <code>c=</code>. On decode, the value will be a zero
    * length string. Therefore this test verifies that we decode a zero length
    * string into a <code>null</code>.
    * 
    * @see com.bigdata.rdf.store.BD#NULL_GRAPH
    * @see <a href="http://trac.bigdata.com/ticket/1177"> Resource... contexts
    *      not encoded/decoded according to openrdf semantics (REST API) </a>
    */
   public void test_encodeDecodeContexts_quads_context_nullGraph_2() {

      final String[] encoded = new String[] { "" };

      final Resource[] decoded = EncodeDecodeValue.decodeContexts(encoded);

      assertEquals(new Resource[] { null }, decoded);

   }

   /**
    * Test case for <code>foo(s,p,o,x,y,z)</code>. This is case is equivalent to
    * <code>foo(s,p,o,new Resource[]{x,y,z}). It is allowed and refers
    * to the specified named graphs.
    * 
    * @see com.bigdata.rdf.store.BD#NULL_GRAPH
    * @see <a href="http://trac.bigdata.com/ticket/1177"> Resource... contexts
    *      not encoded/decoded according to openrdf semantics (REST API) </a>
    */
   public void test_encodeDecodeContexts_quads_context_x_y_z() {

      final ValueFactory vf = SimpleValueFactory.getInstance();
      final IRI x = vf.createIRI(":x");
      final IRI y = vf.createIRI(":y");
      final IRI z = vf.createIRI(":z");

      final String[] encoded = EncodeDecodeValue.encodeContexts(new Resource[] {
            x, y, z });

      // Note: Encoding wraps IRI with <...>
      assertEquals(new String[] { "<:x>", "<:y>", "<:z>" }, encoded);

      final Resource[] decoded = EncodeDecodeValue.decodeContexts(encoded);

      assertEquals(new Resource[] { x, y, z }, decoded);

   }

   /**
    * Test case for <code>foo(s,p,o,x,null,z)</code>. This is case is equivalent
    * to <code>foo(s,p,o,new Resource[]{x,null,z}). It is allowed and refers
    * to the named graph (x), the null graph, and the named graph (z).
    * 
    * @see com.bigdata.rdf.store.BD#NULL_GRAPH
    * @see <a href="http://trac.bigdata.com/ticket/1177"> Resource... contexts
    *      not encoded/decoded according to openrdf semantics (REST API) </a>
    */
   public void test_encodeDecodeContexts_quads_context_x_null_z() {

      final ValueFactory vf = SimpleValueFactory.getInstance();
      final IRI x = vf.createIRI(":x");
      final IRI z = vf.createIRI(":z");

      final String[] encoded = EncodeDecodeValue.encodeContexts(new Resource[] {
            x, null, z });

      // Note: Encoding wraps IRI with <...>
      assertEquals(new String[] { "<:x>", null, "<:z>" }, encoded);

      final Resource[] decoded = EncodeDecodeValue.decodeContexts(encoded);

      assertEquals(new Resource[] { x, null, z }, decoded);

   }


   /**
    * Test case for <code>foo(s,p,o,x,null,z)</code>. This is case is equivalent
    * to <code>foo(s,p,o,new Resource[]{x,null,z}). It is allowed and refers
    * to the named graph (x), the null graph, and the named graph (z).
    * <p>
    * Note: When a <code>null</code> is encoded into an HTTP parameter it will
    * be represented as <code>c=</code>. On decode, the value will be a zero
    * length string. Therefore this test verifies that we decode a zero length
    * string into a <code>null</code>.
    * 
    * @see com.bigdata.rdf.store.BD#NULL_GRAPH
    * @see <a href="http://trac.bigdata.com/ticket/1177"> Resource... contexts
    *      not encoded/decoded according to openrdf semantics (REST API) </a>
    */
   public void test_encodeDecodeContexts_quads_context_x_null_z_2() {

      final ValueFactory vf = SimpleValueFactory.getInstance();
      final IRI x = vf.createIRI(":x");
      final IRI z = vf.createIRI(":z");

      // Note: Encoding wraps IRI with <...>
      final String[] encoded = new String[] { "<:x>", "", "<:z>" };

      final Resource[] decoded = EncodeDecodeValue.decodeContexts(encoded);

      assertEquals(new Resource[] { x, null, z }, decoded);

   }

}
