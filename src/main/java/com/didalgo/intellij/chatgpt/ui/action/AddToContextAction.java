/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.ui.action;

import com.didalgo.intellij.chatgpt.chat.ChatLink;
import com.didalgo.intellij.chatgpt.text.CodeFragmentFactory;
import com.didalgo.intellij.chatgpt.text.TextFragment;
import com.didalgo.intellij.chatgpt.text.TextFragmentUtils;
import com.didalgo.intellij.chatgpt.ui.prompt.context.TextPromptAttachment;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.util.IconUtil;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;

public class AddToContextAction extends AnAction {

    private static final DataKey<JBPopup> DOCUMENTATION_POPUP_KEY = DataKey.create("documentation.popup");

    public AddToContextAction() {
        super("ChatGPT: Add to Context", "Adds selected code as part of next ChatGPT prompt", null);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        // Enable the action only when an editor or other usable content is available
        DataContext dataContext = event.getDataContext();
        event.getPresentation().setEnabled(
                dataContext.getData(CommonDataKeys.EDITOR) != null || dataContext.getData(DOCUMENTATION_POPUP_KEY) != null);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null)
            return;

        JBPopup popup = e.getData(DOCUMENTATION_POPUP_KEY);
        if (popup != null)
            handlePopupData(project, popup);

        Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (editor != null && editor.getVirtualFile() != null)
            handleEditorData(project, editor);
    }

    private void handleEditorData(Project project, Editor editor) {
        var icon = IconUtil.getIcon(editor.getVirtualFile(), 0, project);
        var text = editor.getVirtualFile().getPresentableName();
        TextPromptAttachment entry = new TextPromptAttachment(icon, text, CodeFragmentFactory.create(editor));

        addToContext(project, entry);
    }

    private void handlePopupData(Project project, JBPopup popup) {
        TextFragment textFragment = TextFragmentUtils.scrapContent(popup.getContent());

        var icon = AllIcons.Actions.ShowAsTree;
        var text = "[" + LocalDateTime.now().withNano(0) + "]";
        TextPromptAttachment entry = new TextPromptAttachment(icon, text, textFragment);

        addToContext(project, entry);
    }

    protected void addToContext(Project project, TextPromptAttachment entry) {
        ChatLink.forProject(project).getInputContext().addAttachment(entry);
    }
}
