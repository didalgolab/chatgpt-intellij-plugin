/*
 * Copyright (c) 2024 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.chat.models;

import com.didalgo.intellij.chatgpt.settings.GeneralSettings;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.openai.api.OpenAiApi.ChatCompletionRequest.StreamOptions;
import org.springframework.ai.openai.api.common.OpenAiApiConstants;

import java.util.List;

public class OpenAiModelFamily implements ModelFamily {

    @Override
    public OpenAiChatModel createChatModel(GeneralSettings.AssistantOptions config) {
        var baseUrl = config.isEnableCustomApiEndpointUrl()? config.getApiEndpointUrl(): getDefaultApiEndpointUrl();
        var apiKey = config.getApiKey();
        var api = new OpenAiApi(baseUrl, apiKey);
        var options = OpenAiChatOptions.builder()
                .withModel(config.getModelName())
                .withTemperature((float) config.getTemperature())
                .withStreamOptions(config.isEnableStreamOptions() ? StreamOptions.INCLUDE_USAGE : null)
                .withTopP((float) config.getTopP())
                .withN(1)
                .build();
        return new OpenAiChatModel(api, options);
    }

    @Override
    public String getDefaultApiEndpointUrl() {
        return OpenAiApiConstants.DEFAULT_BASE_URL;
    }

    @Override
    public List<String> getCompatibleApiEndpointUrls() {
        return List.of(
                "https://api.groq.com/openai",
                "https://api.mistral.ai",
                "https://openrouter.ai/api"
        );
    }

    @Override
    public String getApiKeysHomepage() {
        return "https://platform.openai.com/api-keys";
    }
}
