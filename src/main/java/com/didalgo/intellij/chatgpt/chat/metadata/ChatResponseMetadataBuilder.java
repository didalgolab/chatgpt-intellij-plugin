/*
 * Copyright (c) 2024 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.chat.metadata;

import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.EmptyRateLimit;
import org.springframework.ai.chat.metadata.PromptMetadata;
import org.springframework.ai.chat.metadata.RateLimit;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class ChatResponseMetadataBuilder extends ConcurrentHashMap<String, Object>
        implements Consumer<ChatResponseMetadata> {

    private final Map<String, Object> properties = new ConcurrentHashMap<>();
    private final UsageAggregator usage = new UsageAggregator();
    private PromptMetadata promptMetadata = PromptMetadata.empty();
    private RateLimit rateLimit = new EmptyRateLimit();

    @Override
    public void accept(ChatResponseMetadata metadata) {
        properties.putAll(metadata);
        usage.accept(metadata.getUsage());

        if (metadata.getPromptMetadata().iterator().hasNext()) {
            promptMetadata = metadata.getPromptMetadata();
        }
        if (!(metadata.getRateLimit() instanceof EmptyRateLimit)) {
            rateLimit = metadata.getRateLimit();
        }
    }

    public ChatResponseMetadata build() {
        return new ImmutableChatResponseMetadata(properties, usage, promptMetadata, rateLimit);
    }
}
