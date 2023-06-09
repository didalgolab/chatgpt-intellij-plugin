/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.spi;

import com.didalgo.intellij.chatgpt.settings.OpenAISettingsState;
import com.theokanning.openai.service.OpenAiService;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class DefaultOpenAiServiceProvider implements OpenAiServiceProvider {

    @Override
    public boolean supportsEndpoint(String url) {
        return true;
    }

    @Override
    public OpenAiService createService(String page, OpenAISettingsState settings) {
        var modelSettings = settings.getConfigurationPage(page);

        return new OpenAiService(modelSettings.getApiKey(),
                Duration.of(Long.parseLong(settings.getReadTimeout()), ChronoUnit.MILLIS));
    }
}
