/*
 * Copyright 2000-2020 JetBrains s.r.o.
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.ui.prompt.context;

public abstract class BaseStep<T> implements StackStep<T>, SpeedSearchFilter<T> {

    @Override
    public boolean isSpeedSearchEnabled() {
        return false;
    }

    @Override
    public SpeedSearchFilter<T> getSpeedSearchFilter() {
        return this;
    }

    @Override
    public String getIndexedString(T value) {
        return getTextFor(value);
    }

    public abstract String getTextFor(T value);

    @Override
    public boolean isAutoSelectionEnabled() {
        return true;
    }

}
