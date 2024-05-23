/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.chat.client;

import com.didalgo.intellij.chatgpt.chat.AssistantType;
import com.didalgo.intellij.chatgpt.ui.MainConversationHandler;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import org.springframework.ai.chat.ChatModel;
import org.springframework.beans.factory.DisposableBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChatModelHolder {

    private static final Logger log = Logger.getInstance(MainConversationHandler.class);

    private static final Map<AssistantType, ChatModel> chatModels = new ConcurrentHashMap<>();


    public static synchronized ChatModel getChatModel(AssistantType type) {
        return chatModels.computeIfAbsent(type, __ -> createChatModel(type));
    }

    public static synchronized void refresh() {
        List<ChatModel> models = new ArrayList<>(chatModels.values());
        chatModels.clear();
        models.forEach(client -> {
            if (client instanceof DisposableBean disposable) {
                try {
                    disposable.destroy();
                } catch (Exception e) {
                    log.error(e);
                }
            }
        });
    }

    protected static ChatModel createChatModel(AssistantType type) {
        return ApplicationManager.getApplication().getService(ChatModelFactory.class).create(type);
    }
}
