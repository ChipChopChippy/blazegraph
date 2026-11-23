package com.bigdata.rdf.vocab;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleIRI;

import com.bigdata.rdf.store.AbstractTripleStore;
import com.bigdata.rdf.vocab.core.BigdataCoreVocabulary_v20151210;

public class TestMultiVocabulary extends BigdataCoreVocabulary_v20151210 {

	/**
	 * De-serialization ctor.
	 */
	public TestMultiVocabulary() {

		super();

	}

	/**
	 * Used by {@link AbstractTripleStore#create()}.
	 * 
	 * @param namespace
	 *            The namespace of the KB instance.
	 */
	public TestMultiVocabulary(final String namespace) {

		super(namespace);

	}

	@Override
	protected void addValues() {

		addDecl(new TestVocabularyDecl());

		super.addValues();

	}
	
	class TestVocabularyDecl implements VocabularyDecl {

		private final IRI[] uris = new IRI[] { org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(
				"http://blazegraph.com/Data#Position_") };

		public TestVocabularyDecl() {
		}

		public Iterator<IRI> values() {
			return Collections.unmodifiableList(Arrays.asList(uris)).iterator();
		}
	}

}
