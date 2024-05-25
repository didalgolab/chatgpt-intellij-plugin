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

import static com.didalgo.intellij.chatgpt.chat.AssistantType.System.GPT_3_5;

public class GPT35TurboPanel extends ModelPagePanel implements Configurable {
    private static final Predicate<ModelType> openAiModels = model -> model.getFamily() == ModelFamily.OPEN_AI;

    public GPT35TurboPanel() {
        super(GPT_3_5, openAiModels);
    }

    @Override
    public String getDisplayName() {
        return ChatGptBundle.message("ui.setting.menu.text");
    }
}
