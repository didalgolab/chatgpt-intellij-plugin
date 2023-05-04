/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.ui.listener;

import com.didalgo.intellij.chatgpt.chat.ChatLink;
import com.didalgo.intellij.chatgpt.ui.ContextAwareSnippetizer;
import com.intellij.openapi.project.Project;

import java.awt.event.*;
import java.util.function.Supplier;

public class SubmitListener implements ActionListener {

    private final ChatLink chatLink;
    private final Supplier<String> prompt;
    private final ContextAwareSnippetizer snippetizer;

    public SubmitListener(ChatLink chatLink, Supplier<String> prompt, ContextAwareSnippetizer snippetizer) {
        this.chatLink = chatLink;
        this.prompt = prompt;
        this.snippetizer = snippetizer;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String text = prompt.get();
        Project project = chatLink.getProject();
        chatLink.pushMessage(text, snippetizer.fetchSnippets(project));
    }
}
