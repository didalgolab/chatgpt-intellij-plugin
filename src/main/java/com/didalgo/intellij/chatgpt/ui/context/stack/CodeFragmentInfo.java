/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.ui.context.stack;

import com.didalgo.intellij.chatgpt.chat.InputContextEntry;
import com.didalgo.intellij.chatgpt.text.CodeFragment;
import com.intellij.openapi.application.ApplicationManager;

import javax.swing.*;
import java.util.Optional;
import java.util.function.ToIntFunction;

public class CodeFragmentInfo implements InputContextEntry {
    private final CodeFragment codeFragment;
    private final Icon icon;
    private final String text;
    private volatile int tokenCount = -1;

    public CodeFragmentInfo(Icon icon, String text, CodeFragment codeFragment) {
        this.icon = icon;
        this.text = text;
        this.codeFragment = codeFragment;
    }

    @Override
    public Optional<CodeFragment> getCodeFragment() {
        return Optional.of(codeFragment);
    }

    public final Icon getIcon() {
        return icon;
    }

    public final String getText() {
        return text;
    }

    public int getOrComputeTokenCount(ToIntFunction<CodeFragmentInfo> calculator) {
        var tokenCount = this.tokenCount;
        if (tokenCount < 0 && calculator != null) {
            ApplicationManager.getApplication().executeOnPooledThread(() -> setTokenCount(calculator.applyAsInt(this)));
        }
        return tokenCount;
    }

    public void setTokenCount(int tokenCount) {
        this.tokenCount = tokenCount;
    }

    private boolean pinned;

    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }

}
