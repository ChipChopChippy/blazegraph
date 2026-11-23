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
 * Created on Jul 23, 2012
 */

package com.bigdata.rdf.vocab.decls;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleIRI;

import com.bigdata.rdf.vocab.VocabularyDecl;

/**
 * Vocabulary and namespace for VOID.
 * 
 * @see <a href="http://www.w3.org/TR/void/"> Describing Linked Datasets with
 *      the VoiD Vocabulary </a>
 * @see <a href="http://vocab.deri.ie/void/"> Vocabulary of Interlinked Datasets
 *      (VoID) </a>
 * 
 * @author <a href="mailto:thompsonbry@users.sourceforge.net">Bryan Thompson</a>
 * @version $Id: RDFVocabularyDecl.java 4631 2011-06-06 15:06:48Z thompsonbry $
 */
public class VoidVocabularyDecl implements VocabularyDecl {

    public static final String NAMESPACE = "http://rdfs.org/ns/void#";

    // Classes.
    public static final IRI //
            Dataset = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "Dataset"),//
            DatasetDescription = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "DatasetDescription"),//
            Linkset = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "Linkset"),//
            TechnicalFeature = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "TechnicalFeature")//
            ;

    // Properties.
    public static final IRI //
            class_ = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE+"class"),//
            classPartition = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE+"classPartition"),//
            classes = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE+"classes"),//
            dataDump = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE+"dataDump"),//
            distinctObjects = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE+"distinctObjects"),//
            distinctSubjects = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE+"distinctSubjects"),//
            documents = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE+"documents"),//
            entities = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE+"entities"),//
            exampleResource = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE+"exampleResource"),//
            feature = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE+"feature"),//
            inDataset = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE+"inDataset"),//
            linkPredicate = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE+"linkPredicate"),//
            objectsTarget = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE+"objectsTarget"),//
            openSearchDescription = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE+"openSearchDescription"),//
            properties = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE+"properties"),//
            property = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE+"property"),//
            propertyPartition = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE+"propertyPartition"),//
            rootResource = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE+"rootResource"),//
            sparqlEndpoint = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE+"sparqlEndpoint"),//
            subjectsTarget = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE+"subjectsTarget"),//
            subset = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE+"subset"),//
            target = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE+"target"),//
            triples = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE+"triples"),//
            uriLookupEndpoint = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE+"uriLookupEndpoint"),//
            uriRegexPattern = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE+"uriRegexPattern"),//
            uriSpace = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE+"uriSpace"),//
            vocabulary = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE+"vocabulary")//
    ;

    static private final IRI[] uris = new IRI[]{
            org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE),
            // classes
            Dataset,
            DatasetDescription,
            Linkset,
            TechnicalFeature,
            // properties
            class_, classPartition, classes, dataDump, distinctObjects,
            distinctSubjects, documents, entities, exampleResource, feature,
            inDataset, linkPredicate, objectsTarget, openSearchDescription,
            properties, property, propertyPartition, rootResource,
            sparqlEndpoint, subjectsTarget, subset, target, triples,
            uriLookupEndpoint, uriRegexPattern, uriSpace, vocabulary//
    };

    public VoidVocabularyDecl() {
    }
    
    public Iterator<IRI> values() {

        return Collections.unmodifiableList(Arrays.asList(uris)).iterator();
        
    }

}
