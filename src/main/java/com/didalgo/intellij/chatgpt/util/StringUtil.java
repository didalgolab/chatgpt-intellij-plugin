/* Copyright (c) 2023 Mariusz Bernacki <didalgo@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0 */
package com.didalgo.intellij.chatgpt.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StringUtil extends com.intellij.openapi.util.text.StringUtil {

    public static @NotNull String stripHtml(@NotNull String html, @Nullable String breaks) {
        if (breaks != null) {
            html = html.replaceAll("<br/?>", breaks);
        }

        return html.replaceAll("<(.|\n)*?>", "");
    }
}
