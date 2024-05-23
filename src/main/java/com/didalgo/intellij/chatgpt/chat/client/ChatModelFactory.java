/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.chat.client;

import com.didalgo.intellij.chatgpt.chat.AssistantType;
import com.didalgo.intellij.chatgpt.settings.ChatGptSettings;
import org.springframework.ai.chat.ChatModel;

public class ChatModelFactory {

    public ChatModel create(AssistantType type) {
        return create(type, ChatGptSettings.getInstance());
    }

    public ChatModel create(AssistantType type, ChatGptSettings settings) {
        return type.getFamily().createChatModel(settings.getAssistantOptions(type));
    }
}
