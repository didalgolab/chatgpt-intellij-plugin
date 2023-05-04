/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.settings;

import com.intellij.util.xmlb.annotations.Tag;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@Tag("CustomAction")
public class CustomAction {
    @Tag("Name")
    private String name;
    @Tag("Description")
    private String command;

    public CustomAction() { }

    public CustomAction(String name, String command) {
        this.name = name;
        this.command = command;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, command);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CustomAction that) {
            return name.equals(that.name) && command.equals(that.command);
        }
        return false;
    }
}
