/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.ui.action;

import com.didalgo.intellij.chatgpt.text.TextFragment;
import com.didalgo.intellij.chatgpt.ui.prompt.context.TextPromptAttachment;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.testFramework.junit5.TestApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@TestApplication
public class OpenInEditorActionTest {

    private Project project;
    private OpenInEditorAction action;
    private TextPromptAttachment selectedItem;

    @BeforeEach
    public void setUp() {
        project = ProjectManager.getInstance().getDefaultProject();
        action = new OpenInEditorAction();
        selectedItem = new TextPromptAttachment(null, "TEST", TextFragment.of("TEST CONTENT"));
    }

    @Test
    public void actionPerformed_opens_a_new_Editor_that_is_not_editable() throws InterruptedException, InvocationTargetException {
        SwingUtilities.invokeAndWait(() -> {
            var actionContext = Map.of(PlatformDataKeys.SELECTED_ITEM.getName(), selectedItem, CommonDataKeys.PROJECT.getName(), project);
            action.actionPerformed(AnActionEvent.createFromDataContext(ActionPlaces.UNKNOWN, null, actionContext::get));

            // verify
            var editors = FileEditorManager.getInstance(project).getAllEditors();
            assertEquals(1, editors.length);
            var editor = (TextEditor) editors[0];
            try {
                assertFalse(editor.getEditor().getDocument().isWritable());
            } finally {
                FileEditorManager.getInstance(project).closeFile(editor.getFile());
            }
        });
    }
}