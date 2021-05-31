/*
 * This file is part of Quack and is Licensed under the MIT License.
 *
 * Copyright (c) 2018-2021 covers1624 <https://github.com/covers1624>
 */
package net.covers1624.quack.util;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;

/**
 * Created by covers1624 on 31/5/21.
 */
public class ProcessUtils {

    /**
     * This method is backported from Java 9 for use with Java 8 based tools.
     *
     * The following documentation is copied from the Java 9 implementation.
     *
     * Returns a {@code CompletableFuture<Process>} for the termination of the Process.
     * The {@link java.util.concurrent.CompletableFuture} provides the ability
     * to trigger dependent functions or actions that may be run synchronously
     * or asynchronously upon process termination.
     * When the process has terminated the CompletableFuture is
     * {@link java.util.concurrent.CompletableFuture#complete completed} regardless
     * of the exit status of the process.
     * <p>
     * Calling {@code onExit().get()} waits for the process to terminate and returns
     * the Process. The future can be used to check if the process is
     * {@linkplain java.util.concurrent.CompletableFuture#isDone done} or to
     * {@linkplain java.util.concurrent.CompletableFuture#get() wait} for it to terminate.
     * {@linkplain java.util.concurrent.CompletableFuture#cancel(boolean) Cancelling}
     * the CompletableFuture does not affect the Process.
     * <p>
     * Processes returned from {@link ProcessBuilder#start} override the
     * default implementation to provide an efficient mechanism to wait
     * for process exit.
     *
     * @apiNote
     * Using {@link #onExit onExit} is an alternative to
     * {@link Process#waitFor() waitFor} that enables both additional concurrency
     * and convenient access to the result of the Process.
     * Lambda expressions can be used to evaluate the result of the Process
     * execution.
     * If there is other processing to be done before the value is used
     * then {@linkplain #onExit onExit} is a convenient mechanism to
     * free the current thread and block only if and when the value is needed.
     * <br>
     * For example, launching a process to compare two files and get a boolean if they are identical:
     * <pre> {@code   Process p = new ProcessBuilder("cmp", "f1", "f2").start();
     *    Future<Boolean> identical = p.onExit().thenApply(p1 -> p1.exitValue() == 0);
     *    ...
     *    if (identical.get()) { ... }
     * }</pre>
     *
     * @implSpec
     * This implementation executes {@link Process#waitFor()} in a separate thread
     * repeatedly until it returns successfully. If the execution of
     * {@code waitFor} is interrupted, the thread's interrupt status is preserved.
     * <p>
     * When {@link Process#waitFor()} returns successfully the CompletableFuture is
     * {@linkplain java.util.concurrent.CompletableFuture#complete completed} regardless
     * of the exit status of the process.
     *
     * This implementation may consume a lot of memory for thread stacks if a
     * large number of processes are waited for concurrently.
     * <p>
     * External implementations should override this method and provide
     * a more efficient implementation. For example, to delegate to the underlying
     * process, it can do the following:
     * <pre>{@code
     *    public CompletableFuture<Process> onExit() {
     *       return delegate.onExit().thenApply(p -> this);
     *    }
     * }</pre>
     * @apiNote
     * The process may be observed to have terminated with {@link Process#isAlive}
     * before the ComputableFuture is completed and dependent actions are invoked.
     *
     * @return a new {@code CompletableFuture<Process>} for the Process
     *
     * @since 9
     */
    public static CompletableFuture<Process> onExit(Process process) {
        return CompletableFuture.supplyAsync(() -> waitFor(process));
    }

    private static Process waitFor(Process process) {
        boolean interrupted = false;
        while (true) {
            try {
                ForkJoinPool.managedBlock(new ForkJoinPool.ManagedBlocker() {
                    @Override
                    public boolean block() throws InterruptedException {
                        process.waitFor();
                        return true;
                    }

                    @Override
                    public boolean isReleasable() {
                        return !process.isAlive();
                    }
                });
                break;
            } catch (InterruptedException ex) {
                interrupted = true;
            }
        }
        if (interrupted) {
            Thread.currentThread().interrupt();
        }
        return process;
    }
}
