/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.text;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Service
@NoArgsConstructor
@AllArgsConstructor
public final class CodeFragmentMarkdownFormatter implements CodeFragmentFormatter {

    private boolean withDescription = true;

    public static CodeFragmentMarkdownFormatter getDefault() {
        return ApplicationManager.getApplication().getService(CodeFragmentMarkdownFormatter.class);
    }

    @Override
    public CodeFragmentMarkdownFormatter withoutDescription() {
        if (!withDescription) {
            return this;
        }
        return new CodeFragmentMarkdownFormatter(false);
    }

    @Override
    public String format(CodeFragment cf) {
        if (cf.isEmpty()) {
            return "";
        }
        String content = cf.content();
        String fenceBlockDelim = determineMinimumFenceBlockDelimiter(content);

        StringBuilder sb = new StringBuilder();
        if (withDescription) {
            String description = cf.description();
            if (!description.isEmpty()) {
                sb.append('[').append(description).append(']').append('\n');
            }
        }
        sb.append(fenceBlockDelim).append(cf.language()).append('\n');
        sb.append(content).append('\n');
        sb.append(fenceBlockDelim);

        return sb.toString();
    }

    private String determineMinimumFenceBlockDelimiter(String content) {
        StringBuilder sb = new StringBuilder("```");
        while (content.contains(sb)) {
            sb.append("`");
        }
        return sb.toString();
    }
}
