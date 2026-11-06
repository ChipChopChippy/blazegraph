package com.bigdata.rdf.vocab.decls;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleIRI;

import com.bigdata.rdf.vocab.VocabularyDecl;

public class OPVocabularyDecl implements VocabularyDecl {

    static private final IRI[] uris = new IRI[]{//
    	// two named graphs
        org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI("file:///home/OPS/develop/openphacts/datasets/chem2bio2rdf/chembl.nt"),//
        org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI("http://linkedlifedata.com/resource/drugbank"),//
    };

    public OPVocabularyDecl() {
    }
    
    public Iterator<IRI> values() {

        return Collections.unmodifiableList(Arrays.asList(uris)).iterator();
        
    }

}
