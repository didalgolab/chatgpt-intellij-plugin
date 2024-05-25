/*
 * Copyright (c) 2024 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.chat.models;

import com.didalgo.intellij.chatgpt.settings.ChatGptSettings;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;

public class OllamaModelFamily implements ModelFamily {

    private final static String DEFAULT_BASE_URL = "http://localhost:11434";

    @Override
    public OllamaChatModel createChatModel(ChatGptSettings.AssistantOptions config) {
        var baseUrl = config.isEnableCustomApiEndpointUrl()? config.getApiEndpointUrl(): getDefaultApiEndpointUrl();
        var api = new OllamaApi(baseUrl);
        var options = new OllamaOptions()
                .withModel(config.getModelName())
                .withTemperature((float) config.getTemperature())
                .withTopP((float) config.getTopP());
        return new OllamaChatModel(api, options);
    }

    @Override
    public String getDefaultApiEndpointUrl() {
        return DEFAULT_BASE_URL;
    }

    @Override
    public String getApiKeysHomepage() {
        return "";
    }
}
