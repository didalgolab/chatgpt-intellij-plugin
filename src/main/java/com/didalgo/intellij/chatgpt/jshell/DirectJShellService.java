/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.jshell;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;

import java.util.Map;

public class DirectJShellService extends JShellHandle implements Disposable, JShellService {
    private final Project project;

    public DirectJShellService(Project project) {
        this.project = project;
        setVariables(Map.of("project", project));
    }

    public final Project getProject() {
        return project;
    }

    @Override
    public void dispose() {
        getJShell().close();
    }
}
