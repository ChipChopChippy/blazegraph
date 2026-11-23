package com.bigdata.samples.remoting;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleIRI;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import com.bigdata.rdf.store.BD;
import com.bigdata.samples.SparqlBuilder;

public class DemoSesameServer {
    
    private static final String sesameURL = "http://localhost:9999/openrdf-sesame";
    
    private static final String repoID = "bigdata";
    
    /**
     * @param args
     */
    public static void main(String[] args) {
        
        try {
            _main(args);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        
    }
    
    public static void _main(String[] args) throws Exception {

        String sesameURL, repoID;
        
        if (args != null && args.length == 2) {
            sesameURL = args[0];
            repoID = args[1];
        } else {
            sesameURL = DemoSesameServer.sesameURL;
            repoID = DemoSesameServer.repoID;
        }
        
        Repository repo = new HTTPRepository(sesameURL, repoID);
        repo.initialize();
        
        RepositoryConnection cxn = repo.getConnection();
        cxn.setAutoCommit(false);
        
        try { // load some statements built up programmatically
            
            IRI mike = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(BD.NAMESPACE + "Mike");
            IRI bryan = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(BD.NAMESPACE + "Bryan");
            IRI loves = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(BD.NAMESPACE + "loves");
            IRI rdf = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(BD.NAMESPACE + "RDF");
            Model graph = new LinkedHashModel();
            graph.add(mike, loves, rdf);
            graph.add(bryan, loves, rdf);
            
            cxn.add(graph);
            cxn.commit();
            
        } finally {
            
            cxn.close();
            
        }
        
        { // show the entire contents of the repository

            SparqlBuilder sparql = new SparqlBuilder();
            sparql.addTriplePattern("?s", "?p", "?o");
            
            GraphQuery query = cxn.prepareGraphQuery(
                    QueryLanguage.SPARQL, sparql.toString());
            GraphQueryResult result = query.evaluate();
            while (result.hasNext()) {
                Statement stmt = result.next();
                System.err.println(stmt);
            }
            
        }
        
        
    }
}
