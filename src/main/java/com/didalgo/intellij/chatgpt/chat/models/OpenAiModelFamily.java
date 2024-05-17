/*
 * Copyright (c) 2024 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.chat.models;

import com.didalgo.intellij.chatgpt.settings.ChatGptSettings;
import org.springframework.ai.openai.OpenAiChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.ApiUtils;
import org.springframework.ai.openai.api.OpenAiApi;

public class OpenAiModelFamily implements ModelFamily {

    @Override
    public OpenAiChatClient createChatClient(ChatGptSettings.AssistantOptions config) {
        var baseUrl = config.isEnableCustomApiEndpointUrl()? config.getApiEndpointUrl(): getDefaultApiEndpointUrl();
        var apiKey = config.getApiKey();
        var api = new OpenAiApi(baseUrl, apiKey);
        var options = OpenAiChatOptions.builder()
                .withModel(config.getModelName())
                .withTemperature((float) config.getTemperature())
                .withTopP((float) config.getTopP())
                .withN(1)
                .build();
        return new OpenAiChatClient(api, options);
    }

    @Override
    public String getDefaultApiEndpointUrl() {
        return ApiUtils.DEFAULT_BASE_URL;
    }

    @Override
    public String getApiKeysHomepage() {
        return "https://platform.openai.com/api-keys";
    }
}
