/*
 * Copyright 2000-2020 JetBrains s.r.o.
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.ui.prompt.context;

public interface SpeedSearchFilter<T> {
  default boolean canBeHidden(T value) {
    return true;
  }

  String getIndexedString(T value);
}
