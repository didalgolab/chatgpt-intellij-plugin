/*
 * Copyright (c) 2024 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.chat.metadata;

import org.jetbrains.annotations.NotNull;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.PromptMetadata;
import org.springframework.ai.chat.metadata.RateLimit;
import org.springframework.ai.chat.metadata.Usage;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;

public final class ImmutableChatResponseMetadata extends AbstractMap<String, Object> implements ChatResponseMetadata {

    private final Map<String, Object> properties;
    private final Usage usage;
    private final PromptMetadata promptMetadata;
    private final RateLimit rateLimit;

    public ImmutableChatResponseMetadata(Map<String, Object> properties, Usage usage, PromptMetadata promptMetadata, RateLimit rateLimit) {
        this.properties = properties;
        this.usage = usage;
        this.promptMetadata = promptMetadata;
        this.rateLimit = rateLimit;
    }

    @NotNull
    @Override
    public Set<Entry<String, Object>> entrySet() {
        return properties.entrySet();
    }

    @Override
    public Object get(Object key) {
        return properties.get(key);
    }

    @Override
    public RateLimit getRateLimit() {
        return rateLimit;
    }

    @Override
    public Usage getUsage() {
        return usage;
    }

    @Override
    public PromptMetadata getPromptMetadata() {
        return promptMetadata;
    }
}
