/*
 * Copyright (c) 2024 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.chat.models;

import com.didalgo.intellij.chatgpt.settings.GeneralSettings;
import org.springframework.ai.chat.model.ChatModel;

public interface ModelFamily {

    ModelFamily OPEN_AI = new OpenAiModelFamily();

    ModelFamily AZURE_OPENAI = new AzureOpenAiModelFamily();

    ModelFamily ANTHROPIC = new AnthropicModelFamily();

    ModelFamily GEMINI = new GeminiModelFamily();

    ModelFamily OLLAMA = new OllamaModelFamily();

    ChatModel createChatModel(GeneralSettings.AssistantOptions config);

    String getDefaultApiEndpointUrl();

    String getApiKeysHomepage();

    default boolean isApiKeyOptional() {
        return "".equals(getApiKeysHomepage());
    }
}
