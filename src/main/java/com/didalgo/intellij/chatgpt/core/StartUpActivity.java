/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.core;

import com.intellij.openapi.project.Project;
import com.didalgo.intellij.chatgpt.settings.OpenAISettingsState;
import com.didalgo.intellij.chatgpt.ui.action.editor.ActionsUtil;
import com.intellij.openapi.startup.ProjectActivity;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import org.jetbrains.annotations.NotNull;

public class StartUpActivity implements ProjectActivity {

    @Override
    public Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {
        OpenAISettingsState.getInstance();
        ActionsUtil.refreshActions();

        return Unit.INSTANCE;
    }
}
