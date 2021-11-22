/*
 * This file is part of Quack and is Licensed under the MIT License.
 *
 * Copyright (c) 2018-2021 covers1624 <https://github.com/covers1624>
 */
package net.covers1624.quack.gradle.net.download;

import net.covers1624.quack.annotation.Requires;
import net.covers1624.quack.net.DownloadAction;
import net.covers1624.quack.net.apache.ApacheHttpClientDownloadAction;
import net.covers1624.quack.net.download.DownloadListener;
import org.apache.http.impl.client.CloseableHttpClient;
import org.gradle.api.DefaultTask;
import org.gradle.api.internal.tasks.TaskExecutionOutcome;
import org.gradle.api.tasks.TaskAction;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.file.Path;

import static java.util.Objects.requireNonNull;

/**
 * A Gradle task for a {@link ApacheHttpClientDownloadAction}.
 * <p>
 * Created by covers1624 on 22/11/21.
 */
@Requires ("org.slf4j:slf4j-api")
@Requires ("org.gradle:gradle-api")
@Requires ("org.apache.httpcomponents:httpclient")
public class ApacheHttpClientDownloadTask extends DefaultTask implements DownloadAction {

    private final ApacheHttpClientDownloadAction action = new ApacheHttpClientDownloadAction();

    public ApacheHttpClientDownloadTask() {
        getOutputs().upToDateWhen(e -> false);
    }

    @Override
    @TaskAction
    public void execute() throws IOException {
        if (getDownloadListener() == null && !getQuiet()) {
            setDownloadListener(requireNonNull(ProgressLoggerListener.create(getProject(), this)));
        }
        action.execute();
        if (isUpToDate()) {
            getState().setOutcome(TaskExecutionOutcome.UP_TO_DATE);
            setDidWork(false);
        }
    }

    //@formatter:off
    public ApacheHttpClientDownloadTask setClient(CloseableHttpClient client) { action.setClient(client);return this; }
    @Override public ApacheHttpClientDownloadTask setUrl(String url) { action.setUrl(url);return this; }
    @Override public ApacheHttpClientDownloadTask setDest(Dest dest) { action.setDest(dest);return this; }
    @Override public ApacheHttpClientDownloadTask setDest(StringWriter sw) { action.setDest(sw);return this; }
    @Override public ApacheHttpClientDownloadTask setDest(OutputStream os) { action.setDest(os);return this; }
    @Override public ApacheHttpClientDownloadTask setDest(File file) { action.setDest(file);return this; }
    @Override public ApacheHttpClientDownloadTask setDest(Path path) { action.setDest(path);return this; }
    @Override public ApacheHttpClientDownloadTask setOnlyIfModified(boolean onlyIfModified) { action.setOnlyIfModified(onlyIfModified);return this; }
    @Override public ApacheHttpClientDownloadTask setUseETag(boolean useETag) { action.setUseETag(useETag);return this; }
    @Override public ApacheHttpClientDownloadTask setQuiet(boolean quiet) { action.setQuiet(quiet);return this; }
    @Override public ApacheHttpClientDownloadTask setUserAgent(String userAgent) { action.setUserAgent(userAgent);return this; }
    @Override public ApacheHttpClientDownloadTask setDownloadListener(DownloadListener downloadListener) { action.setDownloadListener(downloadListener);return this; }
    public CloseableHttpClient getClient() { return action.getClient(); }
    @Nullable @Override public String getUrl() { return action.getUrl(); }
    @Nullable @Override public Dest getDest() { return action.getDest(); }
    @Override public boolean getOnlyIfModified() { return action.getOnlyIfModified(); }
    @Override public boolean getUseETag() { return action.getUseETag(); }
    @Override public boolean getQuiet() { return action.getQuiet(); }
    @Nullable @Override public String getUserAgent() { return action.getUserAgent(); }
    @Nullable @Override public DownloadListener getDownloadListener() { return action.getDownloadListener(); }
    @Override public boolean isUpToDate() { return action.isUpToDate(); }
    //@formatter:on
}
