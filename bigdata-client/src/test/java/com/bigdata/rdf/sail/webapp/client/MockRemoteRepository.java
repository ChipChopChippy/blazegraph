package com.bigdata.rdf.sail.webapp.client;

import java.nio.charset.StandardCharsets;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.net.URLDecoder;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.Request;
import org.eclipse.jetty.client.Response;
import org.eclipse.jetty.client.Result;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.io.Content;
import org.eclipse.jetty.util.Fields;
import org.eclipse.rdf4j.query.resultio.TupleQueryResultFormat;
import org.eclipse.rdf4j.rio.RDFFormat;

public class MockRemoteRepository extends RemoteRepository {

	public final static class Data {
		public ConnectOptions opts;
		public Request request;
		public IPreparedTupleQuery query;
	}
	public Data data;

	private MockRemoteRepository(RemoteRepositoryManager mgr, String sparqlEndpointURL, IRemoteTx tx, Data data) {
		super(mgr, sparqlEndpointURL, tx);
		this.data = data;
	}

	public static MockRemoteRepository create(final String tupleQueryResponse, final String graphQueryResponse) {
		// pojo to retrieve values from mock service
		final Data data = new Data();

		String serviceURL = "http://localhost";
		HttpClient httpClient = new HttpClient() {
			@Override
			public Request newRequest(String uri) {
				final Fields params = queryParams(uri);
				final HttpFields.Mutable requestHeaders = HttpFields.build();
				final Request[] requestRef = new Request[1];
				final InvocationHandler requestHandler = (proxy, method, args) -> {
					switch (method.getName()) {
					case "method":
					case "body":
					case "timeout":
					case "idleTimeout":
					case "onRequestHeaders":
					case "onRequestContent":
					case "onResponseHeader":
					case "onResponseHeaders":
					case "onResponseContent":
					case "onResponseContentAsync":
					case "onResponseContentSource":
						return proxy;
					case "param":
						params.add((String) args[0], (String) args[1]);
						return proxy;
					case "getParams":
						return params;
					case "headers":
						((Consumer<HttpFields.Mutable>) args[0]).accept(requestHeaders);
						return proxy;
					case "getHeaders":
						return requestHeaders.asImmutable();
					case "getMethod":
						return "GET";
					case "abort":
						return CompletableFuture.completedFuture(Boolean.TRUE);
					case "send":
						sendResponse(requestRef[0], requestHeaders, (Response.CompleteListener) args[0],
								tupleQueryResponse, graphQueryResponse);
						return null;
					case "toString":
						return uri;
					case "hashCode":
						return System.identityHashCode(proxy);
					case "equals":
						return proxy == args[0];
					default:
						return null;
					}
				};
				requestRef[0] = (Request) Proxy.newProxyInstance(
						Request.class.getClassLoader(),
						new Class<?>[] { Request.class }, requestHandler);
				data.request = requestRef[0];
				return requestRef[0];
			}

			@Override
			public boolean isStopped() {
				return false;
			}
		};
		Executor executor = Executors.newCachedThreadPool();
		RemoteRepositoryManager mgr = new RemoteRepositoryManager(serviceURL,
				httpClient, executor) {
			@Override
			public JettyResponseListener doConnect(ConnectOptions opts) throws Exception {
				// Store connection options
				data.opts = opts;
				return super.doConnect(opts);
			}
		};

		return new MockRemoteRepository(mgr, serviceURL, null, data);
	}

	private static Fields queryParams(final String uri) {
		final Fields params = new Fields();
		final String query = URI.create(uri).getRawQuery();
		if (query != null) {
			for (String pair : query.split("&")) {
				final int equals = pair.indexOf('=');
				final String name = equals < 0 ? pair : pair.substring(0, equals);
				final String value = equals < 0 ? "" : pair.substring(equals + 1);
				params.add(URLDecoder.decode(name, StandardCharsets.UTF_8),
						URLDecoder.decode(value, StandardCharsets.UTF_8));
			}
		}
		return params;
	}

	private static void sendResponse(final Request request,
			final HttpFields requestHeaders,
			final Response.CompleteListener listener,
			final String tupleQueryResponse, final String graphQueryResponse) {
		final String accept = requestHeaders.get(HttpHeader.ACCEPT);
		final String requestMimeType = accept == null ? null : accept.split(";")[0];
		final TupleQueryResultFormat tupleQueryMimeType =
				org.eclipse.rdf4j.common.lang.FileFormat.matchMIMEType(requestMimeType,
						org.eclipse.rdf4j.query.resultio.TupleQueryResultWriterRegistry
								.getInstance().getKeys())
						.map(format -> (TupleQueryResultFormat) format).orElse(null);
		final String responseMimeType;
		final String responseContent;
		if (tupleQueryMimeType != null) {
			responseMimeType = TupleQueryResultFormat.TSV.getDefaultMIMEType();
			responseContent = tupleQueryResponse;
		} else {
			responseMimeType = RDFFormat.NTRIPLES.getDefaultMIMEType();
			responseContent = graphQueryResponse;
		}

		final HttpFields.Mutable responseHeaders = HttpFields.build();
		responseHeaders.add(HttpHeader.CONTENT_TYPE, responseMimeType);
		final Response response = (Response) Proxy.newProxyInstance(
				Response.class.getClassLoader(), new Class<?>[] { Response.class },
				(proxy, method, args) -> {
					switch (method.getName()) {
					case "getRequest":
						return request;
					case "getStatus":
						return 200;
					case "getReason":
						return "OK";
					case "getHeaders":
						return responseHeaders.asImmutable();
					case "toString":
						return "MockResponse";
					case "hashCode":
						return System.identityHashCode(proxy);
					case "equals":
						return proxy == args[0];
					default:
						return null;
					}
				});
		final JettyResponseListener responseListener =
				(JettyResponseListener) listener;
		responseListener.onHeaders(response);
		final byte[] bytes = responseContent.getBytes(StandardCharsets.UTF_8);
		responseListener.onContent(response,
				Content.Chunk.from(java.nio.ByteBuffer.wrap(bytes), true),
				() -> { });
		responseListener.onSuccess(response);
		responseListener.onComplete(new Result(request, response));
	}

	@Override
	public IPreparedTupleQuery prepareTupleQuery(String query)
			throws Exception {
		// Store IPreparedTupleQuery
		return data.query = super.prepareTupleQuery(query);
	}
	
}
