/*
 * MIT License
 *
 * Copyright (c) 2018-2021 covers1624
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
