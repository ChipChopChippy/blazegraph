package com.bigdata.samples;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleIRI;

public class LUBM {
    
    final static String NS =
        "http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#";

    final static IRI FULL_PROFESSOR = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NS + 
        "FullProfessor"
        );
    
    final static IRI PROFESSOR = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NS + 
            "Professor"
            );
        
    final static IRI WORKS_FOR = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NS + 
            "worksFor"
            );
        
    final static IRI NAME = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NS + 
            "name"
            );
        
    final static IRI EMAIL_ADDRESS = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NS + 
            "emailAddress"
            );
        
    final static IRI TELEPHONE = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NS + 
            "telephone"
            );
        
}
