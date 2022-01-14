/*
 * This file is part of Quack and is Licensed under the MIT License.
 */
package net.covers1624.quack.util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by covers1624 on 31/5/21.
 */
public class JavaPathUtils {

    // Only windows has a different suffix for this use case, Mac, Linux, and FreeBSD don't.
    private static final String EXE_SUFFIX = System.getProperty("os.name").contains("windows") ? ".exe" : "";
    private static final Path JAVA_HOME = calcJavaHome();

    /**
     * Gets the home directory for the currently running Java installation.
     *
     * @return The Path.
     */
    public static Path getJavaHome() {
        return JAVA_HOME;
    }

    /**
     * Gets an executable from the currently running java installation.
     *
     * @param executableName The executable name.
     * @return The path to the executable.
     */
    public static Path getExecutable(String executableName) {
        return getJavaHome().resolve("bin/" + executableName + EXE_SUFFIX);
    }

    /**
     * Gets the 'java' executable from the currently running java installation.
     *
     * @return The path to the executable.
     */
    public static Path getJavaExecutable() {
        return getExecutable("java");
    }

    /**
     * Gets the 'jarsigner' executable from the currently running java installation.
     *
     * @return The path to the executable.
     */
    public static Path getJarSignerExecutable() {
        return getExecutable("jarsigner");
    }

    private static Path calcJavaHome() {
        Path home = Paths.get(System.getProperty("java.home")).toAbsolutePath().normalize();
        // If our jre is the embedded jre for a jdk, use the jdk path as our home, means javac and other dev tools exist.
        if (home.getFileName().toString().equalsIgnoreCase("jre") && Files.exists(home.getParent().resolve("bin/java" + EXE_SUFFIX))) {
            return home.getParent();
        }
        return home;
    }

}
