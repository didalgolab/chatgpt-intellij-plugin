package com.didalgo.intellij.chatgpt.chat;

import java.util.function.Supplier;

class ConfigurationPageProxy implements ConfigurationPage {
    private final ConfigurationPage delegate;

    ConfigurationPageProxy(ConfigurationPage delegate) {
        this.delegate = delegate;
    }

    public final ConfigurationPage getDelegate() {
        return delegate;
    }

    @Override
    public String getModelPage() {
        return getDelegate().getModelPage();
    }

    @Override
    public String getModelName() {
        return getDelegate().getModelName();
    }

    @Override
    public Supplier<String> getSystemPrompt() {
        return getDelegate().getSystemPrompt();
    }

    @Override
    public boolean isEnableStreamResponse() {
        return getDelegate().isEnableStreamResponse();
    }
}
