/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.ui;

import com.didalgo.intellij.chatgpt.ChatGptBundle;
import com.didalgo.intellij.chatgpt.text.CodeFragment;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.project.Project;

import java.util.ArrayList;
import java.util.List;

public class TextEditorSelectionAwareSnippetizer implements ContextAwareSnippetizer {

    @Override
    public List<CodeFragment> fetchSnippets(Project project) {
        List<CodeFragment> selectedFragments = new ArrayList<>();

        for (FileEditor editor : FileEditorManager.getInstance(project).getSelectedEditors()) {
            if (editor instanceof TextEditor textEditor) {
                SelectionModel selectionModel = textEditor.getEditor().getSelectionModel();

                String text;
                if (selectionModel.hasSelection() && (text = selectionModel.getSelectedText()) != null && !text.isBlank()) {
                    String fileUrl = editor.getFile().getUrl();
                    selectedFragments.add(CodeFragment.of(text, editor.getFile().getExtension(), ChatGptBundle.message("code.fragment.title", fileUrl)));
                }
            }
        }
        return selectedFragments;
    }
}
