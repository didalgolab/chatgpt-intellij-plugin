/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.ui.action.editor;

import com.didalgo.intellij.chatgpt.ChatGptBundle;

public class ExplainAction extends GenericEditorAction {
    public ExplainAction() {
        super(() -> ChatGptBundle.message("action.code.explain.menu"), "Explain this code");
    }
}
