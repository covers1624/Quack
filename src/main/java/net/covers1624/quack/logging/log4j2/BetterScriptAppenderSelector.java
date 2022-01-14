/*
 * This file is part of Quack and is Licensed under the MIT License.
 */
package net.covers1624.quack.logging.log4j2;

import net.covers1624.quack.annotation.Requires;
import org.apache.logging.log4j.core.*;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.appender.AppenderSet;
import org.apache.logging.log4j.core.appender.ScriptAppenderSelector;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.plugins.*;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;
import org.apache.logging.log4j.core.script.AbstractScript;
import org.apache.logging.log4j.core.script.ScriptManager;
import org.apache.logging.log4j.core.script.ScriptRef;

import javax.script.Bindings;
import java.io.Serializable;
import java.util.Objects;

/**
 * Copy of {@link ScriptAppenderSelector} except doesn't explode when given a {@link ScriptRef}
 * <p>
 * Created by covers1624 on 15/1/21.
 */
@Requires ("org.apache.logging.log4j:log4j-core")
@Plugin (name = "BetterScriptAppenderSelector", category = Core.CATEGORY_NAME, elementType = Appender.ELEMENT_TYPE, printObject = true)
public class BetterScriptAppenderSelector extends AbstractAppender {

    /**
     * Builds an appender.
     */
    public static final class Builder implements org.apache.logging.log4j.core.util.Builder<Appender> {

        @PluginElement ("AppenderSet")
        @Required
        private AppenderSet appenderSet;

        @PluginConfiguration
        @Required
        private Configuration configuration;

        @PluginBuilderAttribute
        @Required
        private String name;

        @PluginElement ("Script")
        @Required
        private AbstractScript script;

        @Override
        public Appender build() {
            if (name == null) {
                LOGGER.error("Name missing.");
                return null;
            }
            if (script == null) {
                LOGGER.error("Script missing for ScriptAppenderSelector appender {}", name);
                return null;
            }
            if (appenderSet == null) {
                LOGGER.error("AppenderSet missing for ScriptAppenderSelector appender {}", name);
                return null;
            }
            if (configuration == null) {
                LOGGER.error("Configuration missing for ScriptAppenderSelector appender {}", name);
                return null;
            }
            final ScriptManager scriptManager = configuration.getScriptManager();
            if (!(script instanceof ScriptRef)) {
                scriptManager.addScript(script);
            }
            final Bindings bindings = scriptManager.createBindings(script);
            final Object object = scriptManager.execute(script.getName(), bindings);
            final String appenderName = Objects.toString(object, null);
            final Appender appender = appenderSet.createAppender(appenderName, name);
            return appender;
        }

        public AppenderSet getAppenderSet() {
            return appenderSet;
        }

        public Configuration getConfiguration() {
            return configuration;
        }

        public String getName() {
            return name;
        }

        public AbstractScript getScript() {
            return script;
        }

        public Builder withAppenderNodeSet(@SuppressWarnings ("hiding") final AppenderSet appenderSet) {
            this.appenderSet = appenderSet;
            return this;
        }

        public Builder withConfiguration(@SuppressWarnings ("hiding") final Configuration configuration) {
            this.configuration = configuration;
            return this;
        }

        public Builder withName(@SuppressWarnings ("hiding") final String name) {
            this.name = name;
            return this;
        }

        public Builder withScript(@SuppressWarnings ("hiding") final AbstractScript script) {
            this.script = script;
            return this;
        }

    }

    @PluginBuilderFactory
    public static Builder newBuilder() {
        return new Builder();
    }

    private BetterScriptAppenderSelector(final String name, final Filter filter, final Layout<? extends Serializable> layout) {
        super(name, filter, layout);
    }

    @Override
    public void append(final LogEvent event) {
        // Do nothing: This appender is only used to discover and build another appender
    }
}
