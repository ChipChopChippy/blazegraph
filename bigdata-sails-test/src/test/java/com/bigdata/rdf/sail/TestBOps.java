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

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleLiteral;
import org.eclipse.rdf4j.model.impl.SimpleIRI;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.model.vocabulary.XSD;
import org.eclipse.rdf4j.query.Binding;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.impl.SimpleBinding;

import com.bigdata.rdf.axioms.NoAxioms;
import com.bigdata.rdf.store.BD;
import com.bigdata.rdf.vocab.NoVocabulary;

/**
 * @author <a href="mailto:mrpersonick@users.sourceforge.net">Mike Personick</a>
 * @version $Id$
 */
public class TestBOps extends ProxyBigdataSailTestCase {

    private static final Logger log = LogManager.getLogger(TestBOps.class);
    
    @Override
    public Properties getProperties() {
        
        final Properties props = super.getProperties();
        
        props.setProperty(BigdataSail.Options.TRUTH_MAINTENANCE, "false");
        props.setProperty(BigdataSail.Options.AXIOMS_CLASS, NoAxioms.class.getName());
        props.setProperty(BigdataSail.Options.VOCABULARY_CLASS, NoVocabulary.class.getName());
        props.setProperty(BigdataSail.Options.JUSTIFY, "false");
        props.setProperty(BigdataSail.Options.TEXT_INDEX, "true");
        
        return props;
        
    }

    /**
     * 
     */
    public TestBOps() {
    }

    /**
     * @param arg0
     */
    public TestBOps(String arg0) {
        super(arg0);
    }

    public void testSimpleJoin() throws Exception {

        final BigdataSail sail = getSail();
        try {
        sail.init();
        final BigdataSailRepository repo = new BigdataSailRepository(sail);
        final BigdataSailRepositoryConnection cxn = 
            (BigdataSailRepositoryConnection) repo.getConnection();
        cxn.setAutoCommit(false);
        
        try {
    
//            final ValueFactory vf = sail.getValueFactory();
            
            final String ns = BD.NAMESPACE;
            
            final IRI mike = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(ns+"Mike");
            final IRI bryan = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(ns+"Bryan");
            final IRI person = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(ns+"Person");
            final IRI likes = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(ns+"likes");
            final IRI rdf = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(ns+"RDF");
            final Literal l1 = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createLiteral("Mike");
            final Literal l2 = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createLiteral("Bryan");
/**/
            cxn.setNamespace("ns", ns);
            
            cxn.add(mike, RDF.TYPE, person);
            cxn.add(mike, likes, rdf);
            cxn.add(mike, RDFS.LABEL, l1);
            cxn.add(bryan, RDF.TYPE, person);
            cxn.add(bryan, likes, rdf);
            cxn.add(bryan, RDFS.LABEL, l2);

            /*
             * Note: The either flush() or commit() is required to flush the
             * statement buffers to the database before executing any operations
             * that go around the sail.
             */
            cxn.flush();//commit();
            cxn.commit();//
            
            if (log.isInfoEnabled()) {
                log.info("\n" + cxn.getTripleStore().dumpStore());
            }

            {
                
                final String query = 
                    "PREFIX rdf: <"+RDF.NAMESPACE+"> " +
                    "PREFIX rdfs: <"+RDFS.NAMESPACE+"> " +
                    "PREFIX ns: <"+ns+"> " +
                    
                    "select * " +
                    "WHERE { " +
                    "  ?s rdf:type ns:Person . " +
                    "  ?s ns:likes ?likes . " +
                    "  ?s rdfs:label ?label . " +
                    "}";
                
                final TupleQuery tupleQuery = 
                    cxn.prepareTupleQuery(QueryLanguage.SPARQL, query);
                final TupleQueryResult result = tupleQuery.evaluate();
                
//                while (result.hasNext()) {
//                    System.err.println(result.next());
//                }
 
                final Collection<BindingSet> solution = new LinkedList<BindingSet>();
                solution.add(createBindingSet(new Binding[] {
                    new SimpleBinding("s", mike),
                    new SimpleBinding("likes", rdf),
                    new SimpleBinding("label", l1)
                }));
                solution.add(createBindingSet(new Binding[] {
                    new SimpleBinding("s", bryan),
                    new SimpleBinding("likes", rdf),
                    new SimpleBinding("label", l2)
                }));
                
                compare(result, solution);
                
            }
        } finally { 
            cxn.close();
        }
        } finally {
            sail.__tearDownUnitTest();
        }

    }
    
    public void testSimpleConstraint() throws Exception {

        final BigdataSail sail = getSail();
        try {
        sail.init();
        final BigdataSailRepository repo = new BigdataSailRepository(sail);
        final BigdataSailRepositoryConnection cxn = 
            (BigdataSailRepositoryConnection) repo.getConnection();
        cxn.setAutoCommit(false);
        
        try {
    
            final ValueFactory vf = sail.getValueFactory();
            
            final String ns = BD.NAMESPACE;
            
            final IRI jill = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(ns+"Jill");
            final IRI jane = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(ns+"Jane");
            final IRI person = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(ns+"Person");
            final IRI age = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(ns+"age");
            final IRI IQ = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(ns+"IQ");
            final Literal l1 = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createLiteral("Jill");
            final Literal l2 = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createLiteral("Jane");
            final Literal age1 = vf.createLiteral(20);
            final Literal age2 = vf.createLiteral(30);
            final Literal IQ1 = vf.createLiteral(130);
            final Literal IQ2 = vf.createLiteral(140);
/**/
            cxn.setNamespace("ns", ns);
            
            cxn.add(jill, RDF.TYPE, person);
            cxn.add(jill, RDFS.LABEL, l1);
            cxn.add(jill, age, age1);
            cxn.add(jill, IQ, IQ1);
            cxn.add(jane, RDF.TYPE, person);
            cxn.add(jane, RDFS.LABEL, l2);
            cxn.add(jane, age, age2);
            cxn.add(jane, IQ, IQ2);

            /*
             * Note: The either flush() or commit() is required to flush the
             * statement buffers to the database before executing any operations
             * that go around the sail.
             */
            cxn.flush();//commit();
            cxn.commit();//
            
            if (log.isInfoEnabled()) {
                log.info("\n" + cxn.getTripleStore().dumpStore());
            }

            {
                
                final String query = 
                    "PREFIX rdf: <"+RDF.NAMESPACE+"> " +
                    "PREFIX rdfs: <"+RDFS.NAMESPACE+"> " +
                    "PREFIX ns: <"+ns+"> " +
                    
                    "select * " +
                    "WHERE { " +
                    "  ?s rdf:type ns:Person . " +
                    "  ?s ns:age ?age . " +
                    "  ?s ns:IQ ?iq . " +
                    "  ?s rdfs:label ?label . " +
                    "  FILTER( ?age < 25 && ?iq > 125 ) . " +
                    "}";
                
                final TupleQuery tupleQuery = 
                    cxn.prepareTupleQuery(QueryLanguage.SPARQL, query);
                final TupleQueryResult result = tupleQuery.evaluate();
                
//                while (result.hasNext()) {
//                    System.err.println(result.next());
//                }
 
                final Collection<BindingSet> solution = new LinkedList<BindingSet>();
                solution.add(createBindingSet(new Binding[] {
                    new SimpleBinding("s", jill),
                    new SimpleBinding("age", age1),
                    new SimpleBinding("iq", IQ1),
                    new SimpleBinding("label", l1)
                }));
                
                compare(result, solution);
                
            }
        } finally {
            cxn.close();
        }
        } finally {
            sail.__tearDownUnitTest();
        }

    }
    
    public void testSimpleOptional() throws Exception {

        final BigdataSail sail = getSail();
        try {
        sail.init();
        final BigdataSailRepository repo = new BigdataSailRepository(sail);
        final BigdataSailRepositoryConnection cxn = 
            (BigdataSailRepositoryConnection) repo.getConnection();
        cxn.setAutoCommit(false);
        
        try {
    
//            final ValueFactory vf = sail.getValueFactory();
            
            final String ns = BD.NAMESPACE;
            
            final IRI mike = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(ns+"Mike");
            final IRI bryan = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(ns+"Bryan");
            final IRI person = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(ns+"Person");
            final IRI likes = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(ns+"likes");
            final IRI rdf = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(ns+"RDF");
            final Literal l1 = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createLiteral("Mike");
//            final Literal l2 = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createLiteral("Bryan");
/**/
            cxn.setNamespace("ns", ns);
            
            cxn.add(mike, RDF.TYPE, person);
            cxn.add(mike, likes, rdf);
            cxn.add(mike, RDFS.LABEL, l1);
            cxn.add(bryan, RDF.TYPE, person);
            cxn.add(bryan, likes, rdf);
//            cxn.add(bryan, RDFS.LABEL, l2);

            /*
             * Note: The either flush() or commit() is required to flush the
             * statement buffers to the database before executing any operations
             * that go around the sail.
             */
            cxn.flush();//commit();
            cxn.commit();//
            
            if (log.isInfoEnabled()) {
                log.info("\n" + cxn.getTripleStore().dumpStore());
            }

            {
                
                final String query = 
                    "PREFIX rdf: <"+RDF.NAMESPACE+"> " +
                    "PREFIX rdfs: <"+RDFS.NAMESPACE+"> " +
                    "PREFIX ns: <"+ns+"> " +
                    
                    "select * " +
                    "WHERE { " +
                    "  ?s rdf:type ns:Person . " +
                    "  ?s ns:likes ?likes . " +
                    "  OPTIONAL { ?s rdfs:label ?label . } " +
                    "}";
                
                final TupleQuery tupleQuery = 
                    cxn.prepareTupleQuery(QueryLanguage.SPARQL, query);
                final TupleQueryResult result = tupleQuery.evaluate();
                
//                while (result.hasNext()) {
//                    System.err.println(result.next());
//                }
 
                final Collection<BindingSet> solution = new LinkedList<BindingSet>();
                solution.add(createBindingSet(new Binding[] {
                    new SimpleBinding("s", mike),
                    new SimpleBinding("likes", rdf),
                    new SimpleBinding("label", l1)
                }));
                solution.add(createBindingSet(new Binding[] {
                    new SimpleBinding("s", bryan),
                    new SimpleBinding("likes", rdf),
//                    new SimpleBinding("label", l2)
                }));
                
                compare(result, solution);
                
            }
        } finally {
            cxn.close();
        }
        } finally {
            sail.__tearDownUnitTest();
        }

    }
    
    public void testOrEquals() throws Exception {

        final BigdataSail sail = getSail();
        try {
        sail.init();
        final BigdataSailRepository repo = new BigdataSailRepository(sail);
        final BigdataSailRepositoryConnection cxn = 
            (BigdataSailRepositoryConnection) repo.getConnection();
        cxn.setAutoCommit(false);
        
        try {
    
//            final ValueFactory vf = sail.getValueFactory();
//    
//            final LexiconRelation lex = sail.getDatabase().getLexiconRelation();
            
            final String ns = BD.NAMESPACE;
            
            final IRI mike = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(ns+"Mike");
            final IRI bryan = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(ns+"Bryan");
            final IRI martyn = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(ns+"Martyn");
            final IRI person = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(ns+"Person");
            final IRI p = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(ns+"p");
            final Literal l1 = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createLiteral("Mike");
            final Literal l2 = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createLiteral("Bryan");
            final Literal l3 = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createLiteral("Martyn");
/**/
            cxn.setNamespace("ns", ns);
            
            cxn.add(mike, RDF.TYPE, person);
            cxn.add(mike, RDFS.LABEL, l1);
            cxn.add(bryan, RDF.TYPE, person);
            cxn.add(bryan, RDFS.COMMENT, l2);
            cxn.add(martyn, RDF.TYPE, person);
            cxn.add(martyn, p, l3);

            /*
             * Note: The either flush() or commit() is required to flush the
             * statement buffers to the database before executing any operations
             * that go around the sail.
             */
            cxn.flush();//commit();
            cxn.commit();//
            
            if (log.isInfoEnabled()) {
                log.info("\n" + cxn.getTripleStore().dumpStore());
            }

            {
                
                String query = 
                    "PREFIX rdf: <"+RDF.NAMESPACE+"> " +
                    "PREFIX rdfs: <"+RDFS.NAMESPACE+"> " +
                    "PREFIX ns: <"+ns+"> " +
                    
                    "select * " +
                    "WHERE { " +
                    "  ?s rdf:type ns:Person . " +
                    "  ?s ?p ?label . " +
                    "  FILTER ( ?p = rdfs:label || ?p = rdfs:comment ) . " +
                    "}";
                
                final TupleQuery tupleQuery = 
                    cxn.prepareTupleQuery(QueryLanguage.SPARQL, query);
                final TupleQueryResult result = tupleQuery.evaluate();
                
//                while (result.hasNext()) {
//                    System.err.println(result.next());
//                }
 
                final Collection<BindingSet> solution = new LinkedList<BindingSet>();
                solution.add(createBindingSet(new Binding[] {
                    new SimpleBinding("s", mike),
                    new SimpleBinding("p", RDFS.LABEL),
                    new SimpleBinding("label", l1)
                }));
                solution.add(createBindingSet(new Binding[] {
                    new SimpleBinding("s", bryan),
                    new SimpleBinding("p", RDFS.COMMENT),
                    new SimpleBinding("label", l2)
                }));
                
                compare(result, solution);
                
            }
        } finally {
            cxn.close();
        }
        } finally {
            sail.__tearDownUnitTest();
        }

    }
    
    public void testConcat() throws Exception {

        final BigdataSail sail = getSail();
        try {
        sail.init();
        final BigdataSailRepository repo = new BigdataSailRepository(sail);
        final BigdataSailRepositoryConnection cxn = 
            (BigdataSailRepositoryConnection) repo.getConnection();
        cxn.setAutoCommit(false);
        
        try {
    
            final String ns = BD.NAMESPACE;            
          
            final IRI foo = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(ns+"foo");
            final IRI bar = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(ns+"bar");
            final IRI plain = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(ns+"plain");
            final IRI language = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(ns+"language");
            final IRI string = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(ns+"string");
            
            final Literal fooPlain = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createLiteral("foo");
            final Literal fooLanguage = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createLiteral("foo", "en");
            final Literal fooString = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createLiteral("foo", XSD.STRING);
            final Literal barPlain = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createLiteral("bar");
            final Literal barLanguage = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createLiteral("bar", "en");
            final Literal barString = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createLiteral("bar", XSD.STRING);
            final Literal foobarPlain = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createLiteral("foobar");
            final Literal foobarLanguage = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createLiteral("foobar", "en");
            final Literal foobarString = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createLiteral("foobar", XSD.STRING);
            
/**/
            cxn.setNamespace("ns", ns);
            
            cxn.add(foo, plain, fooPlain);
            cxn.add(bar, plain, barPlain);
            cxn.add(foo, language, fooLanguage);
            cxn.add(bar, language, barLanguage);
            cxn.add(foo, string, fooString);
            cxn.add(bar, string, barString);
            cxn.add(plain, plain, foobarPlain);
            cxn.add(language, language, foobarLanguage);
            cxn.add(string, string, foobarString);
            cxn.add(plain, string, foobarPlain);
            cxn.add(language, plain, foobarPlain);
            cxn.add(language, string, foobarPlain);

            /*
             * Note: The either flush() or commit() is required to flush the
             * statement buffers to the database before executing any operations
             * that go around the sail.
             */
            cxn.flush();//commit();
            cxn.commit();//
            
            if (log.isInfoEnabled()) {
                log.info("\n" + cxn.getTripleStore().dumpStore());
            }

            {
                 
                String query = 
                    "select ?o1 ?o2 ?o3 " +
                    "WHERE { " +
                    "  ?s1 ?p1 ?o1 . " +
                    "  ?s2 ?p2 ?o2 . " +
                    "  ?p1 ?p2 ?o3 . " +
                    "  FILTER(concat(?o1, ?o2) = ?o3)"+
                    "}";
                
               final TupleQuery tupleQuery = 
                    cxn.prepareTupleQuery(QueryLanguage.SPARQL, query);
                final TupleQueryResult result = tupleQuery.evaluate();
                
                int cnt = 0;
                try {
                	while(result.hasNext()) {
                		cnt++;
                	}
                } finally {
                	result.close();
                }
                
                assertEquals(6, cnt);

            }
        } finally {
            cxn.close();
        }
        } finally {
            sail.__tearDownUnitTest();
        }

    }
    
    /*
     * See TestHashJoins, which actually verifies that a hash join was used
     * and TestASTQueryHintsOptimizer, which verifies correct attachment of
     * query hints.
     */
//    public void testHashJoin() throws Exception {
//
//        final BigdataSail sail = getSail();
//        try {
//        sail.init();
//        final BigdataSailRepository repo = new BigdataSailRepository(sail);
//        final BigdataSailRepositoryConnection cxn = 
//            (BigdataSailRepositoryConnection) repo.getConnection();
//        cxn.setAutoCommit(false);
//        
//        try {
//    
////            final ValueFactory vf = sail.getValueFactory();
////    
////            final LexiconRelation lex = sail.getDatabase().getLexiconRelation();
//            
//            final String ns = BD.NAMESPACE;
//            
//            final IRI mikeA = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(ns+"MikeA");
//            final IRI mikeB = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(ns+"MikeB");
//            final IRI bryan = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(ns+"Bryan");
//            final IRI martyn = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(ns+"Martyn");
//            final IRI person = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(ns+"Person");
//            final IRI name = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(ns+"name");
//            final Literal l1 = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createLiteral("Mike");
//            final Literal l2 = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createLiteral("Bryan");
//            final Literal l3 = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createLiteral("Martyn");
///**/
//            cxn.setNamespace("ns", ns);
//            
//            cxn.add(mikeA, RDF.TYPE, person);
//            cxn.add(mikeA, name, l1);
//            cxn.add(mikeB, RDF.TYPE, person);
//            cxn.add(mikeB, name, l1);
//            cxn.add(bryan, RDF.TYPE, person);
//            cxn.add(bryan, name, l2);
//            cxn.add(martyn, RDF.TYPE, person);
//            cxn.add(martyn, name, l3);
//
//            /*
//             * Note: The either flush() or commit() is required to flush the
//             * statement buffers to the database before executing any operations
//             * that go around the sail.
//             */
//            cxn.flush();//commit();
//            cxn.commit();//
//            
//            if (log.isInfoEnabled()) {
//                log.info("\n" + sail.getDatabase().dumpStore());
//            }
//
//            {
//                
////                String query = 
////                    "PREFIX "+QueryHints.PREFIX+": <"+QueryHints.NAMESPACE+QueryHints.HASH_JOIN+"=true> " +
////                    "PREFIX rdf: <"+RDF.NAMESPACE+"> " +
////                    "PREFIX rdfs: <"+RDFS.NAMESPACE+"> " +
////                    "PREFIX bds: <"+BD.SEARCH_NAMESPACE+"> " +
////                    "PREFIX ns: <"+ns+"> " +
////                    
////                    "select distinct ?s1 ?s2 " +
//////                    "select distinct ?s1 " +
////                    "WHERE { " +
////                    "  ?o1 bds:search \"m*\" ." +
////                    "  ?s1 ns:name ?o1 . " +
////                    "  ?s1 rdf:type ns:Person . " +
////                    "  ?s1 ns:name ?name . " +
////                    "  OPTIONAL { " +
////                    "    ?o2 bds:search \"m*\" ." +
////                    "    ?s2 ns:name ?o2 . " +
////                    "    ?s2 rdf:type ns:Person . " +
////                    "    ?s2 ns:name ?name . " +
////                    "    filter(?s1 != ?s2) . " +
////                    "  } " +
//////                    "  filter(!bound(?s2) || ?s1 != ?s2) . " +
////                    "}";
//                
//                final String query = 
//                    "PREFIX "+QueryHints.PREFIX+": <"+QueryHints.NAMESPACE+AST2BOpBase.Annotations.HASH_JOIN+"=true> " +
//                    "PREFIX rdf: <"+RDF.NAMESPACE+"> " +
//                    "PREFIX rdfs: <"+RDFS.NAMESPACE+"> " +
//                    "PREFIX bds: <"+BD.SEARCH_NAMESPACE+"> " +
//                    "PREFIX ns: <"+ns+"> " +
//                    
//                    "select distinct ?s1 ?s2 " +
//                    "WHERE { " +
//                    "  ?s1 rdf:type ns:Person . " +
//                    "  ?s1 ns:name ?name . " +
//                    "  OPTIONAL { " +
//                    "    ?s2 rdf:type ns:Person . " +
//                    "    ?s2 ns:name ?name . " +
//                    "    filter(?s1 != ?s2) . " +
//                    "  } " +
//                    "}";
//
//                
//                final TupleQuery tupleQuery = 
//                    cxn.prepareTupleQuery(QueryLanguage.SPARQL, query);
//                final TupleQueryResult result = tupleQuery.evaluate();
//                
//                while (result.hasNext()) {
//                    final BindingSet tmp = result.next();
//                    if (log.isInfoEnabled())
//                        log.info(tmp.toString());
//                }
// 
////                Collection<BindingSet> solution = new LinkedList<BindingSet>();
////                solution.add(createBindingSet(new Binding[] {
////                    new SimpleBinding("s", mike),
////                    new SimpleBinding("p", RDFS.LABEL),
////                    new SimpleBinding("label", l1)
////                }));
////                solution.add(createBindingSet(new Binding[] {
////                    new SimpleBinding("s", bryan),
////                    new SimpleBinding("p", RDFS.COMMENT),
////                    new SimpleBinding("label", l2)
////                }));
////                
////                compare(result, solution);
//                
//            }
//        } finally {
//            cxn.close();
//        }
//        } finally {
//            sail.__tearDownUnitTest();
//        }
//
//    }
    
}
