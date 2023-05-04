package com.didalgo.intellij.chatgpt.chat;

import java.util.function.Supplier;

class ChatLinkStateConfigurationProxy implements ChatLinkStateConfiguration {
    private final ChatLinkStateConfiguration delegate;

    ChatLinkStateConfigurationProxy(ChatLinkStateConfiguration delegate) {
        this.delegate = delegate;
    }

    public final ChatLinkStateConfiguration getDelegate() {
        return delegate;
    }

    @Override
    public String getGroup() {
        return getDelegate().getGroup();
    }

    @Override
    public String getModelName() {
        return getDelegate().getModelName();
    }

    @Override
    public Supplier<String> getSystemPrompt() {
        return getDelegate().getSystemPrompt();
    }
}
