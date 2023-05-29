package com.didalgo.intellij.chatgpt.ui;

import com.intellij.openapi.editor.textarea.TextComponentEditorImpl;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.text.JTextComponent;

public class ExpandableTextComponentEditorImpl extends TextComponentEditorImpl {

    public ExpandableTextComponentEditorImpl(Project project, @NotNull JTextComponent textComponent) {
        super(project, textComponent);
    }

    @Override
    public boolean isOneLineMode() {
        return false;
    }
}
