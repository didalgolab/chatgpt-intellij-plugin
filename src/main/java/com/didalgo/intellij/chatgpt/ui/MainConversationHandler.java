/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.ui;

import com.didalgo.intellij.chatgpt.ChatGptHandler;
import com.didalgo.intellij.chatgpt.chat.ChatMessageEvent;
import com.didalgo.intellij.chatgpt.chat.ChatMessageListener;
import com.didalgo.intellij.chatgpt.chat.ConversationContext;
import com.didalgo.intellij.chatgpt.chat.ConversationHandler;
import com.didalgo.intellij.chatgpt.core.ChatCompletionRequestProvider;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MainConversationHandler implements ConversationHandler {

    private static final Logger LOG = Logger.getInstance(MainConversationHandler.class);

    private final MainPanel mainPanel;

    public MainConversationHandler(MainPanel mainPanel) {
        this.mainPanel = mainPanel;
    }

    @Override
    public Disposable push(ConversationContext ctx, ChatMessageEvent.Starting event, ChatMessageListener listener) {
        var application = ApplicationManager.getApplication();
        var userMessage = event.getUserMessage();
        var chatCompletionRequestProvider = application.getService(ChatCompletionRequestProvider.class);
        var chatCompletionRequest = chatCompletionRequestProvider.chatCompletionRequest(ctx, userMessage)
                .stream(true)
                .build();

        return application.getService(ChatGptHandler.class)
                .handle(ctx, event.started(chatCompletionRequest), listener)
                .subscribeOn(Schedulers.io())
                .subscribe();
    }
}
