/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.ui.action.tool;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.DumbAwareAction;
import com.didalgo.intellij.chatgpt.settings.OpenAISettingsPanel;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public class SettingsAction extends DumbAwareAction {

    public SettingsAction(@NotNull @Nls String text) {
        super(() -> text, AllIcons.General.Settings);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        ShowSettingsUtil.getInstance().showSettingsDialog(e.getProject(), OpenAISettingsPanel.class);
    }
}
