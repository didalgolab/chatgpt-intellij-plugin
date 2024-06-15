/*
 * Copyright (c) 2024 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.chat.models;

import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.Property;

import java.util.Objects;

public final class CustomModel implements ModelType {
    @Attribute("id")
    private String id;
    @Attribute(value = "family", converter = ModelFamilyConverter.class)
    private ModelFamily family;
    @Attribute("inputTokenLimit")
    private int inputTokenLimit;

    public CustomModel() {}

    public CustomModel(String id, ModelFamily family, int inputTokenLimit) {
        this.id = id;
        this.family = family;
        this.inputTokenLimit = inputTokenLimit;
    }

    @Override
    public ModelFamily getFamily() {
        return family;
    }

    @Override
    public int getInputTokenLimit() {
        return inputTokenLimit;
    }

    @Override
    @Property
    public String id() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (CustomModel) obj;
        return Objects.equals(this.id, that.id) &&
                Objects.equals(this.family, that.family) &&
                this.inputTokenLimit == that.inputTokenLimit;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, family, inputTokenLimit);
    }

    @Override
    public String toString() {
        return "CustomModel[" +
                "id=" + id + ", " +
                "family=" + family + ", " +
                "inputTokenLimit=" + inputTokenLimit + ']';
    }
}
