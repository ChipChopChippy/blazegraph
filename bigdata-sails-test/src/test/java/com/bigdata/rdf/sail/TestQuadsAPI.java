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
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleBNode;
import org.eclipse.rdf4j.model.impl.SimpleLiteral;
import org.eclipse.rdf4j.model.impl.SimpleIRI;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.impl.SimpleBinding;

/**
 * Unit tests the quads aspects of the {@link BigdataSail} implementation.
 * Specify
 * <code>-DtestClass=com.bigdata.rdf.sail.TestBigdataSailWithQuads</code> to run
 * this test suite.
 * 
 * @author <a href="mailto:mrpersonick@users.sourceforge.net">Mike Personick</a>
 * @version $Id$
 */
public class TestQuadsAPI extends QuadsTestCase {

    /**
     * 
     */
    public TestQuadsAPI() {
    }

    /**
     * @param arg0
     */
    public TestQuadsAPI(String arg0) {
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
     * Test loads data into two graphs and verifies some access to those
     * graphs.
     * @throws Exception 
     */
    public void test_2graphs() throws Exception {

        final BigdataSail sail = getSail();
        sail.init();
        final BigdataSailRepository repo = new BigdataSailRepository(sail);
        final BigdataSailRepositoryConnection cxn = 
            (BigdataSailRepositoryConnection) repo.getConnection();
        cxn.setAutoCommit(false);
        
        try {
    
            assertEquals(0, cxn.getTripleStore().getNamedGraphCount());
            
            assertFalse(cxn.getContextIDs().hasNext());
            
            final BNode a = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createBNode("_:a");
            final BNode b = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createBNode("_:b");
            final IRI graphA = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI("http://www.bigdata.com/graphA");
            final IRI graphB = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI("http://www.bigdata.com/graphB");
/**/            
            cxn.add(
                    a,
                    org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(FOAF+"name"),
                    org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createLiteral("Alice"),
                    graphA
                    );
            cxn.add(
                    a,
                    org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(FOAF+"mbox"),
                    org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI("mailto:alice@work.example"),
                    graphA
                    );
            /*
             * Graph B.
             */
            cxn.add(
                    b,
                    org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(FOAF+"name"),
                    org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createLiteral("Bob"),
                    graphB
                    );
            cxn.add(
                    b,
                    org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(FOAF+"mbox"),
                    org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI("mailto:bob@work.example"),
                    graphB
                    );
/**/

            /*
             * Note: The either flush() or commit() is required to flush the
             * statement buffers to the database before executing any operations
             * that go around the sail.
             */
            cxn.flush();//commit();
            
            assertEquals(2, cxn.getTripleStore().getNamedGraphCount());
            
            assertSameIterationAnyOrder(new Resource[] { graphA, graphB }, cxn
                    .getContextIDs());

/**/            
            if (log.isInfoEnabled()) {
                log.info("\n" + cxn.getTripleStore().dumpStore());
            }

        } finally {
            cxn.close();
            sail.__tearDownUnitTest();
        }

    }
    
    /**
     * Matt, do you recognize this unit test: TestQuadsAPI#testSCequality()? I
     * think that this may have been something from you dealing with the binding
     * on the S and C positions. Bryan
     * <p>
     * mooseroy: If i remember correctly, early on there was a problem when you
     * had something like graph ?s{?s ?p ?o} where it wasn't ensuring the ?s in
     * the context and subject position where equal.
     */
    public void testSCequality() throws Exception {

        final BigdataSail sail = getSail();
        sail.init();
        final BigdataSailRepository repo = new BigdataSailRepository(sail);
        final BigdataSailRepositoryConnection cxn = 
            (BigdataSailRepositoryConnection) repo.getConnection();
        cxn.setAutoCommit(false);
        
        try {
    
            assertEquals(0, cxn.getTripleStore().getNamedGraphCount());
            
            assertFalse(cxn.getContextIDs().hasNext());
            
//            final BNode a = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createBNode("_:a");
//            final BNode b = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createBNode("_:b");
            final IRI graphA = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI("http://www.bigdata.com/rdf#graphA");
            final IRI graphB = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI("http://www.bigdata.com/rdf#graphB");
            final IRI s = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI("http://www.bigdata.com/rdf#s");
            final IRI p1 = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI("http://www.bigdata.com/rdf#p1");
            final IRI o1 = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI("http://www.bigdata.com/rdf#o1");
            final IRI p2 = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI("http://www.bigdata.com/rdf#p2");
            final IRI o2 = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI("http://www.bigdata.com/rdf#o2");

            cxn.add(graphA, p1, o1, graphA);

            cxn.add(     s, p1, o1, graphA);
            
            cxn.add(graphA, p2, o2, graphA);
            
            cxn.add(     s, p2, o2, graphB);

            /*
             * Note: The either flush() or commit() is required to flush the
             * statement buffers to the database before executing any operations
             * that go around the sail.
             */
//            cxn.flush();//commit();
            cxn.commit();
            
            assertEquals(2, cxn.getTripleStore().getNamedGraphCount());
            
            assertSameIterationAnyOrder(new Resource[] { graphA, graphB }, cxn
                    .getContextIDs());

/**/            
            if (log.isInfoEnabled()) {
                log.info("\n" + cxn.getTripleStore().dumpStore());
            }
            
            final String query = 
                "SELECT  * \n" +
                "WHERE { \n"+
                "  GRAPH ?g { \n" +
                "    ?g <"+p1+"> <"+o1+"> . \n" +
                "    ?g <"+p2+"> <"+o2+"> . \n" +
                "  }\n" +
                "}";
            
            final TupleQuery tupleQuery = 
                cxn.prepareTupleQuery(QueryLanguage.SPARQL, query);
            
            tupleQuery.setIncludeInferred(true /* includeInferred */);
            
            final TupleQueryResult result = tupleQuery.evaluate();

            final Collection<BindingSet> answer = new LinkedList<BindingSet>();
            
            answer.add(createBindingSet(
                    new SimpleBinding("g", graphA)));
            
            compare(result, answer);
            

        } finally {
            cxn.close();
            sail.__tearDownUnitTest();
        }

    }
    
}
