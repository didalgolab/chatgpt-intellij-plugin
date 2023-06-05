/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.ui.listener;

import com.didalgo.intellij.chatgpt.ChatGptBundle;
import com.didalgo.intellij.chatgpt.ChatGptIcons;
import com.didalgo.intellij.chatgpt.chat.ChatLink;
import com.didalgo.intellij.chatgpt.ui.ContextAwareSnippetizer;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.OptionAction;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.event.*;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class SubmitListener extends AbstractAction implements OptionAction, HyperlinkListener {

    private final ChatLink chatLink;
    private final Supplier<String> prompt;
    private final ContextAwareSnippetizer snippetizer;

    public SubmitListener(ChatLink chatLink, Supplier<String> prompt, ContextAwareSnippetizer snippetizer) {
        super(ChatGptBundle.message("ui.toolwindow.send"), ChatGptIcons.SEND);
        this.chatLink = chatLink;
        this.prompt = prompt;
        this.snippetizer = snippetizer;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        submitPrompt(prompt.get());
    }

    public void submitPrompt(String prompt) {
        Project project = chatLink.getProject();
        chatLink.pushMessage(prompt, snippetizer.fetchSnippets(project));
    }

    @Override
    public void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            String prompt = extractPromptFromUrl(URI.create(e.getDescription()));
            if (!prompt.isEmpty())
                submitPrompt(prompt);
        }
    }

    public static String extractPromptFromUrl(URI uri) {
        final String PROMPT_QUERY_PARAM = "prompt=";
        if ("assistant".equals(uri.getScheme()) && uri.getQuery() != null && uri.getQuery().startsWith(PROMPT_QUERY_PARAM)) {
            String prompt = uri.getQuery().substring(PROMPT_QUERY_PARAM.length());
            return URLDecoder.decode(prompt, StandardCharsets.UTF_8);
        }
        return "";
    }

    public void regenerateResponse(ActionEvent ignored) {
        chatLink.regenerateResponse();
    }

    @Override
    public Action @NotNull [] getOptions() {
        return new Action[] {
                OptionAction.create("Regenerate Response", this::regenerateResponse)
        };
    }

    public static class OptionAction extends AbstractAction {
        private final Consumer<ActionEvent> actionPerformer;

        public OptionAction(String text, Consumer<ActionEvent> actionPerformer) {
            super(text);
            this.actionPerformer = actionPerformer;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            actionPerformer.accept(e);
        }

        public static OptionAction create(String text, Consumer<ActionEvent> actionPerformer) {
            return new OptionAction(text, actionPerformer);
        }
    }
}
