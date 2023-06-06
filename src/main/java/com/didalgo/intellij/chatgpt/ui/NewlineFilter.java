/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.ui;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

public class NewlineFilter extends DocumentFilter {
    static final Character NEWLINE_REPLACEMENT = '\u23CE';

    @Override
    public void insertString(FilterBypass fb, int offset, String text,
                             AttributeSet attr) throws BadLocationException {
        text = denormalize(text);
        super.insertString(fb, offset, text, attr);
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String text,
                        AttributeSet attrs) throws BadLocationException {
        super.replace(fb, offset, length, text, attrs);
    }

    public static String denormalize(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        return text.replace('\n', NEWLINE_REPLACEMENT).replace("\r", "");
    }

    public static String normalize(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        return text.replace(NEWLINE_REPLACEMENT, '\n');
    }
}
