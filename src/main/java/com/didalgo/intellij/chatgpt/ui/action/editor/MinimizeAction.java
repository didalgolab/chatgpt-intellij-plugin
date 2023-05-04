/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.ui.action.editor;

import com.didalgo.intellij.chatgpt.ChatGptBundle;

public class MinimizeAction extends GenericEditorAction {
    public MinimizeAction() {
        super(() -> ChatGptBundle.message("action.code.minimize.menu"), "Rewrite this code in the shortest and the most concise form as you can think of.");
    }
}
