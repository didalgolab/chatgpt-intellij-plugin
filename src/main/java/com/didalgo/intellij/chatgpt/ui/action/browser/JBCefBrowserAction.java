/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.ui.action.browser;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.ui.jcef.JBCefBrowserBase;
import org.cef.browser.CefBrowser;

import javax.swing.*;
import java.util.function.Supplier;

public abstract class JBCefBrowserAction extends AnAction {

    private final JBCefBrowserBase browser;

    public JBCefBrowserAction(JBCefBrowserBase browser, Supplier<String> dynamicText, Icon icon) {
        super(dynamicText, icon);
        this.browser = browser;
    }

    public final JBCefBrowserBase getJBCefBrowserBase() {
        return browser;
    }

    public final CefBrowser getCefBrowser() {
        return getJBCefBrowserBase().getCefBrowser();
    }
}
