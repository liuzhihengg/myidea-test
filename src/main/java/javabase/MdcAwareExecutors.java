package javabase;

import org.slf4j.MDC;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MdcAwareExecutors {

    public static ExecutorService wrap(ExecutorService delegate) {
        return new ExecutorServiceWrapper(delegate);
    }

    private static class ExecutorServiceWrapper implements ExecutorService {
        private final ExecutorService delegate;

        ExecutorServiceWrapper(ExecutorService delegate) {
            this.delegate = delegate;
        }

        private Runnable wrapRunnable(Runnable task) {
            Map<String, String> contextMap = MDC.getCopyOfContextMap();
            return () -> {
                if (contextMap != null) MDC.setContextMap(contextMap);
                else MDC.clear();
                try {
                    task.run();
                } finally {
                    MDC.clear();
                }
            };
        }

        private <T> Callable<T> wrapCallable(Callable<T> task) {
            Map<String, String> contextMap = MDC.getCopyOfContextMap();
            return () -> {
                if (contextMap != null) MDC.setContextMap(contextMap);
                else MDC.clear();
                try {
                    return task.call();
                } finally {
                    MDC.clear();
                }
            };
        }

        // core delegate with wrapping
        @Override public void execute(Runnable command) { delegate.execute(wrapRunnable(command)); }
        @Override public Future<?> submit(Runnable task) { return delegate.submit(wrapRunnable(task)); }
        @Override public <T> Future<T> submit(Callable<T> task) { return delegate.submit(wrapCallable(task)); }
        @Override public <T> Future<T> submit(Runnable task, T result) { return delegate.submit(wrapRunnable(task), result); }

        // passthrough others
        @Override public void shutdown() { delegate.shutdown(); }
        @Override public List<Runnable> shutdownNow() { return delegate.shutdownNow(); }
        @Override public boolean isShutdown() { return delegate.isShutdown(); }
        @Override public boolean isTerminated() { return delegate.isTerminated(); }
        @Override public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
            return delegate.awaitTermination(timeout, unit);
        }

        @Override public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
            List<Callable<T>> wrapped = new ArrayList<>();
            for (Callable<T> t : tasks) wrapped.add(wrapCallable(t));
            return delegate.invokeAll(wrapped);
        }

        @Override public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
            List<Callable<T>> wrapped = new ArrayList<>();
            for (Callable<T> t : tasks) wrapped.add(wrapCallable(t));
            return delegate.invokeAll(wrapped, timeout, unit);
        }

        @Override public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
            List<Callable<T>> wrapped = new ArrayList<>();
            for (Callable<T> t : tasks) wrapped.add(wrapCallable(t));
            return delegate.invokeAny(wrapped);
        }

        @Override public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
                throws InterruptedException, ExecutionException, TimeoutException {
            List<Callable<T>> wrapped = new ArrayList<>();
            for (Callable<T> t : tasks) wrapped.add(wrapCallable(t));
            return delegate.invokeAny(wrapped, timeout, unit);
        }
    }

}
