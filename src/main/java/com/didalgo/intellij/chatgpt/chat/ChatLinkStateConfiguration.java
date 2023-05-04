package com.didalgo.intellij.chatgpt.chat;

import java.util.function.Supplier;

public interface ChatLinkStateConfiguration {

    String getGroup();

    String getModelName();

    Supplier<String> getSystemPrompt();

    default ChatLinkStateConfiguration withSystemPrompt(Supplier<String> systemPrompt) {
        return new ChatLinkStateConfigurationProxy(this) {
            @Override
            public Supplier<String> getSystemPrompt() {
                return systemPrompt;
            }
        };
    }
}
