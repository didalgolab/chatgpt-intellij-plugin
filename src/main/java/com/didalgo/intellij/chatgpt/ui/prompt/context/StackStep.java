/*
 * Copyright 2000-2020 JetBrains s.r.o.
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.ui.prompt.context;

import org.jetbrains.annotations.Nullable;

/**
 * Base interface for a single step (orientation) of a stack, displaying a list
 * of items or a single level of a tree structure.
 *
 * @see ListStackStep
 * @param <T> the type of the objects contained in the list or tree structure.
 */
public interface StackStep<T> {
    StackStep<?> FINAL_CHOICE = null;

    /**
     * Returns the title of list.
     */
    @Nullable String getTitle();

    /**
     * Handles the selection of an item in the list.
     *
     * @param selectedValue the selected item.
     * @param finalChoice If true, the action associated with the selected item should be displayed.
     * If false and the selected item has a submenu, the stack step for the submenu should be returned.
     * @return the substep to be displayed, or {@link #FINAL_CHOICE} if the stack should be closed after
     * the item has been selected.
     * @see #hasSubStep
     */
    @Nullable StackStep<?> onChosen(T selectedValue, boolean finalChoice);

    /**
     * Checks if the specified item in the list has an associated substep.
     *
     * @param selectedValue the value to check for substep presence.
     * @return true if the value has a substep, false otherwise.
     */
    boolean hasSubStep(T selectedValue);

    /**
     * Returns true if items in the list can be selected by typing part of an item's text.
     *
     * @return true if speed search is enabled, false otherwise.
     */
    boolean isSpeedSearchEnabled();

    /**
     * Returns the class supporting speed search in a stack.
     *
     * @return the speed search filter instance, or null if speed search is not supported.
     * @see #isSpeedSearchEnabled()
     */
    @Nullable SpeedSearchFilter<T> getSpeedSearchFilter();

    /**
     * Returns true if the submenu for the first selectable item should be displayed automatically when the item has a submenu.
     *
     * @return true if the submenu for the first selectable item should be displayed automatically, false otherwise.
     */
    boolean isAutoSelectionEnabled();
}
