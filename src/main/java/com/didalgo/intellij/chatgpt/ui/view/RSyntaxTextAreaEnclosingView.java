/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.ui.view;

import javax.swing.text.View;

public class RSyntaxTextAreaEnclosingView extends ProxiedView {

    public RSyntaxTextAreaEnclosingView(View delegate) {
        super(delegate);
    }
}
