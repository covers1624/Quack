/*
 * This file is part of Quack and is Licensed under the MIT License.
 */
package net.covers1624.quack.gradle.net.download;

import net.covers1624.quack.annotation.ReplaceWith;
import net.covers1624.quack.annotation.Requires;
import net.covers1624.quack.net.download.DownloadAction;
import net.covers1624.quack.net.download.DownloadListener;
import net.covers1624.quack.net.download.DownloadSpec;
import org.gradle.api.DefaultTask;
import org.gradle.api.internal.tasks.TaskExecutionOutcome;
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.TaskAction;
import org.jetbrains.annotations.ApiStatus.ScheduledForRemoval;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.function.Predicate;

/**
 * Inspired and vaguely based off https://github.com/michel-kraemer/gradle-download-task
 * <pre>
 * Notable differences:
 *  Wayyy simpler implementation.
 *  Lazy evaluation of file and URL inputs.
 *  Single file downloads.
 *  External validation of file for up-to-date checking.
 *  UserAgent spoofing. (Thanks mojang!)
 *  Ability to set the ProgressLogger to use.
 * </pre>
 * <p>
 * This is split into an Action, Spec and Task.
 * <p>
 * The Spec {@link DownloadSpec}, Provides the specification for how things work.
 * <p>
 * The Action {@link DownloadAction}, What actually handles downloading
 * implements {@link DownloadSpec}, Useful for other tasks that need to download
 * something but not necessarily create an entire task to do said download.
 * <p>
 * The Task {@link DownloadTask} for gradle, Task wrapper for {@link DownloadAction},
 * implements {@link DownloadSpec} and hosts the Action as a task.
 * <p>
 * Created by covers1624 on 8/02/19.
 */
@Deprecated
@ReplaceWith ("net.covers1624.quack.gradle.net.download.ApacheHttpClientDownloadTask")
@ScheduledForRemoval (inVersion = "0.5.0")
@Requires ("org.gradle:gradle-api")
@Requires ("org.apache.commons:commons-lang3")
@Requires ("org.apache.logging.log4j:log4j-api")
@Requires ("org.apache.httpcomponents:httpclient")
public class DownloadTask extends DefaultTask implements DownloadSpec {

    private final DownloadAction action;

    public DownloadTask() {
        action = new DownloadAction();
        getOutputs().upToDateWhen(e -> false);//Always run, we set our self to up-to-date after checks.
    }

    @TaskAction
    public void doTask() throws IOException {
        if (getListener() == null && !isQuiet()) {
            setListener(ProgressLoggerListener.create(getProject(), this));
        }
        action.execute();
        //We always execute, but 'spoof' our self as up-to-date after etag &| onlyIfModified checks.
        if (isUpToDate()) {
            getState().setOutcome(TaskExecutionOutcome.UP_TO_DATE);
            setDidWork(false);
        }
    }

    //@formatter:off
    public void fileUpToDateWhen(Spec<File> spec) { fileUpToDateWhen((Predicate<Path>) e -> spec.isSatisfiedBy(e.toFile())); }
    @Override public void fileUpToDateWhen(Predicate<Path> spec) { action.fileUpToDateWhen(spec); }
    @Override public URL getSrc() { return action.getSrc(); }
    @Override public Path getDest() { return action.getDest(); }
    @Override public boolean getOnlyIfModified() { return action.getOnlyIfModified(); }
    @Override public DownloadAction.UseETag getUseETag() { return action.getUseETag(); }
    @Override public Path getETagFile() { return action.getETagFile(); }
    @Override public String getUserAgent() { return action.getUserAgent(); }
    @Override public boolean isQuiet() { return action.isQuiet(); }
    @Override public boolean isUpToDate() { return action.isUpToDate(); }
    @Override public DownloadListener getListener() { return action.getListener(); }
    @Override public void setSrc(Object src) { action.setSrc(src); }
    @Override public void setDest(Path dest) { action.setDest(dest); }
    @Override public void setDest(File dest) { action.setDest(dest); }
    @Override public void setOnlyIfModified(boolean onlyIfModified) { action.setOnlyIfModified(onlyIfModified); }
    @Override public void setUseETag(Object useETag) { action.setUseETag(useETag); }
    @Override public void setETagFile(Path eTagFile) { action.setETagFile(eTagFile); }
    @Override public void setETagFile(File eTagFile) { action.setETagFile(eTagFile); }
    @Override public void setUserAgent(String userAgent) { action.setUserAgent(userAgent); }
    @Override public void setQuiet(boolean quiet) { action.setQuiet(quiet); }
    @Override public void setListener(DownloadListener listener) { action.setListener(listener); }
    //@formatter:on
}
