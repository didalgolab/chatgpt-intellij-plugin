/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.chat;

import com.didalgo.intellij.chatgpt.core.TextSubstitutor;
import com.didalgo.intellij.chatgpt.ui.context.stack.DefaultInputContext;
import com.didalgo.intellij.chatgpt.text.CodeFragment;
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
    public void pushMessage(String prompt, List<CodeFragment> codeFragments) {
        pushMessage(prompt, codeFragments, getInputContext());
    }

    public void pushMessage(String prompt, List<CodeFragment> codeFragments, InputContext inputContext) {
        ChatMessageComposer composer = ApplicationManager.getApplication().getService(ChatMessageComposer.class);
        List<CodeFragment> mergedCtx = mergeContext(codeFragments, inputContext);
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

    private static List<CodeFragment> mergeContext(List<CodeFragment> codeFragments, InputContext inputContext) {
        if (inputContext.getEntries().isEmpty()) {
            return codeFragments;
        }

        List<CodeFragment> list = new ArrayList<>();
        Optional<CodeFragment> code;
        for (var contextEntry : inputContext.getEntries())
            if ((code = contextEntry.getCodeFragment()).isPresent())
                list.add(code.get());

        for (var codeFragment : codeFragments)
            if (!list.contains(codeFragment))
                list.add(codeFragment);

        return list;
    }
}
