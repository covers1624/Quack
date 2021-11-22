/*
 * This file is part of Quack and is Licensed under the MIT License.
 *
 * Copyright (c) 2018-2021 covers1624 <https://github.com/covers1624>
 */
package net.covers1624.quack.net;

import net.covers1624.quack.net.apache.ApacheHttpClientDownloadAction;
import net.covers1624.quack.net.download.DownloadListener;
import net.covers1624.quack.net.okhttp.OkHttpDownloadAction;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Reader;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by covers1624 on 22/11/21.
 */
public class TestDownloadAction {

    // This is very well known, and likely not to disappear any time soon.
    // These tests only validate that this file is readable Maven Metadata XML with the 'groupId' attribute.
    private static final String WELL_KNOWN_XML = "https://repo1.maven.org/maven2/com/google/guava/guava/maven-metadata.xml";
    private static final String GROUP_ID = "com.google.guava";
    private static final MetadataXpp3Reader XPP_3_READER = new MetadataXpp3Reader();

    //region OkHttp
    @Test
    public void testStringOkHttp() throws Throwable {
        testDownloadString(OkHttpDownloadAction::new);
    }

    @Test
    public void testOnlyIfModifiedOkHttp() throws Throwable {
        testNotModified(OkHttpDownloadAction::new, false, true);
    }

    @Test
    public void testETagOkHttp() throws Throwable {
        testNotModified(OkHttpDownloadAction::new, true, false);
    }

    @Test
    public void testETagAndOnlyIfModifiedOkHttp() throws Throwable {
        testNotModified(OkHttpDownloadAction::new, true, true);
    }
    //endregion

    //region Apache HttpClient
    @Test
    public void testStringApache() throws Throwable {
        testDownloadString(ApacheHttpClientDownloadAction::new);
    }

    @Test
    public void testOnlyIfModifiedApache() throws Throwable {
        testNotModified(ApacheHttpClientDownloadAction::new, false, true);
    }

    @Test
    public void testETagApache() throws Throwable {
        testNotModified(ApacheHttpClientDownloadAction::new, true, false);
    }

    @Test
    public void testETagAndOnlyIfModifiedApache() throws Throwable {
        testNotModified(ApacheHttpClientDownloadAction::new, true, true);
    }
    //endregion

    private static void testDownloadString(Supplier<DownloadAction> actionFunc) throws Throwable {
        StringWriter sw = new StringWriter();
        DownloadAction action = actionFunc.get()
                .setUrl(WELL_KNOWN_XML)
                .setDest(sw)
                .setQuiet(false)
                .setDownloadListener(new TestDownloadListener());
        action.execute();
        Metadata metadata = XPP_3_READER.read(new StringReader(sw.toString()));
        assertEquals(GROUP_ID, metadata.getGroupId());
    }

    private static void testNotModified(Supplier<DownloadAction> actionFunc, boolean etag, boolean onlyIfModified) throws Throwable {
        Path tempDir = Files.createTempDirectory("download_action");
        tempDir.toFile().deleteOnExit();

        Path file = tempDir.resolve("maven-metadata.xml");
        DownloadAction action = actionFunc.get()
                .setUrl(WELL_KNOWN_XML)
                .setDest(file)
                .setQuiet(false)
                .setUseETag(etag)
                .setOnlyIfModified(onlyIfModified)
                .setDownloadListener(new TestDownloadListener());
        action.execute();
        assertEquals(GROUP_ID, parseMeta(file).getGroupId());
        DownloadAction action2 = actionFunc.get()
                .setUrl(WELL_KNOWN_XML)
                .setDest(file)
                .setQuiet(false)
                .setUseETag(etag)
                .setOnlyIfModified(onlyIfModified)
                .setDownloadListener(new TestDownloadListener());
        action2.execute();
        assertTrue(action2.isUpToDate());
        assertEquals(GROUP_ID, parseMeta(file).getGroupId());
    }

    private static Metadata parseMeta(Path file) throws Throwable {
        try (BufferedReader reader = Files.newBufferedReader(file)) {
            return XPP_3_READER.read(reader);
        }
    }

    private static class TestDownloadListener implements DownloadListener {

        private boolean hasInit;
        private boolean hasFinished;
        private long expectedLen = -1;

        @Override
        public void connecting() {
            assertFalse(hasInit);
            assertFalse(hasFinished);
            assertEquals(-1, expectedLen);
            hasInit = true;
        }

        @Override
        public void start(long expectedLen) {
            assertTrue(hasInit);
            assertFalse(hasFinished);
            assertEquals(-1, this.expectedLen);
            this.expectedLen = expectedLen;
        }

        @Override
        public void update(long processedBytes) {
            assertTrue(hasInit);
            assertFalse(hasFinished);
            assertNotEquals(-1, expectedLen);
            assertTrue(processedBytes <= expectedLen);
        }

        @Override
        public void finish(long totalProcessed) {
            assertTrue(hasInit);
            assertFalse(hasFinished);
            assertEquals(expectedLen, totalProcessed);
            hasFinished = true;
        }
    }
}
