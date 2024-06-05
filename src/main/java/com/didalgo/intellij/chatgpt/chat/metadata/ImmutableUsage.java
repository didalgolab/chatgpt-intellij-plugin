/*
 * Copyright (c) 2024 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.chat.metadata;

import org.springframework.ai.chat.metadata.EmptyUsage;
import org.springframework.ai.chat.metadata.Usage;

/**
 * Immutable implementation of {@link Usage}.
 */
public record ImmutableUsage(Long getPromptTokens, Long getGenerationTokens) implements Usage {

    private static final Usage EMPTY_USAGE = new EmptyUsage();

    public static Usage empty() {
        return EMPTY_USAGE;
    }
}