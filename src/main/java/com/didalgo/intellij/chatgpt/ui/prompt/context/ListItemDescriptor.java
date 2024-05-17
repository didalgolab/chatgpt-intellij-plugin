/*
 * Copyright 2000-2020 JetBrains s.r.o.
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.ui.prompt.context;

import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public interface ListItemDescriptor<T> {

    @Nullable String getTextFor(T value);

    @Nullable String getTooltipFor(T value);

    @Nullable Icon getIconFor(T value);

    default Icon getSelectedIconFor(T value) {
        return getIconFor(value);
    }

    boolean hasSeparatorAboveOf(T value);

    @Nullable String getCaptionAboveOf(T value);
}
