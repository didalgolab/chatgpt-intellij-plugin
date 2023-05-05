/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.text;

import java.util.concurrent.atomic.AtomicReference;

import static java.util.Objects.requireNonNull;

/**
 * Represents a fragment of text, potentially in Markdown format.
 */
public interface TextFragment extends CharSequence {

    String markdown();

    static TextFragment of(String markdown) {
        return of(markdown, null);
    }

    static TextFragment of(String markdown, String html) {
        return new Of(markdown, new AtomicReference<>(html));
    }

    static TextFragment empty() {
        return of("");
    }

    record Of(String markdown, AtomicReference<String> html) implements TextFragment {
        public Of {
            requireNonNull(markdown, "markdown");
            requireNonNull(html, "html");
            html = new AtomicReference<>(html.get());
        }

        @Override
        public String toHtml() {
            String cachedHtml = html.get();
            if (cachedHtml == null) {
                String newHtml = TextFragment.super.toHtml();
                html.compareAndSet(null, newHtml);
                return newHtml;
            } else {
                return cachedHtml;
            }
        }

        @Override
        public String toString() {
            return markdown;
        }
    }

    @Override
    default int length() {
        return markdown().length();
    }

    @Override
    default char charAt(int index) {
        return markdown().charAt(index);
    }

    @Override
    default CharSequence subSequence(int start, int end) {
        return markdown().subSequence(start, end);
    }

    default String toHtml() {
        return TextFragmentToHtmlFormatter.getDefault().format(this);
    }

}
