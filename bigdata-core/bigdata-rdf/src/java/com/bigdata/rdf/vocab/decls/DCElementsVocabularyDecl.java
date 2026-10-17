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
 * Vocabulary and namespace for the Dublin Core "elements".
 * 
 * @see http://purl.org/dc/elements/1.1/
 * 
 * @author <a href="mailto:thompsonbry@users.sourceforge.net">Bryan Thompson</a>
 * @version $Id$
 */
public class DCElementsVocabularyDecl implements VocabularyDecl {

    public static final String NAMESPACE = "http://purl.org/dc/elements/1.1/";
    public static final IRI title = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "title");
    public static final IRI creator = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "creator");
    public static final IRI subject = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "subject");
    public static final IRI description = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "description");
    public static final IRI publisher = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "publisher");
    public static final IRI contributor = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "contributor");
    public static final IRI date = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "date");
    public static final IRI type = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "type");
    public static final IRI format = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "format");
    public static final IRI identifier = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "identifier");
    public static final IRI source = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "source");
    public static final IRI language = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "language");
    public static final IRI relation = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "relation");
    public static final IRI coverage = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "coverage");
    public static final IRI rights = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "rights");
        
    static private final IRI[] uris = new IRI[]{
            org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE),//
            title, creator, subject, description, publisher, contributor, date,
            type, format, identifier, source, language, relation, coverage,
            rights
    };

    public DCElementsVocabularyDecl() {
    }
    
    public Iterator<IRI> values() {

        return Collections.unmodifiableList(Arrays.asList(uris)).iterator();
        
    }

}
