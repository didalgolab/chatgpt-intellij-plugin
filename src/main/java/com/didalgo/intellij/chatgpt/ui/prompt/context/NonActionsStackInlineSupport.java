/*
 * Copyright 2000-2020 JetBrains s.r.o.
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.ui.prompt.context;

import com.intellij.ide.IdeBundle;
import com.intellij.ui.scale.JBUIScale;
import com.intellij.util.ui.JBInsets;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;

public class NonActionsStackInlineSupport {
    private static final int INLINE_BUTTON_WIDTH = 16;
    private ListStackImpl myListStack;

    public NonActionsStackInlineSupport(ListStackImpl myListStack) {
        this.myListStack = myListStack;
    }

    public boolean hasExtraButtons(Object element) {
        return calcExtraButtonsCount(element) > 0;
    }

    public int calcExtraButtonsCount(Object element) {
        return hasMoreButton(element) ? 1 : 0;
    }

    public Integer calcButtonIndex(Object element, Point point) {
        if (element == null || !hasMoreButton(element)) return null;
        return calcButtonIndex(myListStack.getList(), 1, point);
    }

    public java.util.List<JComponent> getExtraButtons(JList list, Object value, boolean isSelected) {
        return java.util.Collections.emptyList();
    }

    public String getActiveExtraButtonToolTipText(JList list, Object value) {
        if (hasMoreButton(value) && getActiveButtonIndex(list) == 0) {
            return IdeBundle.message("inline.actions.more.actions.text");
        }

        return null;
    }

    public Integer getActiveButtonIndex(JList list) {
        return (list instanceof ListStackImpl.ListWithInlineButtons) ? ((ListStackImpl.ListWithInlineButtons)list).getSelectedButtonIndex() : null;
    }

    private boolean hasMoreButton(Object element) {
        return myListStack.getListStep().hasSubStep(element)
                && !myListStack.isShowSubmenuOnHover()
                && myListStack.getListStep().isFinal(element);
    }

    public static int buttonWidth(int leftRightInsets) {
        return JBUIScale.scale(INLINE_BUTTON_WIDTH + leftRightInsets * 2);
    }

    public static int buttonWidth() {
        int leftRightInsets = JBUI.CurrentTheme.List.buttonLeftRightInsets();
        return buttonWidth(leftRightInsets);
    }

    public Integer calcButtonIndex(JList<?> list, int buttonsCount, Point point) {
        int index = list.getSelectedIndex();
        Rectangle bounds = list.getCellBounds(index, index);
        if (bounds == null) {
            return null;
        }
        JBInsets.removeFrom(bounds, StackListElementRenderer.getListCellPadding());

        int distanceToRight = bounds.x + bounds.width - point.x;
        int buttonsToRight = distanceToRight / buttonWidth();
        if (buttonsToRight >= buttonsCount) {
            return null;
        }

        return buttonsCount - buttonsToRight - 1;
    }

}