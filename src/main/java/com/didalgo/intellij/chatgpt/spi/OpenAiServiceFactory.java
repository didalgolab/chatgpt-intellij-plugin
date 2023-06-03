/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.spi;

import com.didalgo.intellij.chatgpt.settings.OpenAISettingsState;
import com.intellij.openapi.application.ApplicationManager;
import com.theokanning.openai.service.OpenAiService;

public class OpenAiServiceFactory {

    public OpenAiService create(String group) {
        return create(group, OpenAISettingsState.getInstance());
    }

    public OpenAiService create(String group, OpenAISettingsState settings) {
        var completionUrl = settings.getConfigurationPage(group).getApiEndpointUrl();
        return ApplicationManager.getApplication().getService(OpenAiServiceProviderRegistry.class)
                .getProviderForCompletionUrl(completionUrl)
                .createService(group, settings);
    }
}
