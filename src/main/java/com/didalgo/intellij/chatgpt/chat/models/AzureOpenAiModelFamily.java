/*
 * Copyright (c) 2024 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.chat.models;

import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import com.didalgo.intellij.chatgpt.settings.ChatGptSettings;
import org.springframework.ai.azure.openai.AzureOpenAiChatClient;
import org.springframework.ai.azure.openai.AzureOpenAiChatOptions;

public class AzureOpenAiModelFamily implements ModelFamily {

    @Override
    public AzureOpenAiChatClient createChatClient(ChatGptSettings.AssistantOptions config) {
        var baseUrl = config.getAzureApiEndpoint();
        var apiKey = config.getApiKey();
        var api = new OpenAIClientBuilder().credential(new AzureKeyCredential(apiKey))
                .endpoint(baseUrl)
                .buildClient();
        var options = AzureOpenAiChatOptions.builder()
                .withDeploymentName(config.getAzureDeploymentName())
                .withTemperature((float) config.getTemperature())
                .withTopP((float) config.getTopP())
                .withN(1)
                .build();
        return new AzureOpenAiChatClient(api, options);
    }

    @Override
    public String getDefaultApiEndpointUrl() {
        return "";
    }

    @Override
    public String getApiKeysHomepage() {
        return "https://portal.azure.com/";
    }
}
