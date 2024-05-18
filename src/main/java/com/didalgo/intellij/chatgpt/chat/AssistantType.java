/*
 * Copyright (c) 2024 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.chat;

import com.didalgo.intellij.chatgpt.chat.models.ModelFamily;
import com.didalgo.intellij.chatgpt.settings.AzureOpenAiPanel;
import com.didalgo.intellij.chatgpt.settings.ClaudePanel;
import com.didalgo.intellij.chatgpt.settings.GPT35TurboPanel;
import com.didalgo.intellij.chatgpt.settings.GPT4Panel;
import com.didalgo.intellij.chatgpt.settings.OpenAISettingsPanel;
import com.intellij.openapi.options.Configurable;
import lombok.Getter;
import lombok.experimental.Accessors;

public sealed interface AssistantType
        permits AssistantType.System, AssistantType.Custom {

    String name();

    String displayName();

    ModelFamily getFamily();

    Class<? extends Configurable> getConfigurable();

    @Getter
    enum System implements AssistantType {
        GPT_4("GPT-4", ModelFamily.OPEN_AI, GPT4Panel.class),
        GPT_3_5("GPT-3.5-Turbo", ModelFamily.OPEN_AI, GPT35TurboPanel.class),
        AZURE_OPENAI("Azure OpenAI", ModelFamily.AZURE_OPENAI, AzureOpenAiPanel.class),
        CLAUDE("Claude", ModelFamily.ANTHROPIC, ClaudePanel.class),
        ONLINE("Online ChatGPT", null, OpenAISettingsPanel.class);

        private final @Accessors(fluent = true) String displayName;
        private final ModelFamily family;
        private final Class<? extends Configurable> configurable;

        System(String displayName, ModelFamily family, Class<? extends Configurable> configurable) {
            this.displayName = displayName;
            this.family = family;
            this.configurable = configurable;
        }
    }

    record Custom(
            String name,
            String displayName,
            ModelFamily getFamily,
            Class<? extends Configurable> getConfigurable
    ) implements AssistantType { }
}