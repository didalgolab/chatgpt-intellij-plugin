/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.ui.action.browser;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.ui.MessageDialogBuilder;
import com.intellij.ui.jcef.JBCefBrowserBase;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.net.MalformedURLException;
import java.net.URL;

public class ClearCookiesAction extends JBCefBrowserAction {

    private final JPanel contentPanel;

    public ClearCookiesAction(JBCefBrowserBase browser, JPanel contentPanel) {
        super(browser, () -> "Clear Cookies", AllIcons.Actions.Cancel);
        this.contentPanel = contentPanel;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        boolean yes = MessageDialogBuilder.yesNo("Are you sure you want to clear all cookies?",
                        "Once the cookies are cleared, you will need to " +
                                "login again, are you sure to continue?")
                .yesText("Yes")
                .noText("No")
                .ask(contentPanel);
        if (yes) {
            try {
                var url = new URL(getCefBrowser().getURL());
                getJBCefBrowserBase().getJBCefCookieManager().getCefCookieManager().deleteCookies(url.getHost(), "");
            } catch (MalformedURLException ex) {
                Logger.getInstance(this.getClass())
                        .error("An error occurred while deleting cookies: " + ex.getMessage(), ex);
            }
        }
    }
}
