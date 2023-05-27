/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt;

public interface ModelPage {
    ModelPage GPT_3_5 = ModelPage.of(Of.GPT_3_5);
    ModelPage GPT_4   = ModelPage.of(Of.GPT_4);
    ModelPage ONLINE  = ModelPage.of(Of.ONLINE);

    String name();

    static ModelPage of(String name) {
        return new Of(name);
    }

    record Of(String name) implements ModelPage {
        public static final String GPT_3_5 = "GPT_3_5";
        public static final String GPT_4   = "GPT_4";
        public static final String ONLINE  = "ONLINE";
    }
}
