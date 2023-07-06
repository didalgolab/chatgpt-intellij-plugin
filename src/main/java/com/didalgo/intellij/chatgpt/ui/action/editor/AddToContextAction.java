/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.ui.action.editor;

import com.didalgo.intellij.chatgpt.chat.ChatLink;
import com.didalgo.intellij.chatgpt.ui.context.stack.CodeFragmentInfo;
import com.didalgo.intellij.chatgpt.text.CodeFragmentFactory;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.util.IconUtil;
import org.jetbrains.annotations.NotNull;

public class AddToContextAction extends AnAction {

    public AddToContextAction() {
        super("ChatGPT: Add to Context", "Adds selected code as part of next ChatGPT prompt", null);
    }


    @Override
    public void update(@NotNull AnActionEvent event) {
        // Enable the action only when an editor is available
        event.getPresentation().setEnabled(event.getDataContext().getData(CommonDataKeys.EDITOR) != null);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        // Get the current editor and project
        Editor editor = event.getData(CommonDataKeys.EDITOR);
        Project project = event.getProject();

        if (project != null && editor != null && editor.getVirtualFile() != null) {
            var icon = IconUtil.getIcon(editor.getVirtualFile(), 0, project);
            var text = editor.getVirtualFile().getPresentableName();
            CodeFragmentInfo info = new CodeFragmentInfo(icon, text, CodeFragmentFactory.create(editor));

            ChatLink.forProject(project).getInputContext().addEntry(info);
        }
    }
}
