/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.ui.action.tool;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;

public class PasteInterceptor extends EditorActionHandler {
    private final EditorActionHandler originalHandler;

    public PasteInterceptor(EditorActionHandler originalHandler) {
        this.originalHandler = originalHandler;
    }

    @Override
    protected void doExecute(Editor editor, Caret caret, DataContext dataContext) {
        // Your custom logic here, gumshoe.
        // Maybe log something or manipulate the clipboard data?

        // Don't forget to call the original handler, or you'll break the paste action.
        originalHandler.execute(editor, caret, dataContext);
    }
}