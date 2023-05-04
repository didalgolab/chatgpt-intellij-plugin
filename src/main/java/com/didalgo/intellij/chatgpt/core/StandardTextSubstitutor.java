/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.core;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class StandardTextSubstitutor implements TextSubstitutor {

    private static final String PLACEHOLDER_START = "${{", PLACEHOLDER_END = "}}";
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter
            .ofPattern("E, d MMM yyyy HH:mm:ss z", Locale.ENGLISH);

    private final Project project;

    public StandardTextSubstitutor(Project project) {
        this.project = project;
    }

    @Override
    public String resolvePlaceholders(String text) {
        if (text.contains(PLACEHOLDER_START)) {
            var sb = new StringBuilder(text);
            text = resolvePlaceholders(sb);
        }
        return text;
    }

    @NotNull
    private String resolvePlaceholders(StringBuilder sb) {
        int startIndex = 0;
        while (startIndex < sb.length()) {
            int placeholderStartIndex = sb.indexOf(PLACEHOLDER_START, startIndex);
            int placeholderEndIndex = sb.indexOf(PLACEHOLDER_END, placeholderStartIndex + PLACEHOLDER_START.length());
            if (placeholderStartIndex < 0 || placeholderEndIndex < 0) {
                break;
            }
            String placeholder = sb.substring(placeholderStartIndex + PLACEHOLDER_START.length(), placeholderEndIndex);
            resolvePlaceholder(sb, placeholderStartIndex, placeholderEndIndex + PLACEHOLDER_END.length(), placeholder);
            startIndex = placeholderEndIndex + PLACEHOLDER_END.length();
        }
        return sb.toString();
    }

    private void resolvePlaceholder(StringBuilder sb, int placeholderStartIndex, int placeholderEndIndex, String placeholder) {
        if (placeholder.equals("NOW")) {
            sb.replace(placeholderStartIndex, placeholderEndIndex, currentDateTime());
        }
    }

    protected String currentDateTime() {
        return ZonedDateTime.now().format(DATE_TIME_FORMATTER);
    }
}
