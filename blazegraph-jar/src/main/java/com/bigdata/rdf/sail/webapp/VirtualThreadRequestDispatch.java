package com.bigdata.rdf.sail.webapp;

import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.QoSHandler;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

final class VirtualThreadRequestDispatch {

    static final String ENABLED = "blazegraph.virtualThreads.enabled";
    static final String MAX_REQUESTS = "blazegraph.virtualThreads.maxRequests";
    static final String MAX_QUEUED = "blazegraph.virtualThreads.maxQueuedRequests";
    static final String QUEUE_TIMEOUT = "blazegraph.virtualThreads.queueTimeoutMillis";

    private VirtualThreadRequestDispatch() {
    }

    static void configure(Server server, Properties properties) {
        String enabled = properties.getProperty(ENABLED, "false");
        if ("false".equalsIgnoreCase(enabled))
            return;
        if (!"true".equalsIgnoreCase(enabled))
            throw new IllegalArgumentException(ENABLED + " must be true or false: " + enabled);

        int maxRequests = integerProperty(properties, MAX_REQUESTS, 64, 1);
        int maxQueued = integerProperty(properties, MAX_QUEUED, 64, 0);
        int queueTimeout = integerProperty(properties, QUEUE_TIMEOUT, 30000, 1);
        if (!(server.getThreadPool() instanceof QueuedThreadPool))
            throw new IllegalArgumentException(ENABLED + " requires a Jetty QueuedThreadPool");

        QueuedThreadPool pool = (QueuedThreadPool) server.getThreadPool();
        if (pool.getVirtualThreadsExecutor() != null)
            throw new IllegalStateException("Jetty already has a virtual threads executor");

        VirtualExecutor executor = new VirtualExecutor();
        pool.addBean(executor, true);
        pool.setVirtualThreadsExecutor(executor);

        Handler original = server.getHandler();
        server.setHandler((Handler) null);
        QoSHandler limiter = new QoSHandler(original);
        limiter.setMaxRequestCount(maxRequests);
        limiter.setMaxSuspendedRequestCount(maxQueued);
        limiter.setMaxSuspend(Duration.ofMillis(queueTimeout));
        server.setHandler(limiter);
    }

    private static int integerProperty(Properties properties, String name, int defaultValue, int minimum) {
        String value = properties.getProperty(name, Integer.toString(defaultValue));
        try {
            int parsed = Integer.parseInt(value);
            if (parsed >= minimum)
                return parsed;
        } catch (NumberFormatException ignored) {
            // Report the property name and invalid value below.
        }
        throw new IllegalArgumentException(name + " must be an integer >= " + minimum + ": " + value);
    }

    private static final class VirtualExecutor extends AbstractLifeCycle implements Executor {
        private volatile ExecutorService executor;

        @Override
        protected void doStart() throws Exception {
            executor = Executors.newThreadPerTaskExecutor(
                    Thread.ofVirtual().name("blazegraph-request-", 0).factory());
            super.doStart();
        }

        @Override
        protected void doStop() throws Exception {
            ExecutorService current = executor;
            executor = null;
            if (current != null)
                current.shutdownNow();
            super.doStop();
        }

        @Override
        public void execute(Runnable task) {
            ExecutorService current = executor;
            if (current == null)
                throw new RejectedExecutionException("Virtual request executor is not running");
            current.execute(task);
        }
    }
}
