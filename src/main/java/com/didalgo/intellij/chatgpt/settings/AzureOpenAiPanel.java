/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.settings;

import com.didalgo.intellij.chatgpt.ChatGptBundle;
import com.didalgo.intellij.chatgpt.chat.models.ModelType;
import com.intellij.openapi.options.Configurable;

import java.util.function.Predicate;

import static com.didalgo.intellij.chatgpt.chat.AssistantType.System.*;

public class AzureOpenAiPanel extends ModelPagePanel implements Configurable {
    private static final Predicate<ModelType> anyModel = __ -> true;

    public AzureOpenAiPanel() {
        super(AZURE_OPENAI, anyModel);
    }

    @Override
    public final boolean isAzureCompatible() {
        return true;
    }

    @Override
    public String getDisplayName() {
        return ChatGptBundle.message("ui.setting.menu.text");
    }

}
