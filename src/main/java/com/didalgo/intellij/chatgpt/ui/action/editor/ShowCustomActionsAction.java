/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.ui.action.editor;

import com.didalgo.intellij.chatgpt.ChatGptBundle;
import com.didalgo.intellij.chatgpt.settings.CustomAction;
import com.didalgo.intellij.chatgpt.settings.ChatGptSettings;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ShowCustomActionsAction extends GenericEditorAction {

    public ShowCustomActionsAction() {
        super(() -> ChatGptBundle.message("action.code.custom.action"), "?");
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        ListPopup actionGroupPopup = JBPopupFactory.getInstance()
                .createActionGroupPopup(
                        ChatGptBundle.message("action.code.custom.action"),
                        new CustomPrefixActionGroup(), e.getDataContext(), true, null, Integer.MAX_VALUE);
        actionGroupPopup.showInBestPositionFor(e.getData(CommonDataKeys.EDITOR));
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabledAndVisible(true);
    }

    static class CustomPrefixActionGroup extends ActionGroup {

        public CustomPrefixActionGroup() {
            initialization();
        }

        private List<AnAction> initialization() {
            List<AnAction> anActionList = new ArrayList<>();
            anActionList.add(new CustomPromptAction());
            anActionList.add(new Separator());
            return anActionList;
        }

        @Override
        public AnAction @NotNull [] getChildren(@Nullable AnActionEvent e) {
            List<AnAction> actionList = initialization();
            for (CustomAction customAction : ChatGptSettings.getInstance().getCustomActionsPrefix()) {
                actionList.add(new GenericEditorAction(customAction::getName, customAction.getCommand()));
            }
            return actionList.toArray(AnAction[]::new);
        }
    }
}
