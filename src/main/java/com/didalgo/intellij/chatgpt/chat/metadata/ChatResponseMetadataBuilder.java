/*
 * Copyright (c) 2024 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.chat.metadata;

import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.EmptyRateLimit;

import java.util.Map;
import java.util.function.Consumer;

public class ChatResponseMetadataBuilder implements Consumer<ChatResponseMetadata> {

    private final ChatResponseMetadata.Builder builder = ChatResponseMetadata.builder();
    private final UsageAggregator usageAggregator = new UsageAggregator();

    @Override
    public void accept(ChatResponseMetadata metadata) {
        for (Map.Entry<String, Object> e : metadata.entrySet()) {
            builder.withKeyValue(e.getKey(), e.getValue());
        }
        if (!metadata.getId().isEmpty()) {
            builder.withId(metadata.getId());
        }
        if (!metadata.getModel().isEmpty()) {
            builder.withModel(metadata.getModel());
        }

        usageAggregator.accept(metadata.getUsage());
        if (metadata.getPromptMetadata().iterator().hasNext()) {
            builder.withPromptMetadata(metadata.getPromptMetadata());
        }
        if (!(metadata.getRateLimit() instanceof EmptyRateLimit)) {
            builder.withRateLimit(metadata.getRateLimit());
        }
    }

    public ChatResponseMetadata build() {
        return builder
                .withUsage(usageAggregator.toImmutableUsage())
                .build();
    }
}