/*
 * Copyright 2000-2020 JetBrains s.r.o.
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.ui.prompt.context;

import javax.swing.*;

public class ListSeparator {

    private final String myText;
    private final Icon myIcon;

    public ListSeparator() {
        this("");
    }

    public ListSeparator(String aText) {
        this(aText, null);
    }

    public ListSeparator(String name, Icon icon) {
        myText = name;
        myIcon = icon;
    }

    public String getText() {
        return myText;
    }

    public Icon getIcon() {
        return myIcon;
    }
}
