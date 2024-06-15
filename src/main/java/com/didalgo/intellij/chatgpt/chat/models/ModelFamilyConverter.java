/*
 * Copyright (c) 2024 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.chat.models;

import com.intellij.util.xmlb.Converter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ModelFamilyConverter extends Converter<ModelFamily> {

    @Nullable
    @Override
    public ModelFamily fromString(@NotNull String name) {
        try {
            return ModelFamily.create(Class.forName(name).asSubclass(ModelFamily.class));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Nullable
    @Override
    public String toString(ModelFamily family) {
        return family.getClass().getName();
    }
}
