/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.chat.client;

import com.didalgo.intellij.chatgpt.chat.AssistantType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.DisposableBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChatClientHolder {

    private static final Logger log = Logger.getInstance(ChatClientHolder.class);

    private static final Map<AssistantType, ChatClient> chatClients = new ConcurrentHashMap<>();


    public static synchronized ChatClient getChatClient(AssistantType type) {
        return chatClients.computeIfAbsent(type, __ -> createChatClient(type));
    }

    public static synchronized void refresh() {
        List<ChatClient> clients = new ArrayList<>(chatClients.values());
        chatClients.clear();
        clients.forEach(client -> {
            if (client instanceof DisposableBean disposable) {
                try {
                    disposable.destroy();
                } catch (Exception e) {
                    log.error(e);
                }
            }
        });
    }

    protected static ChatClient createChatClient(AssistantType type) {
        return ApplicationManager.getApplication().getService(ChatClientFactory.class).create(type);
    }
}
