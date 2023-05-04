/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.ui.action.editor;

import com.didalgo.intellij.chatgpt.ChatGptBundle;
import com.didalgo.intellij.chatgpt.settings.CustomAction;
import com.didalgo.intellij.chatgpt.settings.OpenAISettingsState;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DefaultActionGroup;

public class ActionsUtil {

    public static void refreshActions() {
        ActionManager actionManager = ActionManager.getInstance();
        AnAction existingActionGroup = actionManager.getAction("ActionGroup2");
        if (existingActionGroup instanceof DefaultActionGroup group) {
            group.removeAll();
            group.add(new CustomPromptAction());
            group.add(new TokenCountAction());
            group.addSeparator();
            group.add(new ExplainAction());
            group.add(new FindBugAction());
            group.add(new OptimizeAction());
            group.add(new MinimizeAction());
            group.add(new GenericEditorAction(() -> ChatGptBundle.message("action.code.test.menu"), "Add test case for this code."));

            group.addSeparator();
            for (CustomAction customAction : OpenAISettingsState.getInstance().getCustomActionsPrefix()) {
                group.add(new GenericEditorAction(customAction::getName, customAction.getCommand()));
            }
        }
    }
}
