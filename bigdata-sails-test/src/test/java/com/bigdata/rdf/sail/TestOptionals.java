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
 * Created on Sep 16, 2009
 */

package com.bigdata.rdf.sail;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Properties;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleLiteral;
import org.eclipse.rdf4j.model.impl.SimpleIRI;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.impl.SimpleBinding;

/**
 * Unit tests the optionals aspects of the {@link BigdataSail} implementation.
 * 
 * @author <a href="mailto:mrpersonick@users.sourceforge.net">Mike Personick</a>
 * @version $Id$
 */
public class TestOptionals extends QuadsTestCase {

    /**
     * 
     */
    public TestOptionals() {
    }

    /**
     * @param arg0
     */
    public TestOptionals(String arg0) {
        super(arg0);
    }

    /**
     * The foaf: namespace.
     */
    final String FOAF = "http://xmlns.com/foaf/0.1/";
    
    /**
     * foaf:name
     */
    final IRI FOAF_NAME = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(FOAF+"name"); 
    
    /**
     * foaf:mbox
     */
    final IRI FOAF_MBOX = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(FOAF+"mbox"); 
    
    /**
     * foaf:nick
     */
    final IRI FOAF_NICK = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(FOAF+"nick"); 
    
    /**
     * foaf:PersonalProfileDocument
     */
    final IRI FOAF_PPD = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(FOAF+"PersonalProfileDocument"); 
    
    /**
     * foaf:knows
     */
    final IRI FOAF_KNOWS = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(FOAF+"knows"); 
    
    /**
     * The dc: namespace.
     */
    final String DC = "http://purl.org/dc/elements/1.1/";
    
    /**
     * dc:publisher
     */
    final IRI DC_PUBLISHER = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(DC+"publisher"); 
    
    /**
     * dc:title
     */
    final IRI DC_TITLE = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(DC+"title"); 
    
    
    /**
     * Tests mapping of left joins in SPARQL onto optionals in bigdata rules.
     * 
     * @throws Exception 
     */
    public void testLeftJoins() throws Exception {

        final BigdataSail sail = getSail();
        sail.init();
        final BigdataSailRepository repo = new BigdataSailRepository(sail);
        final BigdataSailRepositoryConnection cxn = 
            (BigdataSailRepositoryConnection) repo.getConnection();
        cxn.setAutoCommit(false);
        
        try {
    
            final IRI book1 = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI("http://www.bigdata.com/rdf#book1");
            final IRI book2 = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI("http://www.bigdata.com/rdf#book2");
            final IRI book3 = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI("http://www.bigdata.com/rdf#book3");
            final IRI price = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI("http://www.bigdata.com/rdf#price");
            final IRI XSD_INTEGER = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI("http://www.w3.org/2001/XMLSchema#integer");
/**/            
            cxn.add(
                    book1,
                    DC_TITLE,
                    org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createLiteral("TITLE 1")
                    );
            cxn.add(
                    book1,
                    price,
                    org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createLiteral("10", XSD_INTEGER)
                    );
            cxn.add(
                    book2,
                    DC_TITLE,
                    org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createLiteral("TITLE 2")
                    );
            cxn.add(
                    book2,
                    price,
                    org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createLiteral("20", XSD_INTEGER)
                    );
            cxn.add(
                    book3,
                    DC_TITLE,
                    org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createLiteral("TITLE 3")
                    );
/**/

            /*
             * Note: The either flush() or commit() is required to flush the
             * statement buffers to the database before executing any operations
             * that go around the sail.
             */
//            cxn.flush();//commit();
            cxn.commit();
            
/**/            
            if (log.isInfoEnabled()) {
                log.info(cxn.getTripleStore().dumpStore());
            }

            String query = 
                "SELECT ?title ?price " +
                "WHERE { " +
                "?book <"+DC_TITLE+"> ?title . " +
                "OPTIONAL { ?book <"+price+"> ?price . } . " +
                "}";
            
            final TupleQuery tupleQuery = 
                cxn.prepareTupleQuery(QueryLanguage.SPARQL, query);
            tupleQuery.setIncludeInferred(true /* includeInferred */);
            TupleQueryResult result = tupleQuery.evaluate();

            Collection<BindingSet> answer = new LinkedList<BindingSet>();
            answer.add(createBindingSet(
                    new SimpleBinding("title", org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createLiteral("TITLE 1")), new SimpleBinding("price", org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createLiteral("10", XSD_INTEGER))));
            answer.add(createBindingSet(
                    new SimpleBinding("title", org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createLiteral("TITLE 2")), new SimpleBinding("price", org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createLiteral("20", XSD_INTEGER))));
            answer.add(createBindingSet(
                    new SimpleBinding("title", org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createLiteral("TITLE 3"))));
            
            compare(result, answer);
            
            
        } finally {
            cxn.close();
            sail.__tearDownUnitTest();
        }

    }
    
    public void testOptional() throws Exception {

        Properties properties = getProperties();
//        properties.put("com.bigdata.rdf.sail.isolatableIndices", "true");
        properties.put("com.bigdata.rdf.store.AbstractTripleStore.axiomsClass", "com.bigdata.rdf.axioms.NoAxioms");
        properties.put("com.bigdata.rdf.sail.truthMaintenance", "false");
        properties.put("com.bigdata.rdf.store.AbstractTripleStore.vocabularyClass", "com.bigdata.rdf.vocab.NoVocabulary");
        properties.put("com.bigdata.rdf.store.AbstractTripleStore.justify", "false");
        
        final BigdataSail sail = getSail(properties);
        sail.init();
        final BigdataSailRepository repo = new BigdataSailRepository(sail);
        final BigdataSailRepositoryConnection cxn = 
            (BigdataSailRepositoryConnection) repo.getConnection();
        cxn.setAutoCommit(false);
        
        try {
    
            final ValueFactory vf = sail.getValueFactory();
            
            cxn.add(vf.createIRI("u:1"), 
                    vf.createIRI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"), 
                    vf.createIRI("u:2"));

            cxn.commit();
            
            String query = 
                "SELECT REDUCED ?subj ?subj_class ?subj_label " +
                "WHERE { " +
                "  ?subj a ?subj_class . " +
                "  OPTIONAL { ?subj <http://www.w3.org/2000/01/rdf-schema#label> ?subj_label } " +
                "}";
            
            TupleQuery q = cxn.prepareTupleQuery(QueryLanguage.SPARQL, query);
            q.setBinding("subj", vf.createIRI("u:1"));
            TupleQueryResult tqr = q.evaluate();
            assertTrue(tqr.hasNext());
            final BindingSet tmp = tqr.next();
            if (log.isInfoEnabled())
                log.info(tmp);
            tqr.close();
            
        } finally {
            cxn.close();
            sail.__tearDownUnitTest();
        }

    }
    
}
