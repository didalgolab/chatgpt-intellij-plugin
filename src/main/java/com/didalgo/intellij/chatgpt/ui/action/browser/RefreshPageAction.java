/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.ui.action.browser;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.ui.jcef.JBCefBrowserBase;
import org.jetbrains.annotations.NotNull;

public class RefreshPageAction extends JBCefBrowserAction {

    public RefreshPageAction(JBCefBrowserBase browser) {
        super(browser, () -> "Refresh Page", AllIcons.Actions.Refresh);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        getCefBrowser().reload();
    }
}
