/*
 * Copyright (c) 2024 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.chat.metadata;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.ai.chat.metadata.Usage;

import static org.junit.jupiter.api.Assertions.*;

class UsageAggregatorTest {

    private final UsageAggregator aggregator = new UsageAggregator();

    @Test
    void is_zero_initially() {
        assertUsageEquals(0L, 0L);
    }

    @ParameterizedTest
    @CsvSource({
            "10, 20, 10, 20",
            "5, 30, 5, 30",
            "25, 15, 25, 15"
    })
    void accept_accumulates_usage(long promptTokens, long generationTokens, long expectedPrompt, long expectedGeneration) {
        aggregator.accept(usage(promptTokens, generationTokens));
        assertUsageEquals(expectedPrompt, expectedGeneration);
    }

    @Test
    void accept_accumulates_maximum_usage_from_multiple_values() {
        aggregator.accept(usage(25L, 20L));
        aggregator.accept(usage(0L, 30L));
        aggregator.accept(usage(0L, 0L));

        assertUsageEquals(25L, 30L);
    }

    @Test
    void accept_does_not_accumulate_null_values() {
        aggregator.accept(usage(null, 1L));
        aggregator.accept(null);

        assertUsageEquals(0L, 1L);
    }

    private static Usage usage(Long promptTokens, Long generationTokens) {
        return new ImmutableUsage(promptTokens, generationTokens);
    }

    private void assertUsageEquals(long expectedPromptTokens, long expectedGenerationTokens) {
        assertAll(
                () -> assertEquals(expectedPromptTokens, aggregator.getPromptTokens()),
                () -> assertEquals(expectedPromptTokens, aggregator.toImmutableUsage().getPromptTokens()),
                () -> assertEquals(expectedGenerationTokens, aggregator.getGenerationTokens()),
                () -> assertEquals(expectedGenerationTokens, aggregator.toImmutableUsage().getGenerationTokens())
        );
    }
}