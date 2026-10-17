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
 * Vocabulary and namespace for LUBM using the default namespace.
 * 
 * @see http://swat.cse.lehigh.edu/projects/lubm/ 
 * 
 * @author <a href="mailto:thompsonbry@users.sourceforge.net">Bryan Thompson</a>
 * @version $Id$
 */
public class LUBMVocabularyDecl implements VocabularyDecl {

    private static final String NAMESPACE = "http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#";
    
    static private final IRI[] uris = new IRI[]{
        // namespace
        org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE),//
        // classes
        org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "AdministrativeStaff"),//
        org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "Article"),//
        org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "AssistantProfessor"),//
        org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "AssociateProfessor"),//
        org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "Book"),//
        org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "Chair"),//
        org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "ClericalStaff"),//
        org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "College"),//
        org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "ConferencePaper"),//
        org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "Course"),//
        org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "Dean"),//
        org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "Department"),//
        org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "Director"),//
        org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "Employee"),//
        org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "Faculty"),//
        org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "FullProfessor"),//
        org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "GraduateCourse"),//
        org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "GraduateStudent"),//
        org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "Institute"),//
        org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "JournalArticle"),//
        org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "Lecturer"),//
        org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "Manual"),//
        org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "Organization"),//
        org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "Person"),//
        org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "PostDoc"),//
        org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "Professor"),//
        org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "Program"),//
        org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "Publication"),//
        org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "Research"),//
        org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "ResearchAssistant"),//
        org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "ResearchGroup"),//
        org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "Schedule"),//
        org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "Software"),//
        org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "Specification"),//
        org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "Student"),//
        org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "SystemsStaff"),//
        org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "TeachingAssistant"),//
        org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "TechnicalReport"),//
        org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "UndergraduateStudent"),//
        org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "University"),//
        org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "UnofficialPublication"),//
        org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "VisitingProfessor"),//
        org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "Work"),//
        // predicates
        org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "advisor"),//
        org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "affiliatedOrganizationOf"),//
        org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "affiliateOf"),//
        org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "age"),//
        org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "degreeFrom"),//
        org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "doctoralDegreeFrom"),//
        org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "emailAddress"),//
        org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "hasAlumnus"),//
        org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "headOf"),//
        org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "listedCourse"),//
        org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "mastersDegreeFrom"),//
        org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "member"),//
        org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "memberOf"),//
        org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "name"),//
        org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "officeNumber"),//
        org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "orgPublication"),//
        org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "publicationAuthor"),//
        org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "publicationDate"),//
        org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "publicationResearch"),//
        org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "researchInterest"),//
        org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "researchProject"),//
        org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "softwareDocumentation"),//
        org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "softwareVersion"),//
        org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "subOrganizationOf"),//
        org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "takesCourse"),//
        org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "teacherOf"),//
        org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "teachingAssistantOf"),//
        org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "telephone"),//
        org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "tenured"),//
        org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "title"),//
        org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "undergraduateDegreeFrom"),//
        org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "worksFor"),//
    };

    public LUBMVocabularyDecl() {
    }
    
    public Iterator<IRI> values() {

        return Collections.unmodifiableList(Arrays.asList(uris)).iterator();
        
    }

}
