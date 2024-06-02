/*
 * Copyright (c) 2024 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.ui;

import com.didalgo.intellij.chatgpt.ui.text.ExpandableTextComponentEditorImpl;
import com.didalgo.intellij.chatgpt.ui.text.ExpandableTextFieldExt;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.editor.actionSystem.EditorTextInsertHandler;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.util.Producer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.datatransfer.Transferable;

public class PromptAttachmentPasteHandler extends EditorActionHandler implements EditorTextInsertHandler {

    private static final Logger log = Logger.getInstance(PromptAttachmentPasteHandler.class);

    private final EditorActionHandler originalHandler;

    public PromptAttachmentPasteHandler(EditorActionHandler originalHandler) {
        this.originalHandler = originalHandler;
    }

    @Override
    protected void doExecute(@NotNull Editor editor, @Nullable Caret caret, DataContext dataContext) {
        if (tryHandlePastedContent(editor, dataContext, null))
            return;

        originalHandler.execute(editor, caret, dataContext);
    }

    @Override
    public void execute(Editor editor, DataContext dataContext, @Nullable Producer<? extends Transferable> producer) {
        if (tryHandlePastedContent(editor, dataContext, null))
            return;

        if (originalHandler instanceof EditorTextInsertHandler orgInsrtHandler)
            orgInsrtHandler.execute(editor, dataContext, producer);
        else
            originalHandler.execute(editor, (Caret)null, dataContext);
    }

    private boolean tryHandlePastedContent(@NotNull Editor editor,
                                           DataContext dataContext,
                                           @Nullable Producer<? extends Transferable> producer) {
        try {
            if (editor.getClass() == ExpandableTextComponentEditorImpl.class) {
                var handler = dataContext.getData(ExpandableTextFieldExt.PROMPT_ATTACHMENT_HANDLER_KEY);
                if (handler != null) {
                    var content = getContentsToPasteToEditor(producer);
                    if (content != null && handler.handleTransferable(content))
                        return true;
                }
            }
        } catch (Exception e) {
            log.error("Unexpected error occurred while handling pasted content", e);
        }
        return false;
    }

    protected Transferable getContentsToPasteToEditor(@Nullable Producer<? extends Transferable> producer) {
        return producer == null ? CopyPasteManager.getInstance().getContents() : producer.produce();
    }
}
