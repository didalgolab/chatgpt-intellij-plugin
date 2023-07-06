/*
 * Copyright 2000-2020 JetBrains s.r.o.
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.ui.context.stack;

import com.intellij.openapi.util.NlsContexts.PopupTitle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class BaseListStackStep<T> extends BaseStep<T> implements ListStackStep<T> {
    private @PopupTitle String myTitle;
    private List<T> myValues;
    private List<? extends Icon> myIcons;
    private int myDefaultOptionIndex = -1;

    public BaseListStackStep(@PopupTitle @Nullable String title, List<T> values) {
        this(title, values, new ArrayList<>());
    }

    public BaseListStackStep(@PopupTitle @Nullable String title, @NotNull List<T> values, List<? extends Icon> icons) {
        init(title, values, icons);
    }

    protected final void init(@PopupTitle @Nullable String title, @NotNull List<T> values, @Nullable List<? extends Icon> icons) {
        myTitle = title;
        myValues = values;
        myIcons = icons;
    }

    @Override
    public final @Nullable String getTitle() {
        return myTitle;
    }

    @Override
    public @NotNull List<T> getValues() {
        return myValues;
    }

    @Override
    public @Nullable StackStep<?> onChosen(T selectedValue, boolean finalChoice) {
        return StackStep.FINAL_CHOICE;
    }

    @Override
    public Icon getIconFor(T value) {
        int index = myValues.indexOf(value);
        if (index != -1 && myIcons != null && index < myIcons.size()) {
            return myIcons.get(index);
        }
        else {
            return null;
        }
    }

    public @Nullable Color getBackgroundFor(T value) {
        return null;
    }

    public @Nullable Color getForegroundFor(@SuppressWarnings("unused") T value) {
        return null;
    }

    @Override
    public @NotNull String getTextFor(T value) {
        return value.toString();
    }

    @Override
    public @Nullable ListSeparator getSeparatorAbove(T value) {
        return null;
    }

    @Override
    public boolean isSelectable(T value) {
        return true;
    }

    @Override
    public boolean hasSubStep(T selectedValue) {
        return false;
    }

    public void setDefaultOptionIndex(int aDefaultOptionIndex) {
        myDefaultOptionIndex = aDefaultOptionIndex;
    }

    @Override
    public int getDefaultOptionIndex() {
        return myDefaultOptionIndex;
    }
}
