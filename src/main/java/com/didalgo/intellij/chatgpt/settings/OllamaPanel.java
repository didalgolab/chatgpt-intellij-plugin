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

import static com.didalgo.intellij.chatgpt.chat.AssistantType.System.OLLAMA;

public class OllamaPanel extends ModelPagePanel implements Configurable {
    private static final Predicate<ModelType> ollamaModels = model -> model.getFamily() == ModelFamily.OLLAMA;

    public OllamaPanel() {
        super(OLLAMA, ollamaModels);
    }

    @Override
    public String getDisplayName() {
        return ChatGptBundle.message("ui.setting.menu.text");
    }

    @Override
    protected boolean isModelNameEditable() {
        return true;
    }
}
