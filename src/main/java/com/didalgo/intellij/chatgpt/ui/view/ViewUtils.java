/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.ui.view;

import javax.swing.text.View;

public class ViewUtils {

    public static boolean containsSyntaxTextAreaView(View parent) {
        for (int i = 0; i < parent.getViewCount(); i++)
            if (parent.getView(i) instanceof RSyntaxTextAreaView)
                return true;

        return false;
    }
}
