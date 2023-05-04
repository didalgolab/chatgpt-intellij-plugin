/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.ui.action.browser;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.ui.jcef.JBCefBrowserBase;
import org.jetbrains.annotations.NotNull;

public class ZoomInAction extends JBCefBrowserAction {

    public ZoomInAction(JBCefBrowserBase browser) {
        super(browser, () -> "Zoom In", AllIcons.General.Add);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        var browser = getJBCefBrowserBase();
        browser.setZoomLevel(browser.getZoomLevel() * 1.1);
    }
}
