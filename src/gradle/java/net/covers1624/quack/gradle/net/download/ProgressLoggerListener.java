/*
 * This file is part of Quack and is Licensed under the MIT License.
 */
package net.covers1624.quack.gradle.net.download;

import net.covers1624.quack.annotation.Requires;
import net.covers1624.quack.net.DownloadAction;
import net.covers1624.quack.net.download.DownloadListener;
import net.covers1624.quack.net.download.DownloadSpec;
import net.covers1624.quack.util.DataUtils;
import org.gradle.api.Project;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.internal.logging.progress.ProgressLogger;
import org.gradle.internal.logging.progress.ProgressLoggerFactory;
import org.gradle.internal.service.ServiceRegistry;
import org.jetbrains.annotations.ApiStatus.ScheduledForRemoval;
import org.jetbrains.annotations.Nullable;

/**
 * A {@link DownloadListener} that pipes through to a gradle {@link ProgressLogger}.
 * <p>
 * Created by covers1624 on 23/1/21.
 */
@Requires ("org.gradle:gradle-api")
@Requires ("org.apache.commons:commons-lang3")
@Requires ("org.apache.logging.log4j:log4j-api")
@Requires ("org.apache.httpcomponents:httpclient")
public class ProgressLoggerListener implements DownloadListener {

    private final ProgressLogger logger;

    @Nullable
    private String humanSize;

    public ProgressLoggerListener(ProgressLogger logger) {
        this.logger = logger;
    }

    @Override
    public void connecting() { }

    @Override
    public void start(long expectedLen) {
        if (expectedLen >= 0) {
            humanSize = DataUtils.humanSize(expectedLen);
        }
        logger.started();
    }

    @Override
    public void update(long processedBytes) {
        logger.progress(DataUtils.humanSize(processedBytes) + "/" + humanSize + " downloaded");
    }

    @Override
    public void finish(long totalProcessed) {
        logger.completed();
    }

    @Nullable
    @Deprecated
    @ScheduledForRemoval (inVersion = "0.5.0")
    public static ProgressLoggerListener create(Project project, DownloadSpec spec) {
        try {
            ServiceRegistry registry = ((ProjectInternal) project).getServices();
            ProgressLoggerFactory factory = registry.get(ProgressLoggerFactory.class);
            return new ProgressLoggerListener(factory.newOperation(spec.getClass()).setDescription("Download " + spec.getSrc()));
        } catch (Throwable ignored) {
            return null;
        }
    }

    @Nullable
    public static ProgressLoggerListener create(Project project, DownloadAction action) {
        try {
            ServiceRegistry registry = ((ProjectInternal) project).getServices();
            ProgressLoggerFactory factory = registry.get(ProgressLoggerFactory.class);
            return new ProgressLoggerListener(factory.newOperation(action.getClass()).setDescription("Download " + action.getUrl()));
        } catch (Throwable ignored) {
            return null;
        }
    }
}
