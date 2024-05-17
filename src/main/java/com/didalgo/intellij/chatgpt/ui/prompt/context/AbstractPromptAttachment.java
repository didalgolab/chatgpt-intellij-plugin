/*
 * Copyright (c) 2024 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.ui.prompt.context;

import com.didalgo.intellij.chatgpt.chat.PromptAttachment;
import com.intellij.openapi.application.ApplicationManager;

import javax.swing.*;
import java.util.function.ToIntFunction;

public abstract class AbstractPromptAttachment implements PromptAttachment {
    private final Icon icon;
    private final String name;
    private boolean pinned;
    private volatile int tokenCount = -1;


    protected AbstractPromptAttachment(Icon icon, String name) {
        this.icon = icon;
        this.name = name;
    }

    @Override
    public final Icon getIcon() {
        return icon;
    }

    @Override
    public final String getName() {
        return name;
    }

    @Override
    public boolean isPinned() {
        return pinned;
    }

    @Override
    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }

    @Override
    public int getEstimatedTokenCount(ToIntFunction<? super PromptAttachment> estimator) {
        var tokenCount = this.tokenCount;
        if (tokenCount < 0 && estimator != null) {
            ApplicationManager.getApplication().executeOnPooledThread(() -> setTokenCount(estimateTokenCount(estimator)));
        }
        return tokenCount;
    }

    protected int estimateTokenCount(ToIntFunction<? super PromptAttachment> estimator) {
        return -1;
    }

    public final void setTokenCount(int tokenCount) {
        this.tokenCount = tokenCount;
    }
}
