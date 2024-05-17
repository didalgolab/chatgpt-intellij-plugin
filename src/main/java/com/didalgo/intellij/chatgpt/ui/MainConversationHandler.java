/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.ui;

import com.didalgo.intellij.chatgpt.chat.client.ChatHandler;
import com.didalgo.intellij.chatgpt.chat.ChatMessageEvent;
import com.didalgo.intellij.chatgpt.chat.ChatMessageListener;
import com.didalgo.intellij.chatgpt.chat.ConversationContext;
import com.didalgo.intellij.chatgpt.chat.ConversationHandler;
import com.didalgo.intellij.chatgpt.core.ChatCompletionRequestProvider;
import com.didalgo.intellij.chatgpt.ui.tool.window.ChatPanel;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import reactor.core.Disposable;
import reactor.core.scheduler.Schedulers;

public class MainConversationHandler implements ConversationHandler {

    private static final Logger LOG = Logger.getInstance(MainConversationHandler.class);

    private final ChatPanel chatPanel;

    public MainConversationHandler(ChatPanel chatPanel) {
        this.chatPanel = chatPanel;
    }

    @Override
    public Disposable push(ConversationContext ctx, ChatMessageEvent.Starting event, ChatMessageListener listener) {
        var application = ApplicationManager.getApplication();
        var userMessage = event.getUserMessage();
        var chatCompletionRequestProvider = application.getService(ChatCompletionRequestProvider.class);
        var chatCompletionRequest = chatCompletionRequestProvider.chatCompletionRequest(ctx, userMessage);

        return application.getService(ChatHandler.class)
                .handle(ctx, event.initiating(chatCompletionRequest), listener)
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();
    }
}
