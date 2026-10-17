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
 * Created on Jun 4, 2011
 */

package com.bigdata.rdf.vocab.decls;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleIRI;

import com.bigdata.rdf.vocab.VocabularyDecl;

/**
 * Vocabulary and namespace for SKOS.
 * 
 * @see http://www.w3.org/2004/02/skos/core#
 * @see http://www.w3.org/TR/skos-reference/skos.html
 * 
 * @author <a href="mailto:thompsonbry@users.sourceforge.net">Bryan Thompson</a>
 * @version $Id$
 */
public class SKOSVocabularyDecl implements VocabularyDecl {

    public static final String NAMESPACE = "http://www.w3.org/2004/02/skos/core#";
    public static final IRI Collection = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE+"Collection");
    public static final IRI Concept = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE+"Concept");
    public static final IRI ConceptScheme = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE+"ConceptScheme");
    public static final IRI OrderedCollection = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE+"OrderedCollection");
    public static final IRI altLabel = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE+"altLabel");
    public static final IRI broadMatch = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE+"broadMatch");
    public static final IRI broader = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE+"broader");
    public static final IRI broaderTransitive = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE+"broaderTransitive");
    public static final IRI changeNote = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE+"changeNote");
    public static final IRI closeMatch = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE+"closeMatch");
    public static final IRI definition = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE+"definition");
    public static final IRI editorialNote = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE+"editorialNote");
    public static final IRI exactMatch = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE+"exactMatch");
    public static final IRI example = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE+"example");
    public static final IRI hasTopConcept = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE+"hasTopConcept");
    public static final IRI hiddenLabel = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE+"hiddenLabel");
    public static final IRI historyNote = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE+"historyNote");
    public static final IRI inScheme = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE+"inScheme");
    public static final IRI mappingRelation = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE+"mappingRelation");
    public static final IRI member = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE+"member");
    public static final IRI memberList = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE+"memberList");
    public static final IRI narrowMatch = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE+"narrowMatch");
    public static final IRI narrow = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE+"narrow");
    public static final IRI narrowTransitive = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE+"narrowTransitive");
    public static final IRI notation = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE+"notation");
    public static final IRI note = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE+"note");
    public static final IRI prefLabel = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE+"prefLabel");
    public static final IRI related = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE+"related");
    public static final IRI relatedMatch = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE+"relatedMatch");
    public static final IRI scopeNote = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE+"scopeNote");
    public static final IRI semanticRelation= org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE+"semanticRelation");
    public static final IRI topConceptOf = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE+"topConceptOf");
    
    static private final IRI[] uris = new IRI[] {
        org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE),//
        Collection, Concept, ConceptScheme, OrderedCollection, altLabel,
        broadMatch, broader, broaderTransitive, changeNote, closeMatch,
        definition, editorialNote, exactMatch, example, hasTopConcept,
        hiddenLabel, historyNote, inScheme, mappingRelation, member,
        memberList, narrowMatch, narrow, narrowTransitive, notation, note,
        prefLabel, related, relatedMatch, scopeNote, semanticRelation,
        topConceptOf//
    };

    public SKOSVocabularyDecl() {
    }
    
    public Iterator<IRI> values() {

        return Collections.unmodifiableList(Arrays.asList(uris)).iterator();
        
    }

}
