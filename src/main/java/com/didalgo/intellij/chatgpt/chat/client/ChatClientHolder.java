/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.chat.client;

import com.didalgo.intellij.chatgpt.chat.AssistantType;
import com.intellij.openapi.application.ApplicationManager;
import org.springframework.ai.chat.ChatClient;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChatClientHolder {

    private static final Map<AssistantType, ChatClient> chatClients = new ConcurrentHashMap<>();


    public static synchronized ChatClient getChatClient(AssistantType type) {
        return chatClients.computeIfAbsent(type, __ -> createChatClient(type));
    }

    public static synchronized void refresh() {
        chatClients.clear();
    }

    protected static ChatClient createChatClient(AssistantType type) {
        return ApplicationManager.getApplication().getService(ChatClientFactory.class).create(type);
    }
}
