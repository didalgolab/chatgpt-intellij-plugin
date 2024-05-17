/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.settings;

import com.didalgo.intellij.chatgpt.ChatGptBundle;
import com.didalgo.intellij.chatgpt.chat.models.ModelFamily;
import com.didalgo.intellij.chatgpt.chat.models.ModelType;
import com.intellij.openapi.options.Configurable;

import java.util.function.Predicate;

public class GPT4Panel extends ModelPagePanel implements Configurable {
    private static final Predicate<ModelType> openAiModels = model -> model.getFamily() == ModelFamily.OPEN_AI;

    public GPT4Panel() {
        super(openAiModels);
    }

    @Override
    protected ChatGptSettings.AssistantOptions getModelPageConfig(ChatGptSettings state) {
        return state.getGpt4Config();
    }

    @Override
    public String getDisplayName() {
        return ChatGptBundle.message("ui.setting.menu.text");
    }

}
