/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.ui;

import com.didalgo.intellij.chatgpt.text.CodeFragment;
import com.didalgo.intellij.chatgpt.text.CodeFragmentFactory;
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
                CodeFragmentFactory.createFromSelection(textEditor.getEditor()).ifPresent(selectedFragments::add);
            }
        }
        return selectedFragments;
    }
}
