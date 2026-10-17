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

package com.bigdata.rdf.sail.webapp;

import java.util.Arrays;

import junit.framework.Test;

import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.SimpleIRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.impl.SimpleDataset;

import com.bigdata.journal.IIndexManager;
import com.bigdata.rdf.sail.remote.BigdataSailRemoteRepository;
import com.bigdata.rdf.sail.remote.BigdataSailRemoteRepositoryConnection;
import com.bigdata.rdf.sail.webapp.client.RemoteRepository.AddOp;

/**
 * Test suite for remote support of Sesame API Query methods (setBinding, setDataset, setIncludeInferred) and support of contexts in getStatements
 * 
 *  @author igorkim
 *  
 * @see <a href="http://jira.blazegraph.com/browse/BLZG-789" > BigdataSailRemoteRepositoryConnection should implement interface methods </a>
 * @see <a href="http://jira.blazegraph.com/browse/BLZG-753"> Result of BigdataSailRemoteRepositoryConnection.getStatements() and export() do not preserve statement context.
 * @see <a href="http://jira.blazegraph.com/browse/BLZG-1325"> BigdataSailRemoteRepositoryConnection must support setBinding(), getBindings(), clearBindings(), and removeBinding()
 * @see <a href="http://jira.blazegraph.com/browse/BLZG-1326"> BigdataSailRemoteRepositoryConnection must support setDataset(), getDataset()
 */
public class Test_Ticket_789<S extends IIndexManager> extends
		AbstractTestNanoSparqlClient<S> {

	private final Resource s = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI("http://test/s");
	private final IRI p = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI("http://test/p");
	private final IRI p1 = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI("http://test/p1");
	private final Value o = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI("http://test/o");
	private final Resource c = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI("http://test/c");
	private final IRI defaultGraph1 = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI("http://test/defaultGraph1");
	private final IRI defaultGraph2 = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI("http://test/defaultGraph2");
	private final IRI namedGraph1 = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI("http://test/namedGraph1");
	private final IRI namedGraph2 = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI("http://test/namedGraph2");

	public Test_Ticket_789() {

		super();

	}

	public Test_Ticket_789(final String name) {

		super(name);

	}

	public static Test suite() {

		return ProxySuiteHelper.suiteWhenStandalone(Test_Ticket_789.class,
				"test.*", TestMode.quads);

	}

	private void populate() throws Exception {
		final SimpleValueFactory vf = SimpleValueFactory.getInstance();
		final Statement[] a = new Statement[] {
				vf.createStatement(s, p, o, c),
				vf.createStatement(s, p, o, namedGraph1),
				vf.createStatement(s, p1, o, namedGraph2),
				vf.createStatement(s, p, o, defaultGraph1),
				vf.createStatement(s, p, p, defaultGraph2),
				vf.createStatement(p, p, p, c)
		};

		final AddOp addOp = new AddOp(Arrays.asList(a));

		m_repo.add(addOp);
	}

	/**
	 * Test supposed to check if remote call for getStatements properly handle
	 * contexts
	 */
	public void test_ticket_753_getStatements() throws Exception {

		populate();

		final GraphQueryResult resultIncludeInferred = m_repo.getStatements(s, p, o, true, c);
		try {
			int count = 0;
			while (resultIncludeInferred.hasNext()) {
				resultIncludeInferred.next();
				count++;
			}
			// check that only one of the (s,p,o) triple returned for the specified context c,
			// as the other one belongs to different context namedGraph1
			assertEquals(1, count);
		} finally {
			resultIncludeInferred.close();
		}

	}

	public void test_ticket_1325_bindings() throws Exception {
		populate();
		
		BigdataSailRemoteRepository sailRepo = m_repo.getBigdataSailRemoteRepository();
		BigdataSailRemoteRepositoryConnection con = sailRepo.getConnection();
		
		final TupleQuery tq = con.prepareTupleQuery(QueryLanguage.SPARQL, "select * where {?s ?p ?o}");
		tq.setBinding("s", s);
		tq.setBinding("o", o);
		{
			TupleQueryResult tqr = tq.evaluate();
			try {
				int count = 0;
				while (tqr.hasNext()) {
					BindingSet bs = tqr.next();
					assertEquals(s, bs.getBinding("s").getValue());
					assertEquals(o, bs.getBinding("o").getValue());
					count++;
				}
				// there are 2 statements with specified subject and object: (s, p, o) and (s, p1, o) 
				assertEquals(2, count);
			} finally {
				tqr.close();
			}
			tq.clearBindings();
			assertEquals(0, tq.getBindings().size());
		}
	}

	public void test_ticket_1326_dataset() throws Exception {
		populate();
		
		BigdataSailRemoteRepository sailRepo = m_repo.getBigdataSailRemoteRepository();
		BigdataSailRemoteRepositoryConnection con = sailRepo.getConnection();
		final TupleQuery tq = con.prepareTupleQuery(QueryLanguage.SPARQL, "select * where {?s ?p ?o}");

		final SimpleDataset dataset = new SimpleDataset();
		dataset.addDefaultGraph(defaultGraph1);

		tq.setDataset(dataset);
		{
			TupleQueryResult tqr = tq.evaluate();
			try {
				int count = 0;
				while (tqr.hasNext()) {
					tqr.next();
					count++;
				}
				// there is only 1 statement in defaultGraph1 
				assertEquals(1, count);
			} finally {
				tqr.close();
			}
		}
	}

}
