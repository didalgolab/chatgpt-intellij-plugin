/*
 * Copyright (c) 2024 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.chat.models;

import com.didalgo.ai.gemini.GeminiChatModel;
import com.didalgo.ai.gemini.GeminiChatOptions;
import com.didalgo.ai.gemini.api.GeminiApi;
import com.didalgo.intellij.chatgpt.settings.GeneralSettings;

public class GeminiModelFamily implements ModelFamily {

    @Override
    public GeminiChatModel createChatModel(GeneralSettings.AssistantOptions config) {
        var baseUrl = config.isEnableCustomApiEndpointUrl()? config.getApiEndpointUrl(): getDefaultApiEndpointUrl();
        var apiKey = config.getApiKey();
        var api = new GeminiApi(baseUrl, apiKey);
        var options = GeminiChatOptions.builder()
                .withSafetySettings(GeminiApi.SafetySettings.BLOCK_ONLY_HIGH)
                .withModel(config.getModelName())
                .withTemperature(config.getTemperature())
                .withTopP(config.getTopP())
                .build();
        return new GeminiChatModel(api, options);
    }

    @Override
    public String getDefaultApiEndpointUrl() {
        return GeminiApi.DEFAULT_BASE_URL;
    }

    @Override
    public String getApiKeysHomepage() {
        return "https://aistudio.google.com/app/apikey";
    }
}
