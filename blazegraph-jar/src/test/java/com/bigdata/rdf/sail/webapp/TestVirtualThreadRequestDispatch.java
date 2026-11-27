package com.bigdata.rdf.sail.webapp;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import junit.framework.TestCase;
import org.eclipse.jetty.ee11.servlet.ServletContextHandler;
import org.eclipse.jetty.ee11.servlet.ServletHolder;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.QoSHandler;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.util.thread.Invocable.InvocationType;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

public class TestVirtualThreadRequestDispatch extends TestCase {

    public void testDisabledAndEnabledDispatch() throws Exception {
        assertDispatch(false);
        assertDispatch(true);
    }

    public void testServletDispatch() throws Exception {
        Server server = new Server(0);
        AtomicBoolean virtual = new AtomicBoolean();
        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/");
        context.addServlet(new ServletHolder(new HttpServlet() {
            @Override
            protected void doGet(HttpServletRequest request, HttpServletResponse response) {
                virtual.set(Thread.currentThread().isVirtual());
            }
        }), "/*");
        server.setHandler(context);
        Properties properties = new Properties();
        properties.setProperty(VirtualThreadRequestDispatch.ENABLED, "true");
        VirtualThreadRequestDispatch.configure(server, properties);
        try {
            server.start();
            assertEquals(200, client().send(request(server), HttpResponse.BodyHandlers.discarding()).statusCode());
            assertTrue("Servlet did not run on a virtual thread", virtual.get());
        } finally {
            server.stop();
        }
    }

    private void assertDispatch(boolean enabled) throws Exception {
        Server server = new Server(0);
        AtomicBoolean virtual = new AtomicBoolean();
        Handler handler = new Handler.Abstract(InvocationType.BLOCKING) {
            @Override
            public boolean handle(Request request, Response response, Callback callback) {
                virtual.set(Thread.currentThread().isVirtual());
                callback.succeeded();
                return true;
            }
        };
        server.setHandler(handler);
        Properties properties = new Properties();
        if (enabled)
            properties.setProperty(VirtualThreadRequestDispatch.ENABLED, "true");
        VirtualThreadRequestDispatch.configure(server, properties);
        if (!enabled) {
            assertSame(handler, server.getHandler());
            assertNull(((QueuedThreadPool) server.getThreadPool()).getVirtualThreadsExecutor());
        }
        try {
            server.start();
            assertEquals(200, client().send(request(server), HttpResponse.BodyHandlers.discarding()).statusCode());
            assertEquals(enabled, virtual.get());
        } finally {
            server.stop();
        }
    }

    public void testBoundedQueueAndResume() throws Exception {
        runAdmissionTest(1, 5000, true);
    }

    public void testQueueTimeout() throws Exception {
        runAdmissionTest(1, 100, false);
    }

    public void testImmediateRejection() throws Exception {
        runAdmissionTest(0, 5000, false);
    }

    public void testExecutorStopsWithServer() throws Exception {
        Server server = new Server(0);
        Properties properties = new Properties();
        properties.setProperty(VirtualThreadRequestDispatch.ENABLED, "true");
        VirtualThreadRequestDispatch.configure(server, properties);
        Executor executor = ((QueuedThreadPool) server.getThreadPool()).getVirtualThreadsExecutor();
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch interrupted = new CountDownLatch(1);
        try {
            server.start();
            executor.execute(() -> {
                started.countDown();
                try {
                    new CountDownLatch(1).await();
                } catch (InterruptedException ex) {
                    interrupted.countDown();
                    Thread.currentThread().interrupt();
                }
            });
            assertTrue(started.await(10, TimeUnit.SECONDS));
        } finally {
            server.stop();
        }
        assertTrue("Running task was not interrupted on shutdown", interrupted.await(10, TimeUnit.SECONDS));
        try {
            executor.execute(() -> { });
            fail("Stopped executor accepted a task");
        } catch (RejectedExecutionException expected) {
            // Expected after shutdown.
        }
    }

    private void runAdmissionTest(int maxQueued, int queueTimeout, boolean resume) throws Exception {
        Server server = new Server(0);
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        server.setHandler(new Handler.Abstract(InvocationType.BLOCKING) {
            @Override
            public boolean handle(Request request, Response response, Callback callback) throws Exception {
                entered.countDown();
                if (!release.await(10, TimeUnit.SECONDS))
                    throw new IllegalStateException("Timed out waiting to release request");
                callback.succeeded();
                return true;
            }
        });
        Properties properties = new Properties();
        properties.setProperty(VirtualThreadRequestDispatch.ENABLED, "true");
        properties.setProperty(VirtualThreadRequestDispatch.MAX_REQUESTS, "1");
        properties.setProperty(VirtualThreadRequestDispatch.MAX_QUEUED, Integer.toString(maxQueued));
        properties.setProperty(VirtualThreadRequestDispatch.QUEUE_TIMEOUT, Integer.toString(queueTimeout));
        VirtualThreadRequestDispatch.configure(server, properties);
        QoSHandler limiter = (QoSHandler) server.getHandler();
        try {
            server.start();
            HttpClient client = client();
            CompletableFuture<HttpResponse<Void>> first = client.sendAsync(request(server), HttpResponse.BodyHandlers.discarding());
            assertTrue("First request did not reach handler", entered.await(10, TimeUnit.SECONDS));
            CompletableFuture<HttpResponse<Void>> second = client.sendAsync(request(server), HttpResponse.BodyHandlers.discarding());
            if (resume) {
                long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
                while (limiter.getSuspendedRequestCount() != 1 && System.nanoTime() < deadline)
                    Thread.sleep(10);
                assertEquals(1, limiter.getSuspendedRequestCount());
                assertEquals(503, client.send(request(server), HttpResponse.BodyHandlers.discarding()).statusCode());
                release.countDown();
                assertEquals(200, second.get(10, TimeUnit.SECONDS).statusCode());
            } else {
                assertEquals(503, second.get(10, TimeUnit.SECONDS).statusCode());
                release.countDown();
            }
            assertEquals(200, first.get(10, TimeUnit.SECONDS).statusCode());
        } finally {
            release.countDown();
            server.stop();
        }
    }

    public void testInvalidConfigurationFails() {
        for (String[] invalid : new String[][] {
                { VirtualThreadRequestDispatch.ENABLED, "yes" },
                { VirtualThreadRequestDispatch.MAX_REQUESTS, "0" },
                { VirtualThreadRequestDispatch.MAX_QUEUED, "-1" },
                { VirtualThreadRequestDispatch.QUEUE_TIMEOUT, "0" },
                { VirtualThreadRequestDispatch.QUEUE_TIMEOUT, "invalid" }
        }) {
            Properties properties = new Properties();
            properties.setProperty(VirtualThreadRequestDispatch.ENABLED, "true");
            properties.setProperty(invalid[0], invalid[1]);
            try {
                VirtualThreadRequestDispatch.configure(new Server(0), properties);
                fail("Expected invalid property to fail: " + invalid[0]);
            } catch (IllegalArgumentException ex) {
                assertTrue(ex.getMessage().contains(invalid[0]));
            }
        }
    }

    private static HttpClient client() {
        return HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(5)).build();
    }

    private static HttpRequest request(Server server) {
        int port = ((ServerConnector) server.getConnectors()[0]).getLocalPort();
        return HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/"))
                .timeout(Duration.ofSeconds(15)).GET().build();
    }
}
