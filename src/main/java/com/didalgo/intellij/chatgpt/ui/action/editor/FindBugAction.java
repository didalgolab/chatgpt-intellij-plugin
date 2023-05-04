/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.ui.action.editor;

import com.didalgo.intellij.chatgpt.ChatGptBundle;

public class FindBugAction extends GenericEditorAction {
    public FindBugAction() {
        super(() -> ChatGptBundle.message("action.code.wrong.menu"), "Find the bug in this code");
    }
}
