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
package com.bigdata.rdf.sparql.ast.eval;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;

import com.bigdata.bop.IVariable;
import com.bigdata.rdf.internal.IV;
import com.bigdata.rdf.sparql.ast.GraphPatternGroup;
import com.bigdata.rdf.sparql.ast.IGroupMemberNode;
import com.bigdata.rdf.sparql.ast.StatementPatternNode;
import com.bigdata.rdf.sparql.ast.TermNode;
import com.bigdata.rdf.sparql.ast.service.ServiceCallCreateParams;
import com.bigdata.rdf.sparql.ast.service.ServiceNode;
import com.bigdata.rdf.store.AbstractTripleStore;
import com.bigdata.rdf.store.BD;

/**
 * Helper class for parsing an extracting SERVICE parameters. The SERVICE group
 * graph pattern should consist of zero or triples whose Subject is
 * {@link BD#SERVICE_PARAM}. There may be zero or more such triple patterns. The
 * Predicate (key) and Object (val) positions for those triple patterns are
 * extracted into a {@link ServiceParams} object. For each key, there may be one
 * or more values.
 * 
 * <pre>
 * SERVICE <uri> {
 *   bd:serviceParam :key1 :val1 .
 *   bd:serviceParam :key1 :val2 .
 *   bd:serviceParam :key2 :val3 .
 * }
 * </pre>
 */
public class ServiceParams {

    private static final Logger log = LogManager
            .getLogger(AbstractServiceFactory.class);

    /**
     * The map of service params.
     */
    private final Map<IRI, List<TermNode>> params;

    public ServiceParams() {

        this.params = new LinkedHashMap<IRI, List<TermNode>>();

    }

    /**
     * Add.
     */
    public void add(final IRI param, final TermNode value) {

        if (!params.containsKey(param)) {

            params.put(param, new LinkedList<TermNode>());

        }

        params.get(param).add(value);

    }

    /**
     * Set (clear and add).
     */
    public void set(final IRI param, final TermNode value) {

        clear(param);

        add(param, value);

    }

    /**
     * Clear.
     */
    public void clear(final IRI param) {

        params.remove(param);

    }

    /**
     * Check for existence.
     */
    public boolean contains(final IRI param) {

        return params.containsKey(param);

    }

    /**
     * Get a singleton value for the specified param.
     */
    public TermNode get(final IRI param, final TermNode defaultValue) {

        if (params.containsKey(param)) {

            final List<TermNode> values = params.get(param);

            if (values.size() > 1) {

                throw new RuntimeException("not a singleton param");

            }

            return values.get(0);

        }

        return defaultValue;

    }

    /**
     * Helper.
     */
    public Boolean getAsBoolean(final IRI param) {

        return getAsBoolean(param, null);

    }

    /**
     * Helper.
     */
    public Boolean getAsBoolean(final IRI param, final Boolean defaultValue) {

        final Literal term = getAsLiteral(param, null);

        if (term != null) {

            return term.booleanValue();

        }

        return defaultValue;

    }

    /**
     * Helper.
     */
    public Integer getAsInt(final IRI param) {

        return getAsInt(param, null);

    }

    /**
     * Helper.
     */
    public Integer getAsInt(final IRI param, final Integer defaultValue) {

        final Literal term = getAsLiteral(param, null);

        if (term != null) {

            return term.intValue();

        }

        return defaultValue;

    }

    /**
     * Helper.
     */
    public Long getAsLong(final IRI param) {

        return getAsLong(param, null);

    }

    /**
     * Helper.
     */
    public Long getAsLong(final IRI param, final Long defaultValue) {

        final Literal term = getAsLiteral(param, null);

        if (term != null) {

            return term.longValue();

        }

        return defaultValue;

    }

    /**
     * Helper.
     */
    public String getAsString(final IRI param) {

        return getAsString(param, null);

    }

    /**
     * Helper.
     */
    public String getAsString(final IRI param, final String defaultValue) {

        final Literal term = getAsLiteral(param, null);

        if (term != null) {

            return term.stringValue();

        }

        return defaultValue;

    }

    /**
     * Helper.
     */
    public Literal getAsLiteral(final IRI param) {

        return getAsLiteral(param, null);

    }

    /**
     * Helper.
     */
    public Literal getAsLiteral(final IRI param, final Literal defaultValue) {

        final TermNode term = get(param, null);

        if (term != null) {

            if (term.isVariable()) {

                throw new IllegalArgumentException("not a constant");

            }

            final Value v = term.getValue();

            if (!(v instanceof Literal)) {

                throw new IllegalArgumentException("not a literal");

            }

            return ((Literal) v);

        }

        return defaultValue;

    }

    /**
     * Helper.
     */
    public IRI getAsURI(final IRI param) {

        return getAsURI(param, null);

    }

    /**
     * Helper.
     */
    public IRI getAsURI(final IRI param, final IRI defaultValue) {

        final TermNode term = get(param, null);

        if (term != null) {

            if (term.isVariable()) {

                throw new IllegalArgumentException("not a constant");

            }

            final Value v = term.getValue();

            if (!(v instanceof IRI)) {

                throw new IllegalArgumentException("not a uri");

            }

            return ((IRI) v);

        }

        return defaultValue;

    }

    /**
     * Helper.
     */
    @SuppressWarnings("rawtypes")
    public IVariable<IV> getAsVar(final IRI param) {

        return getAsVar(param, null);

    }

    /**
     * Helper.
     */
    @SuppressWarnings("rawtypes")
    public IVariable<IV> getAsVar(final IRI param,
            final IVariable<IV> defaultValue) {

        final TermNode term = get(param, null);

        if (term != null) {

            if (!term.isVariable()) {

                throw new IllegalArgumentException("not a var");

            }

            return (IVariable<IV>) term.getValueExpression();

        }

        return defaultValue;

    }

    /**
     * Helper.
     */
    public List<TermNode> get(final IRI param) {

        if (params.containsKey(param)) {

            return params.get(param);

        }

        return Collections.emptyList();

    }

    /**
     * Iterator.
     */
    public Iterator<Map.Entry<IRI, List<TermNode>>> iterator() {

        return params.entrySet().iterator();

    }

    @Override
    public String toString() {

        final StringBuilder sb = new StringBuilder();

        sb.append("[");

        for (Map.Entry<IRI, List<TermNode>> e : params.entrySet()) {

            final IRI param = e.getKey();

            final List<TermNode> terms = e.getValue();

            sb.append(param).append(": ");

            if (terms.size() == 1) {

                sb.append(terms.get(0));

            } else {

                sb.append("[");
                for (TermNode t : terms) {

                    sb.append(t).append(", ");

                }
                sb.setLength(sb.length() - 2);
                sb.append("]");

            }

            sb.append(", ");

        }

        if (sb.length() > 1)
            sb.setLength(sb.length() - 2);
        sb.append("]");

        return sb.toString();

    }

    /**
     * Gather the service params (any statement patterns with the subject of
     * {@link BD#SERVICE_PARAM}.
     */
    static public ServiceParams gatherServiceParams(
            final ServiceCallCreateParams createParams) {

        if (createParams == null)
            throw new IllegalArgumentException();

        final AbstractTripleStore store = createParams.getTripleStore();

        if (store == null)
            throw new IllegalArgumentException();

        final ServiceNode serviceNode = createParams.getServiceNode();

        if (serviceNode == null)
            throw new IllegalArgumentException();

        final GraphPatternGroup<IGroupMemberNode> group = serviceNode
                .getGraphPattern();

        if (group == null)
            throw new IllegalArgumentException();

        final ServiceParams serviceParams = new ServiceParams();

        final Iterator<IGroupMemberNode> it = group.iterator();

        while (it.hasNext()) {

            final IGroupMemberNode node = it.next();

            if (node instanceof StatementPatternNode) {

                final StatementPatternNode sp = (StatementPatternNode) node;

                final TermNode s = sp.s();

                if (s.isConstant() && BD.SERVICE_PARAM.equals(s.getValue())) {

                    if (sp.p().isVariable()) {

                        throw new RuntimeException(
                                "not a valid service param triple pattern, "
                                        + "predicate must be constant: " + sp);

                    }

                    final IRI param = (IRI) sp.p().getValue();

                    serviceParams.add(param, sp.o());

                }

            }

        }

        if (log.isDebugEnabled()) {

            log.debug(serviceParams);
            
        }

        return serviceParams;

    }

} // class ServiceParams
