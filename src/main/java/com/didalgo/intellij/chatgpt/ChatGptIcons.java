/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

public class ChatGptIcons {
    public static final Icon ME = IconLoader.getIcon("/images/me.png", ChatGptIcons.class);
    public static final Icon AI = IconLoader.getIcon("/images/ai.png", ChatGptIcons.class);
    public static final Icon OPEN_AI = IconLoader.getIcon("/images/openai.png", ChatGptIcons.class);
    public static final Icon OPEN_AI_BLACK = IconLoader.getIcon("/images/openai-black.svg", ChatGptIcons.class);
    public static final Icon TOOL_WINDOW = IconLoader.getIcon("/icons/toolWindow.svg", ChatGptIcons.class);
    public static final Icon DEFAULT_ICON = IconLoader.getIcon("/icons/chatgpt-icon.svg", ChatGptIcons.class);
}
