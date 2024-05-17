/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.ui;

import com.didalgo.intellij.chatgpt.text.CodeFragment;
import com.intellij.openapi.project.Project;

import java.util.List;

public interface ContextAwareSnippetizer {

    List<CodeFragment> fetchSnippets(Project project);

}
