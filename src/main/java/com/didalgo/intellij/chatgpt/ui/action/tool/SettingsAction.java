/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.ui.action.tool;

import com.didalgo.intellij.chatgpt.settings.OpenAISettingsPanel;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.DumbAwareAction;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public class SettingsAction extends DumbAwareAction {

    private final Class<? extends Configurable> panelToSelect;

    public SettingsAction(@NotNull @Nls String text) {
        this(text, OpenAISettingsPanel.class);
    }

    public SettingsAction(@NotNull @Nls String text, @NotNull Class<? extends Configurable> panelToSelect) {
        super(() -> text, AllIcons.General.Settings);
        this.panelToSelect = panelToSelect;
    }

    public Class<? extends Configurable> getPanelToSelect() {
        return panelToSelect;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        ShowSettingsUtil.getInstance().showSettingsDialog(e.getProject(), getPanelToSelect());
    }
}
