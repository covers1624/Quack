/*
 * This file is part of Quack and is Licensed under the MIT License.
 *
 * Copyright (c) 2018-2021 covers1624 <https://github.com/covers1624>
 */
package net.covers1624.quack.gradle.net.download;

import net.covers1624.quack.annotation.Requires;
import net.covers1624.quack.net.download.DownloadAction;
import net.covers1624.quack.net.download.DownloadListener;
import net.covers1624.quack.net.download.DownloadSpec;
import org.gradle.api.Project;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.internal.logging.progress.ProgressLogger;
import org.gradle.internal.logging.progress.ProgressLoggerFactory;
import org.gradle.internal.service.ServiceRegistry;

/**
 * A {@link DownloadListener} that pipes through to a gradle {@link ProgressLogger}
 * <p>
 * Created by covers1624 on 23/1/21.
 */
@Requires ("org.gradle:gradle-api")
@Requires ("org.apache.commons:commons-lang3")
@Requires ("org.apache.logging.log4j:log4j-api")
@Requires ("org.apache.httpcomponents:httpclient")
public class ProgressLoggerListener implements DownloadListener {

    private final ProgressLogger logger;

    private String humanSize;

    public ProgressLoggerListener(ProgressLogger logger) {
        this.logger = logger;
    }

    @Override
    public void connecting() { }

    @Override
    public void start(long expectedLen) {
        if (expectedLen >= 0) {
            humanSize = DownloadAction.toLengthText(expectedLen);
        }
        logger.started();
    }

    @Override
    public void update(long processedBytes) {
        logger.progress(DownloadAction.toLengthText(processedBytes) + "/" + humanSize + " downloaded");
    }

    @Override
    public void finish(long totalProcessed) {
        logger.completed();
    }

    public static ProgressLoggerListener create(Project project, DownloadSpec spec) {
        try {
            ServiceRegistry registry = ((ProjectInternal) project).getServices();
            ProgressLoggerFactory factory = registry.get(ProgressLoggerFactory.class);
            return new ProgressLoggerListener(factory.newOperation(spec.getClass()).setDescription("Download " + spec.getSrc()));
        } catch (Throwable ignored) {
            return null;
        }
    }
}
