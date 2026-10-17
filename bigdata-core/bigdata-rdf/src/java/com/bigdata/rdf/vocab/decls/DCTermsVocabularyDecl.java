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
 * Created on Jun 6, 2011
 */

package com.bigdata.rdf.vocab.decls;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleIRI;

/**
 * Dublin Core terms.
 * 
 * @see http://purl.org/dc/terms/
 * 
 * @author <a href="mailto:thompsonbry@users.sourceforge.net">Bryan Thompson</a>
 * @version $Id$
 */
public class DCTermsVocabularyDecl {

    public static final String NAMESPACE = "http://purl.org/dc/terms/";
    public static final IRI description = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "description");
    public static final IRI hasVersion = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "hasVersion");
    public static final IRI issued = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "issued");
    public static final IRI modified = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "modified");
    public static final IRI publisher = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "publisher");
    public static final IRI reviewer = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "reviewer");
    public static final IRI Review = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "Review");
    public static final IRI text = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "text");
    public static final IRI title = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "title");

    static private final IRI[] uris = new IRI[]{
            org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE),//
            description, hasVersion, issued, modified, publisher, reviewer,
            Review, text, title, };

    public DCTermsVocabularyDecl() {
    }

    public Iterator<IRI> values() {

        return Collections.unmodifiableList(Arrays.asList(uris)).iterator();

    }

}
