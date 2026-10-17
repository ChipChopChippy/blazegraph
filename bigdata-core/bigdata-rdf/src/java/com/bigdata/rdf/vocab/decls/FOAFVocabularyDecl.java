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
 * Vocabulary and namespace for FOAF, not including terms marked as "archaic".
 * 
 * @see http://xmlns.com/foaf/spec/
 * 
 * @author <a href="mailto:thompsonbry@users.sourceforge.net">Bryan Thompson</a>
 * @version $Id$
 */
public class FOAFVocabularyDecl implements VocabularyDecl {

    /*
     * Namespace
     */
    public static final String NAMESPACE = "http://xmlns.com/foaf/0.1/";
    /*
     * Classes
     */
    public static final IRI Agent = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "Agent"); 
    public static final IRI Document = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "Document"); 
    public static final IRI Group= org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "Group"); 
    public static final IRI Image = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "Image"); 
    public static final IRI LabelProperty = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "LabelProperty"); 
    public static final IRI OnlineAccount = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "OnlineAccount"); 
    public static final IRI OnlineChatAccount = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "OnlineChatAccount"); 
    public static final IRI OnlineEcommerceAccount = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "OnlineEcommerceAccount"); 
    public static final IRI OnlineGamingAccount = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "OnlineGamingAccount"); 
    public static final IRI Organization = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "Organization"); 
    public static final IRI Person = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "Person");
    public static final IRI PersonalProfileDocument = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "PersonalProfileDocument"); 
    public static final IRI Project = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "Project");
    /*
     * Properties.
     */
    public static final IRI account = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "account");
    public static final IRI accountName = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "accountName");
    public static final IRI accountServiceHomepage = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "accountServiceHomepage");
    public static final IRI age = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "age");
    public static final IRI aimChatID = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "aimChatID");
    public static final IRI based_near = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "based_near");
    public static final IRI birthday = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "birthday");
    public static final IRI currentProject = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "currentProject");
    public static final IRI depiction = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "depiction");
    public static final IRI depicts = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "depicts");
    public static final IRI familyName = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "familyName");
    public static final IRI firstName = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "firstName");
    public static final IRI gender = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "gender");
    public static final IRI givenName = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "givenName");
    public static final IRI homepage = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "homepage");
    public static final IRI icqChatID = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "icqChatID");
    public static final IRI img = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "img");
    public static final IRI interest = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "interest");
    public static final IRI isPrimaryTopicOf = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "isPrimaryTopicOf");
    public static final IRI jabberID = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "jabberID");
    public static final IRI knows = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "knows");
    public static final IRI lastName = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "lastName");
    public static final IRI logo = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "logo");
    public static final IRI made = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "made");
    public static final IRI maker = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "maker");
    public static final IRI mbox = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "mbox");
    public static final IRI mbox_sha1sum = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "mbox_sha1sum");
    public static final IRI member = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "member");
    public static final IRI membershipClass = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "membershipClass");
    public static final IRI msnChatID = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "msnChatID");
    public static final IRI myersBriggs = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "myersBriggs");
    public static final IRI name = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "name");
    public static final IRI nick = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "nick");
    public static final IRI openid = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "openid");
    public static final IRI page = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "page");
    public static final IRI pastProject = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "pastProject");
    public static final IRI phone = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "phone");
    public static final IRI plan = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "plan");
    public static final IRI primaryTopic = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "primaryTopic");
    public static final IRI publications = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "publications");
    public static final IRI schoolHomepage = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "schoolHomepage");
    public static final IRI sha1 = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "sha1");
    public static final IRI skypeID = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "skypeID");
    public static final IRI status = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "status");
    public static final IRI thumbnail = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "thumbnail");
    public static final IRI tipjar = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "tipjar");
    public static final IRI title = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "title");
    public static final IRI topic = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "topic");
    public static final IRI topic_interest = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "topic_interest");
    public static final IRI weblog = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "weblog");
    public static final IRI workInfoHomepage = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "workInfoHomepage");
    public static final IRI workplaceHomepage = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "workplaceHomepage");
    public static final IRI yahooChatID = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "yahooChatID");
    // archaic
//  org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "dnaChecksum"),//
//  org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "family_name"),//
//  org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "fundedBy"),//
//  org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "geekcode"),//
//  org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "givenname"),//
//  org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "holdsAccount"),//
//  org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE + "theme"),//

    /*
     * Note: While FOAF is pretty dynamic, DO NOT add new terms to this
     * vocabulary as that can introduce a vocabulary inconsistency (which will
     * be reported as an error for existing KBs). Instead, the NAMESPACE will be
     * used to provide a reasonably tight representation for any new terms added
     * to this vocabulary over time.
     */
    static private final IRI[] uris = new IRI[] {//
        org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(NAMESPACE),//
            // Classes
            Agent, Document, Group, Image, LabelProperty, OnlineAccount,
            OnlineChatAccount, OnlineEcommerceAccount, OnlineGamingAccount,
            Organization, Person, PersonalProfileDocument, Project,
            // Properties
            account, accountName, accountServiceHomepage, age, aimChatID,
            based_near, birthday, currentProject, depiction, depicts,
            familyName, firstName, gender, givenName, homepage, icqChatID, img,
            interest, isPrimaryTopicOf, jabberID, knows, lastName, logo, made,
            maker, mbox, mbox_sha1sum, member, membershipClass, msnChatID,
            myersBriggs, name, nick, openid, page, pastProject, phone, plan,
            primaryTopic, publications, schoolHomepage, sha1, skypeID, status,
            thumbnail, tipjar, title, topic, topic_interest, weblog,
            workInfoHomepage, workplaceHomepage, yahooChatID,
    };

    public FOAFVocabularyDecl() {
    }

    public Iterator<IRI> values() {

        return Collections.unmodifiableList(Arrays.asList(uris)).iterator();

    }

}
