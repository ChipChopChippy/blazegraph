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

import org.eclipse.rdf4j.common.xml.XMLWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.LinkedList;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleBNode;
import org.eclipse.rdf4j.model.impl.SimpleLiteral;
import org.eclipse.rdf4j.model.impl.SimpleIRI;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.impl.SimpleBinding;
import org.eclipse.rdf4j.query.resultio.sparqlxml.SPARQLResultsXMLWriter;

/**
 * Unit tests the UNION aspects of the {@link BigdataSail} implementation.
 * 
 * @author <a href="mailto:mrpersonick@users.sourceforge.net">Mike Personick</a>
 * @version $Id$
 */
public class TestUnions extends QuadsTestCase {

    protected static final Logger log = LogManager.getLogger(TestUnions.class);
    
    /**
     * 
     */
    public TestUnions() {
    }

    /**
     * @param arg0
     */
    public TestUnions(String arg0) {
        super(arg0);
    }

    /**
     * The dc10: namespace.
     */
    final String DC10 = "http://purl.org/dc/elements/1.0/";
    
    /**
     * The dc11: namespace.
     */
    final String DC11 = "http://purl.org/dc/elements/1.1/";
    
    /**
     * dc10:title
     */
    final IRI DC10_TITLE = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(DC10+"title"); 
    
    /**
     * dc10:creator
     */
    final IRI DC10_CREATOR = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(DC10+"creator"); 
    
    /**
     * dc11:title
     */
    final IRI DC11_TITLE = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(DC11+"title"); 
    
    /**
     * dc11:creator
     */
    final IRI DC11_CREATOR = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(DC11+"creator"); 
    
    /**
     * Tests mapping of UNIONS in SPARQL onto unions in bigdata rules.
     * 
     * @throws Exception 
     */
    public void testUnions() throws Exception {

        final BigdataSail sail = getSail();
        sail.init();
        final BigdataSailRepository repo = new BigdataSailRepository(sail);
        final BigdataSailRepositoryConnection cxn = 
            (BigdataSailRepositoryConnection) repo.getConnection();
        cxn.setAutoCommit(false);
        
        try {
    
            final BNode a = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createBNode("_:a");
            final BNode b = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createBNode("_:b");
            final BNode c = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createBNode("_:c");
/**/            
            cxn.add(
                    a,
                    DC10_TITLE,
                    org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createLiteral("A")
                    );
            cxn.add(
                    a,
                    DC10_CREATOR,
                    org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createLiteral("A")
                    );
            cxn.add(
                    b,
                    DC11_TITLE,
                    org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createLiteral("B")
                    );
            cxn.add(
                    b,
                    DC11_CREATOR,
                    org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createLiteral("B")
                    );
            cxn.add(
                    c,
                    DC10_TITLE,
                    org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createLiteral("C")
                    );
            cxn.add(
                    c,
                    DC11_CREATOR,
                    org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createLiteral("C")
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
                log.info(((BigdataSailRepositoryConnection) cxn).getTripleStore().dumpStore());
            }

            {
                
                String query = 
                    "SELECT ?title ?creator " +
                    "WHERE { " +
                    "  { ?book <"+DC10_TITLE+"> ?title . " +
                    "    ?book <"+DC10_CREATOR+"> ?creator . " +
                    "  } " +
                    "  UNION " +
                    "  { ?book <"+DC11_TITLE+"> ?title ." +
                    "    ?book <"+DC11_CREATOR+"> ?creator . " +
                    "  } " +
                    "}";
                
                final TupleQuery tupleQuery = 
                    cxn.prepareTupleQuery(QueryLanguage.SPARQL, query);
                tupleQuery.setIncludeInferred(true /* includeInferred */);
                TupleQueryResult result = tupleQuery.evaluate();
    
                Collection<BindingSet> answer = new LinkedList<BindingSet>();
                answer.add(createBindingSet(
                        new SimpleBinding("title", org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createLiteral("A")),
                        new SimpleBinding("creator", org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createLiteral("A"))
                        ));
                answer.add(createBindingSet(
                        new SimpleBinding("title", org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createLiteral("B")),
                        new SimpleBinding("creator", org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createLiteral("B"))
                        ));
                
                compare(result, answer);

            }
            
        } finally {
            cxn.close();
            sail.__tearDownUnitTest();
        }

    }
    
    /**
     * Tests mapping of UNIONS in SPARQL onto unions in bigdata rules.
     * 
     * @throws Exception 
     */
    public void testSesameFilters() throws Exception {

        final BigdataSail sail = getSail();
        sail.init();
        final BigdataSailRepository repo = new BigdataSailRepository(sail);
        final BigdataSailRepositoryConnection cxn = 
            (BigdataSailRepositoryConnection) repo.getConnection();
        cxn.setAutoCommit(false);
        
        try {
    
            final IRI jack = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI("_:Jack");
            final IRI jill = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI("_:Jill");
            final IRI person = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI("_:Person");
            final IRI age = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI("_:age");
            final IRI integer = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI("http://www.w3.org/2001/XMLSchema#integer");
/**/            
            cxn.add(
                    jack,
                    RDF.TYPE,
                    person
                    );
            cxn.add(
                    jill,
                    RDF.TYPE,
                    person
                    );
            cxn.add(
                    jack,
                    age,
                    org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createLiteral("40", integer)
                    );
            cxn.add(
                    jill,
                    age,
                    org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createLiteral("30", integer)
                    );
/**/

            /*
             * Note: The either flush() or commit() is required to flush the
             * statement buffers to the database before executing any operations
             * that go around the sail.
             */
            cxn.flush();//commit();
            
/**/            
            log.info("hello");
            if (log.isInfoEnabled()) {
                log.info(cxn.getTripleStore().dumpStore());
            }

            {
                
                String query = 
                    "SELECT * " +
                    "WHERE { " +
                    "  { " +
                    "    ?x <"+RDF.TYPE+"> <"+person+"> . " +
                    "    ?x <"+age+"> ?age1 . " +
                    "    FILTER( ?age1 > 35 ) . " +
                    "  } " +
                    "  UNION " +
                    "  { " +
                    "    ?x <"+RDF.TYPE+"> <"+person+"> . " +
                    "    ?x <"+age+"> ?age2 . " +
                    "    FILTER( ?age2 > 25 ) . " +
                    "  } " +
                    "}";
                
                final StringWriter sw = new StringWriter();
                
                final TupleQuery tupleQuery = 
                    cxn.prepareTupleQuery(QueryLanguage.SPARQL, query);
                tupleQuery.setIncludeInferred(true /* includeInferred */);
                tupleQuery.evaluate(new SPARQLResultsXMLWriter(new XMLWriter(sw)));
    
                if(log.isInfoEnabled())log.info(sw.toString());
            }
            
        } finally {
            cxn.close();
            sail.__tearDownUnitTest();
        }

    }
    
}
