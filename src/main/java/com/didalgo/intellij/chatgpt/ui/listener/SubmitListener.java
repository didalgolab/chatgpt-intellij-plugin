/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.ui.listener;

import com.didalgo.intellij.chatgpt.chat.ChatLink;
import com.didalgo.intellij.chatgpt.jshell.JShellHandle;
import com.didalgo.intellij.chatgpt.jshell.execution.DirectExecutionControlProvider;
import com.didalgo.intellij.chatgpt.ui.ContextAwareSnippetizer;
import com.intellij.openapi.project.Project;
import jdk.jshell.JShell;

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

        if (false) {
            ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(JShell.class.getClassLoader());
                JShell jShell = JShell.builder()
                        .executionEngine(new DirectExecutionControlProvider(), null)
                        .build();
                JShellHandle handle = new JShellHandle();

                jShell.close();

            } finally {
                Thread.currentThread().setContextClassLoader(originalClassLoader);
            }
            return;
        }

        Project project = chatLink.getProject();
        chatLink.pushMessage(text, snippetizer.fetchSnippets(project));
    }
}
