/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.chat.client;

import com.didalgo.intellij.chatgpt.chat.AssistantType;
import com.didalgo.intellij.chatgpt.settings.GeneralSettings;
import org.springframework.ai.chat.model.ChatModel;

public class ChatModelFactory {

    public ChatModel create(AssistantType type) {
        return create(type, GeneralSettings.getInstance());
    }

    public ChatModel create(AssistantType type, GeneralSettings settings) {
        return type.getFamily().createChatModel(settings.getAssistantOptions(type));
    }
}
