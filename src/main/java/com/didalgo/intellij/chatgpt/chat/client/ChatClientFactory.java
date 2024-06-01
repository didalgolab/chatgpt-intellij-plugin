/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.chat.client;

import com.didalgo.intellij.chatgpt.chat.AssistantType;
import com.didalgo.intellij.chatgpt.settings.GeneralSettings;
import org.springframework.ai.chat.client.ChatClient;

public class ChatClientFactory {

    public ChatClient create(AssistantType type) {
        return create(type, GeneralSettings.getInstance());
    }

    public ChatClient create(AssistantType type, GeneralSettings settings) {
        var chatModel = type.getFamily().createChatModel(settings.getAssistantOptions(type));
        return ChatClient.create(chatModel);
    }
}
