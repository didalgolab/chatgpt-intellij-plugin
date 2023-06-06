/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.core;

import com.intellij.openapi.project.Project;
import com.didalgo.intellij.chatgpt.settings.OpenAISettingsState;
import com.didalgo.intellij.chatgpt.ui.action.editor.ActionsUtil;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;

public class StartUpActivity implements StartupActivity {

    @Override
    public void runActivity(@NotNull Project project) {
        OpenAISettingsState.getInstance();
        ActionsUtil.refreshActions();
    }
}
