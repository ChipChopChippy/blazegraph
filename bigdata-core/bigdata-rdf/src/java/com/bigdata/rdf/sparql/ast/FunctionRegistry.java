package com.bigdata.rdf.sparql.ast;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleIRI;
import org.eclipse.rdf4j.model.vocabulary.FN;
import org.eclipse.rdf4j.model.vocabulary.XSD;
import org.eclipse.rdf4j.query.algebra.Compare.CompareOp;

import com.bigdata.bop.BOp;
import com.bigdata.bop.BOpContextBase;
import com.bigdata.bop.Constant;
import com.bigdata.bop.ContextBindingSet;
import com.bigdata.bop.IBindingSet;
import com.bigdata.bop.IConstant;
import com.bigdata.bop.IValueExpression;
import com.bigdata.bop.IVariable;
import com.bigdata.bop.IVariableOrConstant;
import com.bigdata.bop.ImmutableBOp;
import com.bigdata.bop.NV;
import com.bigdata.bop.aggregate.AggregateBase;
import com.bigdata.bop.aggregate.IAggregate;
import com.bigdata.bop.rdf.aggregate.GROUP_CONCAT;
import com.bigdata.rdf.internal.ILexiconConfiguration;
import com.bigdata.rdf.internal.IV;
import com.bigdata.rdf.internal.constraints.AndBOp;
import com.bigdata.rdf.internal.constraints.BNodeBOp;
import com.bigdata.rdf.internal.constraints.CoalesceBOp;
import com.bigdata.rdf.internal.constraints.CompareBOp;
import com.bigdata.rdf.internal.constraints.ComputedIN;
import com.bigdata.rdf.internal.constraints.ConcatBOp;
import com.bigdata.rdf.internal.constraints.DatatypeBOp;
import com.bigdata.rdf.internal.constraints.DateBOp;
import com.bigdata.rdf.internal.constraints.DateBOp.DateOp;
import com.bigdata.rdf.internal.constraints.DigestBOp;
import com.bigdata.rdf.internal.constraints.DigestBOp.DigestOp;
import com.bigdata.rdf.internal.constraints.EncodeForURIBOp;
import com.bigdata.rdf.internal.constraints.FalseBOp;
import com.bigdata.rdf.internal.constraints.FuncBOp;
import com.bigdata.rdf.internal.constraints.IfBOp;
import com.bigdata.rdf.internal.constraints.InHashBOp;
import com.bigdata.rdf.internal.constraints.IriBOp;
import com.bigdata.rdf.internal.constraints.IsBNodeBOp;
import com.bigdata.rdf.internal.constraints.IsBoundBOp;
import com.bigdata.rdf.internal.constraints.IsLiteralBOp;
import com.bigdata.rdf.internal.constraints.IsNumericBOp;
import com.bigdata.rdf.internal.constraints.IsURIBOp;
import com.bigdata.rdf.internal.constraints.LangBOp;
import com.bigdata.rdf.internal.constraints.LangMatchesBOp;
import com.bigdata.rdf.internal.constraints.LcaseBOp;
import com.bigdata.rdf.internal.constraints.MathBOp;
import com.bigdata.rdf.internal.constraints.MathBOp.MathOp;
import com.bigdata.rdf.internal.constraints.NotBOp;
import com.bigdata.rdf.internal.constraints.NowBOp;
import com.bigdata.rdf.internal.constraints.NumericBOp;
import com.bigdata.rdf.internal.constraints.NumericBOp.NumericOp;
import com.bigdata.rdf.internal.constraints.OrBOp;
import com.bigdata.rdf.internal.constraints.RandBOp;
import com.bigdata.rdf.internal.constraints.RegexBOp;
import com.bigdata.rdf.internal.constraints.ReplaceBOp;
import com.bigdata.rdf.internal.constraints.SameTermBOp;
import com.bigdata.rdf.internal.constraints.SparqlTypeErrorBOp;
import com.bigdata.rdf.internal.constraints.StrAfterBOp;
import com.bigdata.rdf.internal.constraints.StrBOp;
import com.bigdata.rdf.internal.constraints.StrBeforeBOp;
import com.bigdata.rdf.internal.constraints.StrcontainsBOp;
import com.bigdata.rdf.internal.constraints.StrdtBOp;
import com.bigdata.rdf.internal.constraints.StrendsBOp;
import com.bigdata.rdf.internal.constraints.StrlangBOp;
import com.bigdata.rdf.internal.constraints.StrlenBOp;
import com.bigdata.rdf.internal.constraints.StrstartsBOp;
import com.bigdata.rdf.internal.constraints.SubstrBOp;
import com.bigdata.rdf.internal.constraints.TrueBOp;
import com.bigdata.rdf.internal.constraints.UUIDBOp;
import com.bigdata.rdf.internal.constraints.UcaseBOp;
import com.bigdata.rdf.internal.constraints.XsdLongBOp;
import com.bigdata.rdf.internal.constraints.XsdStrBOp;
import com.bigdata.rdf.internal.constraints.XsdUnsignedLongBOp;
import com.bigdata.rdf.lexicon.LexiconRelation;
import com.bigdata.rdf.sparql.ast.eval.AST2BOpUtility;
import com.bigdata.rdf.sparql.ast.eval.ASTSearchInSearchOptimizer;
import com.bigdata.rdf.sparql.ast.optimizers.IASTOptimizer;
import com.bigdata.rdf.store.BD;

/**
 * Registry for built-in and external SPARQL functions.
 * <p>
 * Note: Use an alternative namespace for functions which do not have official
 * namespaces.
 * 
 * @see <a href="http://www.w3.org/2005/xpath-functions/">XPath functions</a>
 * @see <a
 *      href="http://lists.w3.org/Archives/Public/public-rdf-dawg/2006AprJun/att-0138/sparql-function-uri.html">Sparql
 *      functions</a>
 * 
 *      TODO We should restrict registration of functions in the
 *      {@link BD#NAMESPACE} to static initialization.  The code
 *      already does not permit replacement of registered functions.
 */
@SuppressWarnings("rawtypes")
public class FunctionRegistry {

    public interface Annotations extends AggregateBase.Annotations{
    }

	private static ConcurrentMap<IRI, Factory> factories = new ConcurrentHashMap<IRI, Factory>();

	public static final String SPARQL_FUNCTIONS = "http://www.w3.org/2006/sparql-functions#";
	public static final String XPATH_FUNCTIONS = "http://www.w3.org/2005/xpath-functions#";

	/**
     * Functions in SPARQL 1.0 for which there is not yet any official IRI.
     */
    public static final String SPARQL10_UNDEFINED_FUNCTIONS = "http://www.bigdata.com/sparql-1.0-undefined-functions";

    /**
     * Functions in SPARQL 1.1 for which there is not yet any official IRI.
     */
    public static final String SPARQL11_UNDEFINED_FUNCTIONS = "http://www.bigdata.com/sparql-1.1-undefined-functions";

    public static final IRI BOUND = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(SPARQL_FUNCTIONS+"bound");
    public static final IRI IS_LITERAL = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(SPARQL_FUNCTIONS+"isLiteral");
    public static final IRI IS_BLANK = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(SPARQL_FUNCTIONS+"isBlank");
    public static final IRI IS_URI = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(SPARQL_FUNCTIONS+"isURI");
    public static final IRI IS_IRI = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(SPARQL_FUNCTIONS+"isIRI");
    public static final IRI IS_NUMERIC = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(SPARQL_FUNCTIONS+"isNumeric");

    public static final IRI STR = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(SPARQL_FUNCTIONS+"str");
    public static final IRI LANG = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(SPARQL_FUNCTIONS+"lang");
    public static final IRI DATATYPE = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(SPARQL_FUNCTIONS+"datatype");
    public static final IRI LANG_MATCHES = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(SPARQL_FUNCTIONS+"langMatches");
    public static final IRI REGEX = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(XPATH_FUNCTIONS+"matches");
    public static final IRI OR = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(SPARQL_FUNCTIONS+"logical-or");
    public static final IRI AND = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(SPARQL_FUNCTIONS+"logical-and");
    public static final IRI NOT = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(XPATH_FUNCTIONS+"not");
    public static final IRI SAME_TERM = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(SPARQL_FUNCTIONS+"sameTerm");
    public static final IRI CONCAT = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(SPARQL_FUNCTIONS+"concat");
    public static final IRI COALESCE = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(SPARQL_FUNCTIONS+"coalesce");

    public static final IRI IN = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(SPARQL_FUNCTIONS+"in");
    public static final IRI NOT_IN = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(SPARQL_FUNCTIONS+"notIn");

    public static final IRI IF = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(SPARQL_FUNCTIONS+"if");

    public static final IRI NOW = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(XPATH_FUNCTIONS+"now");
    public static final IRI YEAR = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(XPATH_FUNCTIONS+"year-from-dateTime");
    public static final IRI MONTH = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(XPATH_FUNCTIONS+"month-from-dateTime");
    public static final IRI DAY = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(XPATH_FUNCTIONS+"day-from-dateTime");
    public static final IRI HOURS = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(XPATH_FUNCTIONS+"hours-from-dateTime");
    public static final IRI MINUTES = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(XPATH_FUNCTIONS+"minutes-from-dateTime");
    public static final IRI SECONDS = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(XPATH_FUNCTIONS+"seconds-from-dateTime");
    public static final IRI TZ = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(XPATH_FUNCTIONS+"tz");
    public static final IRI TIMEZONE = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(XPATH_FUNCTIONS+"timezone-from-dateTime");
    
    public static final IRI MD5 = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(SPARQL_FUNCTIONS+"md5");
    public static final IRI SHA1 = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(SPARQL_FUNCTIONS+"sha1");
    public static final IRI SHA224 = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(SPARQL_FUNCTIONS+"sha224");
    public static final IRI SHA256 = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(SPARQL_FUNCTIONS+"sha256");
    public static final IRI SHA384 = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(SPARQL_FUNCTIONS+"sha384");
    public static final IRI SHA512 = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(SPARQL_FUNCTIONS+"sha512");

    public static final IRI UUID = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(SPARQL_FUNCTIONS+"uuid");
    public static final IRI STRUUID = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(SPARQL_FUNCTIONS+"struuid");
    
    public static final IRI STR_DT = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(SPARQL_FUNCTIONS+"strdt");
    public static final IRI STR_LANG = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(SPARQL_FUNCTIONS+"strlang");
    public static final IRI LCASE = FN.LOWER_CASE;//org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(XPATH_FUNCTIONS+"lower-case");
    public static final IRI UCASE = FN.UPPER_CASE;//org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(XPATH_FUNCTIONS+"upper-case");
    public static final IRI ENCODE_FOR_URI = FN.ENCODE_FOR_URI;//org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(SPARQL_FUNCTIONS+"encodeForUri");
    public static final IRI STR_LEN = FN.STRING_LENGTH;//org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(XPATH_FUNCTIONS+"string-length");
    public static final IRI SUBSTR = FN.SUBSTRING;//org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(SPARQL_FUNCTIONS+"substr");
    public static final IRI CONTAINS = FN.CONTAINS;
    public static final IRI STARTS_WITH = FN.STARTS_WITH; 
    public static final IRI ENDS_WITH = FN.ENDS_WITH;
    public static final IRI STR_AFTER = FN.SUBSTRING_AFTER; // FIXME implement. See StrAfter
    public static final IRI STR_BEFORE = FN.SUBSTRING_BEFORE; // FIXME implement. See StrBefore
    public static final IRI REPLACE = FN.REPLACE; // FIXME implement. See Replace.

    /**
     * The IRI function, as defined in <a
     * href="http://www.w3.org/TR/sparql11-query/#SparqlOps">SPARQL 1.1 Query
     * Language for RDF</a>.
     */
    public static final IRI IRI = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(SPARQL11_UNDEFINED_FUNCTIONS
            + "iri");

    /**
     * The BNODE()/BNODE(Literal) function as defined in <a
     * href="http://www.w3.org/TR/sparql11-query/#SparqlOps">SPARQL 1.1 Query
     * Language for RDF</a>.
     */
    public static final IRI BNODE = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(SPARQL11_UNDEFINED_FUNCTIONS+ "bnode");

    /**
     * The EXISTS(graphPattern) function as defined in <a
     * href="http://www.w3.org/TR/sparql11-query/#SparqlOps">SPARQL 1.1 Query
     * Language for RDF</a>.
     */
    public static final IRI EXISTS = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(SPARQL11_UNDEFINED_FUNCTIONS
            + "exists");

    /**
     * The NOT EXISTS(graphPattern) function as defined in <a
     * href="http://www.w3.org/TR/sparql11-query/#SparqlOps">SPARQL 1.1 Query
     * Language for RDF</a>.
     */
    public static final IRI NOT_EXISTS = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(
            SPARQL11_UNDEFINED_FUNCTIONS + "not-exists");

    public static final IRI EQ = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(XPATH_FUNCTIONS+"equal-to");
    public static final IRI NE = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(XPATH_FUNCTIONS+"not-equal-to");
    public static final IRI GT = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(XPATH_FUNCTIONS+"greater-than");
    public static final IRI GE = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(XPATH_FUNCTIONS+"greater-than-or-equal-to");
    public static final IRI LT = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(XPATH_FUNCTIONS+"less-than");
    public static final IRI LE = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(XPATH_FUNCTIONS+"less-than-or-equal-to");

    public static final IRI ADD = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(XPATH_FUNCTIONS+"numeric-add");
    public static final IRI SUBTRACT = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(XPATH_FUNCTIONS+"numeric-subtract");
    public static final IRI MULTIPLY = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(XPATH_FUNCTIONS+"numeric-multiply");
    public static final IRI DIVIDE = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(XPATH_FUNCTIONS+"numeric-divide");
    
    public static final IRI ABS   = FN.NUMERIC_ABS;//org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(XPATH_FUNCTIONS+"numeric-abs");
    public static final IRI ROUND = FN.NUMERIC_ROUND;//org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(SPARQL_FUNCTIONS+"numeric-round");
    public static final IRI CEIL  = FN.NUMERIC_CEIL;//org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(SPARQL_FUNCTIONS+"numeric-ceil");
    public static final IRI FLOOR = FN.NUMERIC_FLOOR;//org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(SPARQL_FUNCTIONS+"numeric-floor");
    public static final IRI RAND  = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(SPARQL_FUNCTIONS+"numeric-rand");

    public static final IRI AVERAGE = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(SPARQL_FUNCTIONS+"average");
    public static final IRI COUNT = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(SPARQL_FUNCTIONS+"count");
    public static final IRI GROUP_CONCAT = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(SPARQL_FUNCTIONS+"groupConcat");
    public static final IRI MAX = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(SPARQL_FUNCTIONS+"max");
    public static final IRI MIN = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(SPARQL_FUNCTIONS+"min");
    public static final IRI SAMPLE = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(SPARQL_FUNCTIONS+"sample");
    public static final IRI SUM = org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance().createIRI(SPARQL_FUNCTIONS+"sum");

    public static final IRI XSD_BOOL = XSD.BOOLEAN;
    public static final IRI XSD_DT = XSD.DATETIME;
    public static final IRI XSD_DEC = XSD.DECIMAL;
    public static final IRI XSD_DBL = XSD.DOUBLE;
    public static final IRI XSD_FLT = XSD.FLOAT;
    public static final IRI XSD_INT = XSD.INTEGER;
    public static final IRI XSD_STR = XSD.STRING;
    public static final IRI XSD_DATE = XSD.DATE;
    public static final IRI XSD_LONG = XSD.LONG;
    public static final IRI XSD_UNSIGNED_LONG = XSD.UNSIGNED_LONG;

    static {
        add(AVERAGE, new AggregateFactory() {
            public IValueExpression<? extends IV> create(final BOpContextBase context, final GlobalAnnotations globals,
                    Map<String, Object> scalarValues, final ValueExpressionNode... args) {

                checkArgs(args, ValueExpressionNode.class);

//                final IValueExpression ve = args[0].getValueExpression();
                final IValueExpression ve = AST2BOpUtility.toVE(context, globals, args[0]);
                
                return new com.bigdata.bop.rdf.aggregate.AVERAGE(new BOp[]{ve}, scalarValues);

            }
        });

        add(COUNT, new AggregateFactory() {
            public IValueExpression<? extends IV> create(final BOpContextBase context, final GlobalAnnotations globals,
                    Map<String, Object> scalarValues, final ValueExpressionNode... args) {

                checkArgs(args, ValueExpressionNode.class);

//                final IValueExpression ve = args[0].getValueExpression();
                final IValueExpression ve = AST2BOpUtility.toVE(context, globals, args[0]);

                return new com.bigdata.bop.rdf.aggregate.COUNT(new BOp[]{ve}, scalarValues);

            }
        });

        add(GROUP_CONCAT, new GroupConcatFactory());

        add(MAX, new AggregateFactory() {
            public IValueExpression<? extends IV> create(final BOpContextBase context, final GlobalAnnotations globals,
                    Map<String, Object> scalarValues, final ValueExpressionNode... args) {

                checkArgs(args, ValueExpressionNode.class);

                IValueExpression<? extends IV> expressions[]=new IValueExpression[args.length];
                for(int i=0;i<args.length;i++){
                    expressions[i] = AST2BOpUtility.toVE(context, globals, args[i]);
                }
                return new com.bigdata.bop.rdf.aggregate.MAX(expressions, scalarValues);
            }
        });

        add(MIN, new AggregateFactory() {
            public IValueExpression<? extends IV> create(final BOpContextBase context, final GlobalAnnotations globals,
                    Map<String, Object> scalarValues, final ValueExpressionNode... args) {

                checkArgs(args, ValueExpressionNode.class);

                IValueExpression<? extends IV> expressions[]=new IValueExpression[args.length];
                for(int i=0;i<args.length;i++){
                    expressions[i] = AST2BOpUtility.toVE(context, globals, args[i]);
                }
                return new com.bigdata.bop.rdf.aggregate.MIN(expressions, scalarValues);

            }
        });

        add(SAMPLE, new AggregateFactory() {
            public IValueExpression<? extends IV> create(final BOpContextBase context, final GlobalAnnotations globals,
                    Map<String, Object> scalarValues, final ValueExpressionNode... args) {

                checkArgs(args, ValueExpressionNode.class);

//              final IValueExpression ve = args[0].getValueExpression();
                final IValueExpression ve = AST2BOpUtility.toVE(context, globals, args[0]);

                return new com.bigdata.bop.rdf.aggregate.SAMPLE(false,ve);

            }
        });

        add(SUM, new AggregateFactory() {
            public IValueExpression<? extends IV> create(final BOpContextBase context, final GlobalAnnotations globals,
                    Map<String, Object> scalarValues, final ValueExpressionNode... args) {

                checkArgs(args, ValueExpressionNode.class);

                IValueExpression<? extends IV> expressions[]=new IValueExpression[args.length];
                for(int i=0;i<args.length;i++){
                    expressions[i] = AST2BOpUtility.toVE(context, globals, args[i]);
                }
                return new com.bigdata.bop.rdf.aggregate.SUM(expressions, scalarValues);

            }
        });
		// add the bigdata built-ins

		add(BOUND, new Factory() {
			public IValueExpression<? extends IV> create(final BOpContextBase context, final GlobalAnnotations globals,
					Map<String, Object> scalarValues, final ValueExpressionNode... args) {

				checkArgs(args, VarNode.class);

                final IVariable<IV> var = ((VarNode) args[0])
                        .getValueExpression();

                return new IsBoundBOp(var);

			}
		});

		add(IS_LITERAL, new Factory() {
			public IValueExpression<? extends IV> create(final BOpContextBase context, final GlobalAnnotations globals,
					Map<String, Object> scalarValues, final ValueExpressionNode... args) {

				checkArgs(args, ValueExpressionNode.class);

//              final IValueExpression ve = args[0].getValueExpression();
                final IValueExpression ve = AST2BOpUtility.toVE(context, globals, args[0]);

				return new IsLiteralBOp(ve);

			}
		});

		add(IS_BLANK, new Factory() {
			public IValueExpression<? extends IV> create(final BOpContextBase context, final GlobalAnnotations globals,
					Map<String, Object> scalarValues, final ValueExpressionNode... args) {

				checkArgs(args, ValueExpressionNode.class);

//              final IValueExpression ve = args[0].getValueExpression();
                final IValueExpression ve = AST2BOpUtility.toVE(context, globals, args[0]);

				return new IsBNodeBOp(ve);

			}
		});

		add(IS_URI, new Factory() {
			public IValueExpression<? extends IV> create(final BOpContextBase context, final GlobalAnnotations globals,
					Map<String, Object> scalarValues, final ValueExpressionNode... args) {

				checkArgs(args, ValueExpressionNode.class);

//				final IValueExpression<? extends IV> var = args[0].getValueExpression();
                final IValueExpression ve = AST2BOpUtility.toVE(context, globals, args[0]);

                return new IsURIBOp(ve);

			}
		});
		add(IS_IRI, new Factory() {
            public IValueExpression<? extends IV> create(final BOpContextBase context, final GlobalAnnotations globals,
                    Map<String, Object> scalarValues, final ValueExpressionNode... args) {

                checkArgs(args, ValueExpressionNode.class);

//                final IValueExpression<? extends IV> var = args[0].getValueExpression();
                final IValueExpression ve = AST2BOpUtility.toVE(context,globals, args[0]);

				return new IsURIBOp(ve);

			}
		});
		add(IS_NUMERIC, new Factory() {
            public IValueExpression<? extends IV> create(final BOpContextBase context, final GlobalAnnotations globals,
                    Map<String, Object> scalarValues, final ValueExpressionNode... args) {

                checkArgs(args, ValueExpressionNode.class);

//                final IValueExpression<? extends IV> var = args[0].getValueExpression();
                final IValueExpression ve = AST2BOpUtility.toVE(context,globals, args[0]);

                return new IsNumericBOp(ve);

            }
        });

		add(STR, new Factory() {
			public IValueExpression<? extends IV> create(final BOpContextBase context, final GlobalAnnotations globals,
					Map<String, Object> scalarValues, final ValueExpressionNode... args) {

				checkArgs(args, ValueExpressionNode.class);

//				final IValueExpression<? extends IV> var = args[0].getValueExpression();
                final IValueExpression ve = AST2BOpUtility.toVE(context,globals, args[0]);

				return new StrBOp(ve, globals);

			}
		});

		add(ENCODE_FOR_URI,new Factory() {
            public IValueExpression<? extends IV> create(final BOpContextBase context, final GlobalAnnotations globals,
                    Map<String, Object> scalarValues, final ValueExpressionNode... args) {

                checkArgs(args, ValueExpressionNode.class);

//                final IValueExpression<? extends IV> var = args[0].getValueExpression();
                final IValueExpression ve = AST2BOpUtility.toVE(context,globals, args[0]);

                return new EncodeForURIBOp(ve, globals);

            }
        });

		add(LCASE,new Factory() {
            public IValueExpression<? extends IV> create(final BOpContextBase context, final GlobalAnnotations globals,
                    Map<String, Object> scalarValues, final ValueExpressionNode... args) {

                checkArgs(args, ValueExpressionNode.class);

                final IValueExpression<? extends IV> ve = AST2BOpUtility.toVE(context,globals, args[0]);
                return new LcaseBOp(ve, globals);

            }
        });

		add(UCASE,new Factory() {
            public IValueExpression<? extends IV> create(final BOpContextBase context, final GlobalAnnotations globals,
                    Map<String, Object> scalarValues, final ValueExpressionNode... args) {

                checkArgs(args, ValueExpressionNode.class);

                final IValueExpression<? extends IV> ve = AST2BOpUtility.toVE(context,globals, args[0]);
                return new UcaseBOp(ve, globals);

            }
        });
		add(STR_DT,new Factory() {
            public IValueExpression<? extends IV> create(final BOpContextBase context, final GlobalAnnotations globals,
                    Map<String, Object> scalarValues, final ValueExpressionNode... args) {

                checkArgs(args, ValueExpressionNode.class,ValueExpressionNode.class);

                final IValueExpression<? extends IV> ve = AST2BOpUtility.toVE(context,globals, args[0]);
                final IValueExpression<? extends IV> type = AST2BOpUtility.toVE(context,globals, args[1]);
                return new StrdtBOp(ve,type ,globals);

            }
        });
		add(STR_LANG,new Factory() {
            public IValueExpression<? extends IV> create(final BOpContextBase context, final GlobalAnnotations globals,
                    Map<String, Object> scalarValues, final ValueExpressionNode... args) {

                checkArgs(args, ValueExpressionNode.class,ValueExpressionNode.class);

                final IValueExpression<? extends IV> var = AST2BOpUtility.toVE(context,globals, args[0]);
                final IValueExpression<? extends IV> lang = AST2BOpUtility.toVE(context,globals, args[1]);
                return new StrlangBOp(var,lang ,globals);

            }
        });
		add(STR_LEN,new Factory() {
            public IValueExpression<? extends IV> create(final BOpContextBase context, final GlobalAnnotations globals,
                    Map<String, Object> scalarValues, final ValueExpressionNode... args) {

                checkArgs(args, ValueExpressionNode.class);

                final IValueExpression<? extends IV> var = AST2BOpUtility.toVE(context,globals, args[0]);
                return new StrlenBOp(var,globals);

            }
        });
		add(SUBSTR,new Factory() {
            public IValueExpression<? extends IV> create(final BOpContextBase context, final GlobalAnnotations globals,
                    Map<String, Object> scalarValues, final ValueExpressionNode... args) {

                checkArgs(args, ValueExpressionNode.class,ValueExpressionNode.class);

                final IValueExpression<? extends IV> var = AST2BOpUtility.toVE(context,
                        globals, args[0]);

//                if (args.length == 2) {
//                	
//                } else {
                	
	                final IValueExpression<? extends IV> start = AST2BOpUtility
	                        .toVE(context, globals, args[1]);
	                
	                final IValueExpression<? extends IV> length = args.length >= 3 ? AST2BOpUtility
	                        .toVE(context, globals, args[2]) : null;
	                
	                return new SubstrBOp(var, start, length, globals);
	                
//                }

            }
        });
        add(CONTAINS,new Factory() {
            public IValueExpression<? extends IV> create(final BOpContextBase context, final GlobalAnnotations globals,
                    Map<String, Object> scalarValues, final ValueExpressionNode... args) {

                checkArgs(args, ValueExpressionNode.class,
                        ValueExpressionNode.class);

                final IValueExpression<? extends IV> x = AST2BOpUtility.toVE(context,
                        globals, args[0]);

                final IValueExpression<? extends IV> y = AST2BOpUtility.toVE(context,
                        globals, args[1]);

                return new StrcontainsBOp(x, y);

            }
        });
		add(LANG, new Factory() {
			public IValueExpression<? extends IV> create(final BOpContextBase context, final GlobalAnnotations globals,
					Map<String, Object> scalarValues, final ValueExpressionNode... args) {

				checkArgs(args, ValueExpressionNode.class);

				final IValueExpression<? extends IV> ve =
					AST2BOpUtility.toVE(context,globals, args[0]);
				return new LangBOp(ve, globals);

			}
		});
		add(CONCAT, new Factory() {
            public IValueExpression<? extends IV> create(final BOpContextBase context, final GlobalAnnotations globals,
                    Map<String, Object> scalarValues, final ValueExpressionNode... args) {

                checkArgs(args, ValueExpressionNode.class);
                IValueExpression<? extends IV> expressions[]=new IValueExpression[args.length];
                for(int i=0;i<args.length;i++){
                    expressions[i] = AST2BOpUtility.toVE(context,globals, args[i]);
                }
                return new ConcatBOp(globals,expressions);

            }
        });
		add(COALESCE, new Factory() {
            public IValueExpression<? extends IV> create(final BOpContextBase context, final GlobalAnnotations globals,
                    Map<String, Object> scalarValues, final ValueExpressionNode... args) {

                checkArgs(args, ValueExpressionNode.class);
                IValueExpression<? extends IV> expressions[]=new IValueExpression[args.length];
                for(int i=0;i<args.length;i++){
                    expressions[i] = AST2BOpUtility.toVE(context,globals, args[i]);
                }
                return new CoalesceBOp(globals,expressions);

            }
        });
		add(IF, new Factory() {
            public IValueExpression<? extends IV> create(final BOpContextBase context, final GlobalAnnotations globals,
                    Map<String, Object> scalarValues, final ValueExpressionNode... args) {

                checkArgs(args, ValueExpressionNode.class,ValueExpressionNode.class,ValueExpressionNode.class);
                final IValueExpression<? extends IV> conditional = AST2BOpUtility.toVE(context,globals, args[0]);
                final IValueExpression<? extends IV> expression1 = AST2BOpUtility.toVE(context,globals, args[1]);
                final IValueExpression<? extends IV> expression2 = AST2BOpUtility.toVE(context,globals, args[2]);
                return new IfBOp(conditional,expression1,expression2);
            }
        });
		add(DATATYPE, new Factory() {
			public IValueExpression<? extends IV> create(final BOpContextBase context, final GlobalAnnotations globals,
					Map<String, Object> scalarValues, final ValueExpressionNode... args) {

				checkArgs(args, ValueExpressionNode.class);

				final IValueExpression<? extends IV> ve =
					AST2BOpUtility.toVE(context,globals, args[0]);
				return new DatatypeBOp(ve, globals);

			}
		});

		add(RAND,new Factory() {
            public IValueExpression<? extends IV> create(final BOpContextBase context, final GlobalAnnotations globals,
                    Map<String, Object> scalarValues, final ValueExpressionNode... args) {
                if(args!=null&&args.length>0){
                    throw new IllegalArgumentException("wrong # of args");
                }
                return new RandBOp();

            }
        });

		add(LANG_MATCHES, new Factory() {
			public IValueExpression<? extends IV> create(final BOpContextBase context, final GlobalAnnotations globals,
					Map<String, Object> scalarValues, final ValueExpressionNode... args) {

				checkArgs(args,
						ValueExpressionNode.class, ValueExpressionNode.class);

				final IValueExpression<? extends IV> left =
					AST2BOpUtility.toVE(context,globals, args[0]);

				final IValueExpression<? extends IV> right =
					AST2BOpUtility.toVE(context,globals, args[1]);
				
				return new LangMatchesBOp(left, right);

			}
		});

		add(REGEX, new Factory() {
			public IValueExpression<? extends IV> create(final BOpContextBase context, final GlobalAnnotations globals,
					Map<String, Object> scalarValues, final ValueExpressionNode... args) {

				checkArgs(args,
						ValueExpressionNode.class, ValueExpressionNode.class);
				
				final IValueExpression<? extends IV> var =
					AST2BOpUtility.toVE(context,globals, args[0]);
				
				final IValueExpression<? extends IV> pattern =
					AST2BOpUtility.toVE(context,globals, args[1]);
				
				final RegexBOp regex;
				
				if (args.length == 2) {

					regex = new RegexBOp(var, pattern);

				} else {

					final IValueExpression<? extends IV> flags =
						AST2BOpUtility.toVE(context,globals, args[2]);

					regex = new RegexBOp(var, pattern, flags);

				}
				
				return regex;

			}
		});

		add(AND, new Factory() {
			public IValueExpression<? extends IV> create(final BOpContextBase context, final GlobalAnnotations globals,
					Map<String, Object> scalarValues, final ValueExpressionNode... args) {

				checkArgs(args,
						ValueExpressionNode.class, ValueExpressionNode.class);

				final IValueExpression<? extends IV> left =
					AST2BOpUtility.toVE(context,globals, args[0]);
				
				final IValueExpression<? extends IV> right =
					AST2BOpUtility.toVE(context,globals, args[1]);
				
				return new AndBOp(left, right);

			}
		});

		add(OR, new Factory() {
			public IValueExpression<? extends IV> create(final BOpContextBase context, final GlobalAnnotations globals,
					Map<String, Object> scalarValues, final ValueExpressionNode... args) {

				checkArgs(args,
						ValueExpressionNode.class, ValueExpressionNode.class);

				final IValueExpression<? extends IV> left =
					AST2BOpUtility.toVE(context, globals, args[0]);
				
				final IValueExpression<? extends IV> right =
					AST2BOpUtility.toVE(context, globals, args[1]);

				return new OrBOp(left, right);

			}
		});

		add(NOT, new Factory() {
			public IValueExpression<? extends IV> create(final BOpContextBase context, final GlobalAnnotations globals,
					Map<String, Object> scalarValues, final ValueExpressionNode... args) {

				checkArgs(args, ValueExpressionNode.class);

                final IValueExpression<? extends IV> arg = AST2BOpUtility.toVE(context, globals, args[0]);

				return new NotBOp(arg);

			}
		});

		add(IRI,new Factory() {
            public IValueExpression<? extends IV> create(final BOpContextBase context, final GlobalAnnotations globals,
                    Map<String, Object> scalarValues, final ValueExpressionNode... args) {

                checkArgs(args, ValueExpressionNode.class);

//                final IValueExpression<? extends IV> var = args[0].getValueExpression();
                final IValueExpression ve = AST2BOpUtility.toVE(context,globals, args[0]);
                
                final String baseURI = (String)
                        scalarValues.get(IriBOp.Annotations.BASE_URI);

                return new IriBOp(ve, baseURI, globals);

            }
        });

		add(BNODE,new Factory() {
            public IValueExpression<? extends IV> create(final BOpContextBase context, final GlobalAnnotations globals,
                    Map<String, Object> scalarValues, final ValueExpressionNode... args) {

            	if (args.length == 0) {
            		
            		return new BNodeBOp(globals);
            		
            	} else {
                
            		checkArgs(args, ValueExpressionNode.class);

//                final IValueExpression<? extends IV> var = args[0].getValueExpression();
	                final IValueExpression ve = AST2BOpUtility.toVE(context,globals, args[0]);
	
	                return new BNodeBOp(ve, globals);

            	}
            	
            }
            
        });

		add(STARTS_WITH,new Factory() {
            public IValueExpression<? extends IV> create(final BOpContextBase context, final GlobalAnnotations globals,
                    Map<String, Object> scalarValues, final ValueExpressionNode... args) {

                checkArgs(args, ValueExpressionNode.class,ValueExpressionNode.class);

                final IValueExpression<? extends IV> var = AST2BOpUtility.toVE(context,
                        globals, args[0]);
                
                final IValueExpression<? extends IV> token = AST2BOpUtility
                        .toVE(context, globals, args[1]);
	                
                return new StrstartsBOp(var, token);
	                
            }
        });
		
		add(ENDS_WITH,new Factory() {
            public IValueExpression<? extends IV> create(final BOpContextBase context, final GlobalAnnotations globals,
                    Map<String, Object> scalarValues, final ValueExpressionNode... args) {

                checkArgs(args, ValueExpressionNode.class,ValueExpressionNode.class);

                final IValueExpression<? extends IV> var = AST2BOpUtility.toVE(context,
                        globals, args[0]);
                
                final IValueExpression<? extends IV> token = AST2BOpUtility
                        .toVE(context, globals, args[1]);
	                
                return new StrendsBOp(var, token);
	                
            }
        });
		
		add(STR_BEFORE,new Factory() {
            public IValueExpression<? extends IV> create(final BOpContextBase context, final GlobalAnnotations globals,
                    Map<String, Object> scalarValues, final ValueExpressionNode... args) {

                checkArgs(args, ValueExpressionNode.class, ValueExpressionNode.class);

                final IValueExpression<? extends IV> arg1 = AST2BOpUtility.toVE(context,
                        globals, args[0]);

                final IValueExpression<? extends IV> arg2 = AST2BOpUtility.toVE(context,
                		globals, args[1]);
                
                return new StrBeforeBOp(arg1, arg2, globals);

            }
        });
		
		add(STR_AFTER,new Factory() {
            public IValueExpression<? extends IV> create(final BOpContextBase context, final GlobalAnnotations globals,
                    Map<String, Object> scalarValues, final ValueExpressionNode... args) {

                checkArgs(args, ValueExpressionNode.class, ValueExpressionNode.class);

                final IValueExpression<? extends IV> arg1 = AST2BOpUtility.toVE(context,
                        globals, args[0]);

                final IValueExpression<? extends IV> arg2 = AST2BOpUtility.toVE(context,
                		globals, args[1]);
                
                return new StrAfterBOp(arg1, arg2, globals);

            }
        });
		
		add(REPLACE,new Factory() {
            public IValueExpression<? extends IV> create(final BOpContextBase context, final GlobalAnnotations globals,
                    Map<String, Object> scalarValues, final ValueExpressionNode... args) {

                checkArgs(args, ValueExpressionNode.class, ValueExpressionNode.class, ValueExpressionNode.class);

				final IValueExpression<? extends IV> var =
					AST2BOpUtility.toVE(context,globals, args[0]);
				
				final IValueExpression<? extends IV> pattern =
					AST2BOpUtility.toVE(context,globals, args[1]);

				final IValueExpression<? extends IV> replacement =
					AST2BOpUtility.toVE(context,globals, args[2]);

				if (args.length == 3) {

					return new ReplaceBOp(var, pattern, replacement, globals);

				} else {

					final IValueExpression<? extends IV> flags =
						AST2BOpUtility.toVE(context,globals, args[3]);

					return new ReplaceBOp(var, pattern, replacement, flags, globals);

				}

            }
        });
		
		add(IN, new InFactory(false/*not*/));

        add(NOT_IN, new InFactory(true/*not*/));

		add(SAME_TERM, SameTermFactory.INSTANCE);

		add(EQ, new CompareFactory(CompareOp.EQ));

		add(NE, new CompareFactory(CompareOp.NE));

		add(GT, new CompareFactory(CompareOp.GT));

		add(GE, new CompareFactory(CompareOp.GE));

		add(LT, new CompareFactory(CompareOp.LT));

		add(LE, new CompareFactory(CompareOp.LE));

		add(ADD, new MathFactory(MathOp.PLUS));

		add(SUBTRACT, new MathFactory(MathOp.MINUS));

		add(MULTIPLY, new MathFactory(MathOp.MULTIPLY));

		add(DIVIDE, new MathFactory(MathOp.DIVIDE));

		add(ABS, new NumericFactory(NumericOp.ABS));

		add(ROUND, new NumericFactory(NumericOp.ROUND));

		add(CEIL, new NumericFactory(NumericOp.CEIL));

		add(FLOOR, new NumericFactory(NumericOp.FLOOR));

	    add(XSD_BOOL, new CastFactory(XSD.BOOLEAN.toString()));

	    add(XSD_DT, new CastFactory(XSD.DATETIME.toString()));

	    add(XSD_DEC, new CastFactory(XSD.DECIMAL.toString()));

	    add(XSD_DBL, new CastFactory(XSD.DOUBLE.toString()));

	    add(XSD_FLT, new CastFactory(XSD.FLOAT.toString()));

	    add(XSD_INT, new CastFactory(XSD.INTEGER.toString()));

        add(XSD_LONG, new Factory() {
            public IValueExpression<? extends IV> create(final BOpContextBase context, final GlobalAnnotations globals,
                    Map<String, Object> scalarValues, final ValueExpressionNode... args) {

                checkArgs(args, ValueExpressionNode.class);

//              final IValueExpression<? extends IV> var = args[0].getValueExpression();
                final IValueExpression ve = AST2BOpUtility.toVE(context,globals, args[0]);

                return new XsdLongBOp(ve, globals);

            }
        });

        add(XSD_UNSIGNED_LONG, new Factory() {
            public IValueExpression<? extends IV> create(final BOpContextBase context, final GlobalAnnotations globals,
                    Map<String, Object> scalarValues, final ValueExpressionNode... args) {

                checkArgs(args, ValueExpressionNode.class);

//              final IValueExpression<? extends IV> var = args[0].getValueExpression();
                final IValueExpression ve = AST2BOpUtility.toVE(context,globals, args[0]);

                return new XsdUnsignedLongBOp(ve, globals);

            }
        });

	    /*
	     * Changed the xsd:string cast operator to use XsdStrBOp instead of the
	     * Sesame cast function.
	     */
//	    add(XSD_STR, new CastFactory(XSD.STRING.toString()));
		add(XSD_STR, new Factory() {
			public IValueExpression<? extends IV> create(final BOpContextBase context, final GlobalAnnotations globals,
					Map<String, Object> scalarValues, final ValueExpressionNode... args) {

				checkArgs(args, ValueExpressionNode.class);

//				final IValueExpression<? extends IV> var = args[0].getValueExpression();
                final IValueExpression ve = AST2BOpUtility.toVE(context,globals, args[0]);

				return new XsdStrBOp(ve, globals);

			}
		});


	    add(YEAR,new DateFactory(DateOp.YEAR));

	    add(MONTH,new DateFactory(DateOp.MONTH));

	    add(DAY,new DateFactory(DateOp.DAY));

	    add(HOURS,new DateFactory(DateOp.HOURS));

	    add(MINUTES,new DateFactory(DateOp.MINUTES));

	    add(SECONDS,new DateFactory(DateOp.SECONDS));

	    add(TIMEZONE,new DateFactory(DateOp.TIMEZONE));
	    
	    add(XSD_DATE, new DateFactory(DateOp.DATE));

        add(TZ,new DateFactory(DateOp.TZ));

        add(NOW,new Factory() {
            public IValueExpression<? extends IV> create(final BOpContextBase context, final GlobalAnnotations globals,
                    Map<String, Object> scalarValues, final ValueExpressionNode... args) {

                if (args != null && args.length > 0)
                    throw new IllegalArgumentException("no args for NOW()");

                return new NowBOp(globals);

            }
        });

        add(UUID,new Factory() {
            public IValueExpression<? extends IV> create(final BOpContextBase context, final GlobalAnnotations globals,
                    Map<String, Object> scalarValues, final ValueExpressionNode... args) {

                if (args != null && args.length > 0)
                    throw new IllegalArgumentException("no args for UUID()");

                return new UUIDBOp(globals, false);

            }
        });

        add(STRUUID,new Factory() {
            public IValueExpression<? extends IV> create(final BOpContextBase context, final GlobalAnnotations globals,
                    Map<String, Object> scalarValues, final ValueExpressionNode... args) {

                if (args != null && args.length > 0)
                    throw new IllegalArgumentException("no args for STRUUID()");

                return new UUIDBOp(globals, true);

            }
        });

	    add(MD5,new DigestFactory(DigestOp.MD5));

	    add(SHA1,new DigestFactory(DigestOp.SHA1));

	    add(SHA224,new DigestFactory(DigestOp.SHA224));

	    add(SHA256,new DigestFactory(DigestOp.SHA256));

	    add(SHA384,new DigestFactory(DigestOp.SHA384));

        add(SHA512,new DigestFactory(DigestOp.SHA512));

        add(EXISTS, new ExistsFactory(true));

        add(NOT_EXISTS, new ExistsFactory(false));
       
    }

    public static boolean containsFunction(IRI functionUri){
        
        return factories.containsKey(functionUri);
        
    }

    public static boolean isAggregate(final IRI functionUri) {

        final Factory f = factories.get(functionUri);

        return f != null && f instanceof AggregateFactory;

    }

    /**
     * Verify type constraints.
     * 
     * @param args
     *            The arguments to some function.
     * @param types
     *            The type constraints. If there are more arguments given than
     *            constraints, then the last constraint in this vararg parameter
     *            will be used to validate the additional arguments.
     * @throws IllegalArgumentException
     *             if the type constraints are violated.
     */
    public static final void checkArgs(final ValueExpressionNode[] args,
    		final Class... types) {

        if (args.length < types.length) {

            throw new IllegalArgumentException("wrong # of args");

    	}

    	for (int i = 0; i < args.length; i++) {

    	    if (!types[i >= types.length ? types.length - 1 : i]
                    .isAssignableFrom(args[i].getClass())) {
            
    	        throw new IllegalArgumentException("wrong type for arg# " + i
                        + ": " + args[i].getClass());
            
    	    }
    	    
    	}

    }

    /**
     * Convert a {@link FunctionNode} into an {@link IValueExpression}.
     * 
     * @param globals
     *            The global annotations, including the lexicon namespace.
     * @param functionURI
     *            The function IRI.
     * @param scalarValues
     *            Scalar values for the function (optional). This is used for
     *            things like the <code>separator</code> in GROUP_CONCAT.
     * @param args
     *            The function arguments.
     *            
     * @return The {@link IValueExpression}.
     */
    public static final IValueExpression<? extends IV> toVE(
            final BOpContextBase context,// BLZG-1343
			final GlobalAnnotations globals, final IRI functionURI,
			final Map<String,Object> scalarValues,
			final ValueExpressionNode... args) {

        if (functionURI == null)
            throw new IllegalArgumentException("functionURI is null");
        
//		final Factory f = factories.get(functionURI);
//
//        if (f == null) {
//            /*
//             * TODO If we eagerly translate FunctionNodes in the AST to IV value
//             * expressions then we should probably attach a function which will
//             * result in a runtime type error when it encounters value
//             * expression for a function IRI which was not known to the backend.
//             * However, if we handle this translation lazily then this might not
//             * be an issue.
//             */
//            throw new IllegalArgumentException("unknown function: "
//                    + functionURI);
//        }

        final Factory f;
        if (factories.containsKey(functionURI)) {
        	
        	f = factories.get(functionURI);
        	
        } else {
        	
        	f = new UnknownFunctionFactory(functionURI);        	
        }
        
		return f.create(context, globals, scalarValues, args);

	}

    /**
     * Register a factory for a function.
     * 
     * @param functionURI
     *            The function IRI.
     * @param factory
     *            The factory.
     * 
     * @throws UnsupportedOperationException
     *             if there is already a {@link Factory} registered for that
     *             IRI.
     */
    public static final void add(final IRI functionURI, final Factory factory) {

        if (factories.putIfAbsent(functionURI, factory) != null) {

            throw new UnsupportedOperationException("Already declared.");

	    }

	}

    /**
     * Register an alias for a functionURI which has already been declared.
     * 
     * @param functionURI
     *            The function IRI.
     * @param aliasURI
     *            The alias.
     * 
     * @throws UnsupportedOperationException
     *             if the function IRI has not been declared.
     * @throws UnsupportedOperationException
     *             if the alias IRI has already been declared.
     */
    public static final void addAlias(final IRI functionURI, final IRI aliasURI) {

        if (!factories.containsKey(functionURI)) {

            throw new UnsupportedOperationException("FunctionURI:"+functionURI+ " not present.");

        }

        if (factories.putIfAbsent(aliasURI, factories.get(functionURI)) != null) {

            throw new UnsupportedOperationException("Already declared.");

        }
    }

    /**
     * Remove a registered function {@link Factory}.
     * 
     * @param functionURI
     *            The {@link IRI} of the function.
     *            
     * @return The factory -or- <code>null</code> if there was no function
     *         registered for that {@link IRI}.
     */
    public static Factory remove(final IRI functionURI) {
        
        return factories.remove(functionURI);
        
    }
    
    /**
     * An interface for creating {@link IValueExpression}s from a function IRI
     * and its arguments.
     */
	public static interface Factory {

        /**
         * Create an {@link IValueExpression} instance.
         * 
         * @param context
         *            The {@link BOpContextBase} required to evaluate
         *            {@link IValueExpression}s. This is used if we need to
         *            statically evaluate an {@link IValueExpression} during
         *            query optimization. During query execution the
         *            {@link ContextBindingSet} will convey this information.
         * @param globals
         *            The global annotations, including the lexicon namespace.
         * @param scalarValues
         *            The scalar arguments (used by some {@link IAggregate}s).
         * @param args
         *            The function arguments.
         * @return The {@link IValueExpression}.
         * @see BLZG-1372 create() was refactored to pass in the
         *      {@link BOpContextBase} to allow correct resolution of the
         *      {@link LexiconRelation} and {@link ILexiconConfiguration} in
         *      order to properly evaluate {@link IValueExpression}s during
         *      query optimization.
         */
		IValueExpression<? extends IV> create(
		        final BOpContextBase context,//BLZG-1343
				final GlobalAnnotations globals,//
				final Map<String,Object> scalarValues,//
				final ValueExpressionNode... args);

	}
	
	/**
	 * Marker interface for aggregate functions.
	 * 
	 * @author <a href="mailto:thompsonbry@users.sourceforge.net">Bryan Thompson</a>
	 */
	public static interface AggregateFactory extends Factory {
		
	}

	public static class CompareFactory implements Factory {

		private final CompareOp op;

		public CompareFactory(final CompareOp op) {
			this.op = op;
		}

        public IValueExpression<? extends IV> create(
                final BOpContextBase context,
                final GlobalAnnotations globals,
                final Map<String, Object> scalarValues,
                final ValueExpressionNode... args) {

            checkArgs(args,
					ValueExpressionNode.class, ValueExpressionNode.class);

			final IValueExpression<? extends IV> left =
				AST2BOpUtility.toVE(context, globals, args[0]);
			
			final IValueExpression<? extends IV> right =
				AST2BOpUtility.toVE(context, globals, args[1]);

	    	/*
	         * If we are dealing with a IRI constant we can use 
	         * SparqlTypeErrorBOp for any operator other than EQ, NE.
	         *
	    	 */

	    	if (!(op == CompareOp.EQ || op == CompareOp.NE)) {
    		
	    		if (left instanceof Constant &&
	    			((Constant<? extends IV>) left).get().isURI()) {

    	    		return SparqlTypeErrorBOp.INSTANCE;

	    		}

	    		if (right instanceof Constant &&
	    			((Constant<? extends IV>) right).get().isURI()) {

    	    		return SparqlTypeErrorBOp.INSTANCE;

	    		}

	    	}

	    	/*
	    	 * If we have a variable or constant for both operands and they
	    	 * are equal, we can optimize to true 
	    	 */
            if (left instanceof IVariableOrConstant &&
        		right instanceof IVariableOrConstant &&
        		left.equals(right)) {
            	
                if (op == CompareOp.EQ || op == CompareOp.LE || op == CompareOp.GE) {

	    			return TrueBOp.INSTANCE;

	    		} else {

	    			return FalseBOp.INSTANCE;

	    		}

	    	}

	        return new CompareBOp(left, right, op);

		}

	}

	public static class SameTermFactory implements Factory {

	    public static final SameTermFactory INSTANCE = new SameTermFactory();
	    
		public IValueExpression<? extends IV> create(final BOpContextBase context,
				final GlobalAnnotations globals, Map<String, Object> scalarValues, final ValueExpressionNode... args) {

			checkArgs(args,
					ValueExpressionNode.class, ValueExpressionNode.class);

			final IValueExpression<? extends IV> left =
				AST2BOpUtility.toVE(context,globals, args[0]);
			
			final IValueExpression<? extends IV> right =
				AST2BOpUtility.toVE(context,globals, args[1]);

	        return new SameTermBOp(left, right, CompareOp.EQ);

		}

	}

	public static class MathFactory implements Factory {

		private final MathOp op;

		public MathFactory(final MathOp op) {
			this.op = op;
		}

		public IValueExpression<? extends IV> create(final BOpContextBase context,
				final GlobalAnnotations globals, Map<String, Object> scalarValues, final ValueExpressionNode... args) {

			checkArgs(args,
					ValueExpressionNode.class, ValueExpressionNode.class);

			final IValueExpression<? extends IV> left =
				AST2BOpUtility.toVE(context,globals, args[0]);
			final IValueExpression<? extends IV> right =
				AST2BOpUtility.toVE(context,globals, args[1]);

			return new MathBOp(left, right, op,globals);

		}

	}

	public static class NumericFactory implements Factory {

        private final NumericOp op;

        public NumericFactory(final NumericOp op) {
            this.op = op;
        }

        public IValueExpression<? extends IV> create(final BOpContextBase context,
                final GlobalAnnotations globals, Map<String, Object> scalarValues, final ValueExpressionNode... args) {

            checkArgs(args,
                    ValueExpressionNode.class);

            final IValueExpression<? extends IV> left =
                AST2BOpUtility.toVE(context,globals, args[0]);

            return new NumericBOp(left,  op);

        }

    }
	public static class DigestFactory implements Factory {

        private final DigestOp op;

        public DigestFactory(final DigestOp op) {
            this.op = op;
        }

        public IValueExpression<? extends IV> create(final BOpContextBase context,
                final GlobalAnnotations globals, Map<String, Object> scalarValues, final ValueExpressionNode... args) {

            checkArgs(args,
                    ValueExpressionNode.class);

            final IValueExpression<? extends IV> left =
                AST2BOpUtility.toVE(context,globals, args[0]);

            return new DigestBOp(left, op, globals);

        }

    }

	public static class DateFactory implements Factory {

        private final DateOp op;

        public DateFactory(final DateOp op) {
            this.op = op;
        }

        public IValueExpression<? extends IV> create(final BOpContextBase context,
                final GlobalAnnotations globals, Map<String, Object> scalarValues, final ValueExpressionNode... args) {

            checkArgs(args,
                    ValueExpressionNode.class);

            final IValueExpression<? extends IV> left =
                AST2BOpUtility.toVE(context,globals, args[0]);

            return new DateBOp(left, op, globals);

        }

    }

	public static class CastFactory implements Factory {

		private final String uri;

		public CastFactory(final String uri) {
			this.uri = uri;
		}

		public IValueExpression<? extends IV> create(final BOpContextBase context,
				final GlobalAnnotations globals, Map<String, Object> scalarValues, final ValueExpressionNode... args) {

			final IValueExpression<? extends IV>[] bops =
				new IValueExpression[args.length];

			for (int i = 0; i < args.length; i++) {
				bops[i] = AST2BOpUtility.toVE(context,globals, args[i]);
			}

			return new FuncBOp(bops, uri, globals);

		}

	}

	public static class GroupConcatFactory implements AggregateFactory {
	   
	    public interface Annotations extends GROUP_CONCAT.Annotations{
	    }

	    public GroupConcatFactory() {
        }

	    public IValueExpression<? extends IV> create(final BOpContextBase context, final GlobalAnnotations globals,
	            final Map<String,Object> scalarValues,
                final ValueExpressionNode... args) {

            checkArgs(args, ValueExpressionNode.class);
            
            final LinkedHashMap<String, Object> tmp = new LinkedHashMap<String, Object>(
                    scalarValues);

            if (!scalarValues.containsKey(Annotations.SEPARATOR)) {
                
                // Per the spec.
                tmp.put(Annotations.SEPARATOR, " ");

            }

            tmp.put(Annotations.NAMESPACE, globals.lex);
            
            final IValueExpression ve = AST2BOpUtility.toVE(context,globals, args[0]);

            return new com.bigdata.bop.rdf.aggregate.GROUP_CONCAT(
                    new BOp[] { ve }, tmp);

        }
	    
	}

    /**
     * <code>NumericExpression IN ArgList</code> is an infix operator. The left
     * argument in the syntax must be provided as the first argument to this
     * factory. The ArgList must be provided as the [1...nargs] arguments to the
     * factory.
     * <p>
     * This optimizes IN/0 (foo IN()) as FALSE.
     * <p>
     * This optimizes IN/1 (foo IN(valueExpr) as SameTerm.
     * <p>
     * This optimizes expr IN Constants using a hash set over the constants.
     */
    public static class InFactory implements Factory {

    	public static interface Annotations {
    		
    		/**
    		 * Literals are not allowed in an IN clause in SPARQL, but sometimes
    		 * this operator is used as part of an optimized re-write where
    		 * literals should be allowed.
    		 * 
    		 * @see ASTSearchInSearchOptimizer
    		 */
    		String ALLOW_LITERALS = Annotations.class.getName() + ".allowLiterals";
    		
    	}

        private final boolean not;

        /**
         * 
         * @param not
         *            <code>true</code> iff this is NOT IN.
         */
        public InFactory(final boolean not) {

            this.not = not;

        }

        public IValueExpression<? extends IV> create(final BOpContextBase context, final GlobalAnnotations globals,
                final Map<String, Object> scalarValues,
                final ValueExpressionNode... args) {

            if (args.length == 0) {
                /*
                 * The first argument is the left hand side of the infix IN
                 * operator.
                 */
                throw new IllegalArgumentException();
            }
            
            if (args.length == 1) {

                /*
                 * "foo IN()" is always false. 
                 * 
                 * "foo NOT IN()" is always true.
                 */
                return not ? TrueBOp.INSTANCE : FalseBOp.INSTANCE;

            }

            final boolean allowLiterals;
            if (scalarValues != null && scalarValues.containsKey(Annotations.ALLOW_LITERALS)) {
            	
            	allowLiterals = (Boolean) scalarValues.get(Annotations.ALLOW_LITERALS);
            	
            } else {
            	
            	allowLiterals = false;
            	
            }
            
            if (args.length == 2) {

                /*
                 * "foo IN(bar)" is SameTerm(foo,bar) if bar is a IRI, otherwise CompareBOp.
                 */

//            	final IValueExpression<? extends IV> val = AST2BOpUtility.toVE(context,globals, args[1]);
            	
//                final IValueExpression ret = SameTermFactory.INSTANCE.create(
//                        globals, scalarValues, args);
            	
                /*
                 * MP: Changed this to check for allowLiterals.  When we are allowing
                 * literals, chances are we want to bypass the CompareOp logic
                 * for literal comparison.
                 */
            	if (allowLiterals) {
            		
                	final IValueExpression ret = SameTermFactory.INSTANCE.create(context,
                            globals, scalarValues, args);

                      if (not)
                          return new NotBOp(ret);

                      return ret;
            		
            	}
                
            	// compare factory is smart enough to optimize for SameTerm when necessary
            	final IValueExpression ret = new CompareFactory(CompareOp.EQ).create(context,
                      globals, scalarValues, args);

                if (not)
                    return new NotBOp(ret);

                return ret;

            }

            try {

                /*
                 * First, attempt to use an optimized variant. The args MUST be
                 * [var,constant(s)].
                 */

                checkArgs(args, ValueExpressionNode.class, ConstantNode.class);

                final IValueExpression<? extends IV> arg = AST2BOpUtility.toVE(context,globals, args[0]);

                final IConstant<? extends IV> set[] = new IConstant[args.length - 1];

                try {
                
                	for (int i = 1; i < args.length; i++) {

                		set[i - 1] = ((ConstantNode) args[i]).getValueExpression();
                		
                		final IV iv = set[i - 1].get();
                		
                		if (!allowLiterals && iv.isLiteral()) {
                			
                			throw new IllegalArgumentException("must use CompareBOps for literals");
                			
                		}

                	}
                	
                	return new InHashBOp(not, arg, set);
                	
                } catch (IllegalArgumentException ex) {
                	
                	IValueExpression ret = null;
                	
                	final CompareFactory compare = new CompareFactory(CompareOp.EQ);
                	
                	for (int i = 1; i < args.length; i++) {
                		
                		if (ret == null) {
                			
                            ret = compare.create(context, globals, scalarValues, args[0],
                                    args[i]);

                        } else {

                            ret = new OrBOp(ret, compare.create(context, globals,
                                    scalarValues, args[0], args[i]));

                		}
                		
                	}
                	
                    if (not)
                        return new NotBOp(ret);

                    return ret;
                    
                }

            } catch (IllegalArgumentException iae) {

                /*
                 * Use a variant which handles value expressions for the members
                 * of the set. The first member of the list is taken to be the
                 * valueExpr which is then tested against each other member of
                 * the list.
                 */

                checkArgs(args, ValueExpressionNode.class,
                        ValueExpressionNode.class);

                final IValueExpression<? extends IV> set[] = new IValueExpression[args.length];

                for (int i = 0; i < args.length; i++) {

                    set[i] = AST2BOpUtility.toVE(context,globals, args[i]);

                }

                return new ComputedIN(not, set);

            }

        }

    }

    /**
     * Factory for EXISTS() and NOT EXISTS(). The EXISTS() node in the AST must
     * be marked with the group graph pattern as an annotation. The
     * {@link FunctionNode} will be a simple test of an anonymous variable.
     * EXISTS tests the variable for <code>true</code>. Not exists tests the
     * variable for <code>false</code>. The name of that anonymous variable is
     * the sole {@link ValueExpressionNode} passed into this factory.
     * <p>
     * The "guts" of the EXISTS logic is translated by an {@link IASTOptimizer}
     * into an ASK subquery (an instance of {@link SubqueryRoot}). This will be
     * executed as a special BOp that bind the success or failure of that ASK
     * subquery on the anonymous variable. The "ASK" subquery does not fail, it
     * just binds the truth state of the ASK subquery on the anonymous variable.
     * The {@link IValueExpression} returned by this factory just tests the
     * truth state of that anonymous variable.
     */
    public static class ExistsFactory implements Factory {

        final private boolean exists;

        public ExistsFactory(boolean exists) {

            this.exists = exists;

        }

        @Override
        public IValueExpression<? extends IV> create(
                final BOpContextBase context,
        		final GlobalAnnotations globals,
                final Map<String, Object> scalarValues, 
                final ValueExpressionNode... args) {

            if (args.length != 1)
                throw new IllegalArgumentException();

            final VarNode anonvar = (VarNode) args[0];

            return exists ? anonvar.getValueExpression() : new NotBOp(
                    anonvar.getValueExpression());

        }

    }
    
    private static class UnknownFunctionFactory implements Factory {
    	
    	private IRI functionURI;
    	
    	public UnknownFunctionFactory(final IRI functionURI) {
    		
    		this.functionURI = functionURI;
    		
    	}
    	
        @Override
        public IValueExpression<? extends IV> create(
                final BOpContextBase context,
                final GlobalAnnotations globals,
                final Map<String, Object> scalarValues, 
                final ValueExpressionNode... args) {

            return new UnknownFunctionBOp(functionURI);

        }
    	
    }
    
	public static class UnknownFunctionBOp 
			extends ImmutableBOp implements IValueExpression<IV> {

		private static final long serialVersionUID = 1L;
		
		private static final String FUNCTION_URI = "FUNCTION_URI";

		public UnknownFunctionBOp(final IRI functionURI) {
			
			this(BOp.NOARGS, NV.asMap(FUNCTION_URI, functionURI));
			
		}
		
		/**
		 * Constructor required for {@link com.bigdata.bop.BOpUtility#deepCopy(FilterNode)}.
		 * 
		 * @param op
		 */
		public UnknownFunctionBOp(final UnknownFunctionBOp op) {

			super(op);

		}

		/**
		 * Required shallow copy constructor.
		 * 
		 * @param args
		 *            The operands.
		 * @param op
		 *            The operation.
		 */
		public UnknownFunctionBOp(final BOp[] args, Map<String, Object> anns) {

			super(args, anns);

		}

		public IV get(final IBindingSet bindingSet) {
			
			throw new UnsupportedOperationException(
					"unknown function: " + getRequiredProperty(FUNCTION_URI));
			
		}
		
	}

}
