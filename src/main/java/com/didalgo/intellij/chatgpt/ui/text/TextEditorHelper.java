/*
 * Copyright (c) 2024 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.ui.text;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKey;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class TextEditorHelper {

    public static final DataKey<JTextArea> TEXT_AREA_KEY = DataKey.create("MyTextArea");


    public static Stream<TextEditor> selectedTextEditors(Project project, Predicate<TextEditor> supportedEditors) {
        FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
        return Arrays.stream(fileEditorManager.getSelectedEditors())
                .filter(editor -> editor instanceof TextEditor textEditor && supportedEditors.test(textEditor))
                .map(TextEditor.class::cast);
    }

    public static VirtualFile getEditorFile(TextEditor editor) {
        return FileDocumentManager.getInstance().getFile(editor.getEditor().getDocument());
    }

    public static String getSelectedTextOrEntireContent(JTextComponent tc) {
        String selectedText = tc.getSelectedText();
        return (selectedText == null || selectedText.isEmpty())? tc.getText() : selectedText;
    }

    public static JTextArea getTextArea(AnActionEvent event) {
        return event.getData(TEXT_AREA_KEY);
    }

}
