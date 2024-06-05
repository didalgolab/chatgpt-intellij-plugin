/*
 * Copyright (c) 2024 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.chat.metadata;

import lombok.Setter;
import org.jetbrains.annotations.Nullable;
import org.springframework.ai.chat.metadata.Usage;

import java.util.function.Consumer;

/**
 * Aggregates {@link Usage} instances into a single {@link Usage} object.
 */
@Setter
public class UsageAggregator implements Usage, Consumer<Usage> {

    /**
     *  The number of prompt tokens.
     */
    private long promptTokens;

    /**
     *  The number of generation tokens.
     */
    private long generationTokens;

    /**
     * Creates a new {@code UsageAggregator} with zero prompt and generation tokens.
     */
    public UsageAggregator() {
        this(0L, 0L);
    }

    /**
     * Creates a new {@code UsageAggregator} with the given prompt and generation tokens.
     *
     * @param promptTokens        the number of prompt tokens
     * @param generationTokens    the number of generation tokens
     */
    public UsageAggregator(Long promptTokens, Long generationTokens) {
        this.promptTokens = promptTokens;
        this.generationTokens = generationTokens;
    }

    @Override
    public Long getPromptTokens() {
        return promptTokens;
    }

    @Override
    public Long getGenerationTokens() {
        return generationTokens;
    }

    @Override
    public void accept(@Nullable Usage source) {
        if (source != null) {
            setPromptTokens(getMaxOrDefault(getPromptTokens(), source.getPromptTokens()));
            setGenerationTokens(getMaxOrDefault(getGenerationTokens(), source.getGenerationTokens()));
        }
    }

    private static long getMaxOrDefault(long first, @Nullable Long second) {
        return second != null ? Math.max(first, second) : first;
    }

    /**
     * Creates an immutable usage instance (snapshot) from this object.
     *
     * @return the immutable copy
     */
    public ImmutableUsage toImmutableUsage() {
        return new ImmutableUsage(promptTokens, generationTokens);
    }
}