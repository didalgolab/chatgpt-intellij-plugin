package com.didalgo.intellij.chatgpt.chat;

import java.util.function.Supplier;

public interface ConfigurationPage {

    String getModelPage();

    String getModelName();

    Supplier<String> getSystemPrompt();

    boolean isEnableStreamResponse();

    default ConfigurationPage withSystemPrompt(Supplier<String> systemPrompt) {
        return new ConfigurationPageProxy(this) {
            @Override
            public Supplier<String> getSystemPrompt() {
                return systemPrompt;
            }
        };
    }
}
