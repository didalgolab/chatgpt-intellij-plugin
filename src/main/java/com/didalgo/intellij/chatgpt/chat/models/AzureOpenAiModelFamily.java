/*
 * Copyright (c) 2024 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.chat.models;

import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import com.didalgo.intellij.chatgpt.settings.GeneralSettings;
import org.springframework.ai.azure.openai.AzureOpenAiChatModel;
import org.springframework.ai.azure.openai.AzureOpenAiChatOptions;
import org.springframework.util.StringUtils;

public class AzureOpenAiModelFamily implements ModelFamily {

    @Override
    public AzureOpenAiChatModel createChatModel(GeneralSettings.AssistantOptions config) {
        checkConfigurationCompletness(config);

        var baseUrl = config.getAzureApiEndpoint();
        var apiKey = config.getApiKey();
        var api = new OpenAIClientBuilder().credential(new AzureKeyCredential(apiKey))
                .endpoint(baseUrl);
        var options = AzureOpenAiChatOptions.builder()
                .withDeploymentName(config.getAzureDeploymentName())
                .withTemperature(config.getTemperature())
                .withTopP(config.getTopP())
                .withN(1)
                .build();

        return new AzureOpenAiChatModel(api, options);
    }

    private static void checkConfigurationCompletness(GeneralSettings.AssistantOptions config) {
        if (!StringUtils.hasLength(config.getAzureApiEndpoint())) {
            throw new IllegalArgumentException("Azure OpenAI `apiEndpoint` is empty");
        }
        if (!StringUtils.hasLength(config.getApiKey())) {
            throw new IllegalArgumentException("Azure OpenAI `apiKey` is empty");
        }
        if (!StringUtils.hasLength(config.getAzureDeploymentName())) {
            throw new IllegalArgumentException("Azure OpenAI `deploymentName` is empty");
        }
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
