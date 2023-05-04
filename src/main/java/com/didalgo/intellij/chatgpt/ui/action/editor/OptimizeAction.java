/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.ui.action.editor;

import com.didalgo.intellij.chatgpt.ChatGptBundle;

public class OptimizeAction extends GenericEditorAction {
    public OptimizeAction() {
        super(() -> ChatGptBundle.message("action.code.optimize.menu"), "Optimize this code");
    }
}
