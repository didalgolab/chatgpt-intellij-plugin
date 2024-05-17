/*
 * Copyright (c) 2024 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.chat.models;

import com.didalgo.intellij.chatgpt.settings.ChatGptSettings;
import org.springframework.ai.chat.ChatClient;

public interface ModelFamily {

    ModelFamily OPEN_AI = new OpenAiModelFamily();

    ModelFamily AZURE_OPENAI = new AzureOpenAiModelFamily();

    ModelFamily ANTHROPIC = new AnthropicModelFamily();

    ChatClient createChatClient(ChatGptSettings.AssistantOptions config);

    String getDefaultApiEndpointUrl();

    String getApiKeysHomepage();

}
