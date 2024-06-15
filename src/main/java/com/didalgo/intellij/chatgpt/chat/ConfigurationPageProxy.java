/*
 * Copyright (c) 2024 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.chat;

import com.didalgo.intellij.chatgpt.chat.models.ModelType;

import java.util.function.Supplier;

class ConfigurationPageProxy implements AssistantConfiguration {
    private final AssistantConfiguration delegate;

    ConfigurationPageProxy(AssistantConfiguration delegate) {
        this.delegate = delegate;
    }

    public final AssistantConfiguration getDelegate() {
        return delegate;
    }

    @Override
    public AssistantType getAssistantType() {
        return getDelegate().getAssistantType();
    }

    @Override
    public String getModelName() {
        return getDelegate().getModelName();
    }

    @Override
    public ModelType getModelType() {
        return getDelegate().getModelType();
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
