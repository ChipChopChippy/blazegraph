package com.bigdata.rdf.vocab;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleIRI;

public class TestVocabularyDecl_BLZG1591 implements VocabularyDecl {

    static private final IRI[] uris = new IRI[]{//
    	// two named graphs
        org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI("http://test.com/P")
    };

    public TestVocabularyDecl_BLZG1591() {
    }
    
    public Iterator<IRI> values() {

        return Collections.unmodifiableList(Arrays.asList(uris)).iterator();
        
    }

}
