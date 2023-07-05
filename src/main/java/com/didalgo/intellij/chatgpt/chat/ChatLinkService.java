/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.chat;

import com.didalgo.intellij.chatgpt.core.TextSubstitutor;
import com.didalgo.intellij.chatgpt.text.TextContent;
import com.didalgo.intellij.chatgpt.ui.context.stack.DefaultInputContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.theokanning.openai.completion.chat.ChatMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ChatLinkService extends AbstractChatLink {

    private final Project project;
    private final InputContext inputContext;
    private final ConversationHandler conversationHandler;
    private final ChatLinkState conversationContext;

    public ChatLinkService(Project project, ConversationHandler engine, ConfigurationPage configuration) {
        this.project = project;
        this.conversationHandler = engine;
        this.conversationContext = new ChatLinkState(configuration);
        this.conversationContext.setTextSubstitutor(project.getService(TextSubstitutor.class));
        this.inputContext = new DefaultInputContext();
    }

    @Override
    public Project getProject() {
        return project;
    }

    @Override
    public InputContext getInputContext() {
        return inputContext;
    }

    @Override
    public ConversationContext getConversationContext() {
        return conversationContext;
    }

    @Override
    public void pushMessage(String prompt, List<? extends TextContent> textContents) {
        pushMessage(prompt, textContents, getInputContext());
    }

    public void pushMessage(String prompt, List<? extends TextContent> textContents, InputContext inputContext) {
        ChatMessageComposer composer = ApplicationManager.getApplication().getService(ChatMessageComposer.class);
        List<? extends TextContent> mergedCtx = mergeContext(textContents, inputContext);
        ChatMessage message = composer.compose(conversationContext, prompt, mergedCtx);
        if (message.getContent().isEmpty()) {
            return;
        }

        inputContext.clear();

        ChatMessageListener listener = this.chatMessageListeners.fire();
        ChatMessageEvent.Starting event = ChatMessageEvent.starting(this, message);
        try {
            listener.exchangeStarting(event);
            conversationHandler.push(conversationContext, event, listener);
        } catch (ChatExchangeAbortException ex) {
            listener.exchangeCancelled(event.cancelled());
            getConversationContext().setLastPostedCodeFragments(List.of());
        } catch (Throwable x) {
            listener.exchangeFailed(event.failed(x));
            getConversationContext().setLastPostedCodeFragments(List.of());
        }
    }

    private static List<? extends TextContent> mergeContext(List<? extends TextContent> textContents, InputContext inputContext) {
        if (inputContext.getEntries().isEmpty()) {
            return textContents;
        }

        List<TextContent> list = new ArrayList<>();
        Optional<TextContent> code;
        for (var contextEntry : inputContext.getEntries())
            if ((code = contextEntry.getTextContent()).isPresent())
                list.add(code.get());

        for (var codeFragment : textContents)
            if (!list.contains(codeFragment))
                list.add(codeFragment);

        return list;
    }
}
