package net.covers1624.quack.logging.log4j2;

import net.covers1624.quack.annotation.Requires;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by covers1624 on 7/12/20.
 */
@Requires ("org.apache.logging.log4j:log4j-api")
public class Log4jUtils {

    public static void redirectStreams() {
        Logger stdout = LogManager.getLogger("STDOUT");
        Logger stderr = LogManager.getLogger("STDERR");
        System.setOut(new TracingPrintStream(stdout));
        System.setErr(new TracingPrintStream(stderr));
    }

}
