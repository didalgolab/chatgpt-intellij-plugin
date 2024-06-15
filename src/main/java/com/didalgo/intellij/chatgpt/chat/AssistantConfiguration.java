/*
 * Copyright (c) 2024 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.chat;

import com.didalgo.intellij.chatgpt.chat.models.ModelType;

import java.util.function.Supplier;

public interface AssistantConfiguration {

    AssistantType getAssistantType();

    String getModelName();

    ModelType getModelType();

    Supplier<String> getSystemPrompt();

    boolean isEnableStreamResponse();

    default AssistantConfiguration withSystemPrompt(Supplier<String> systemPrompt) {
        return new ConfigurationPageProxy(this) {
            @Override
            public Supplier<String> getSystemPrompt() {
                return systemPrompt;
            }
        };
    }
}
