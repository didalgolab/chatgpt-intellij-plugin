/*
 * Copyright 2000-2020 JetBrains s.r.o.
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.ui.prompt.context;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.ErrorLabel;
import com.intellij.ui.GroupedElementsRenderer;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class GroupedItemsListRenderer<E> extends GroupedElementsRenderer.List implements ListCellRenderer<E> {
    protected ListItemDescriptor<E> myDescriptor;

    protected JLabel myNextStepLabel;
    protected int myCurrentIndex;


    public GroupedItemsListRenderer(ListItemDescriptor<E> descriptor) {
        myDescriptor = descriptor;
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends E> list, E value, int index, boolean isSelected, boolean cellHasFocus) {
        String caption = myDescriptor.getCaptionAboveOf(value);
        boolean hasSeparator = hasSeparator(value, index);
        Icon icon = getItemIcon(value, isSelected);
        final JComponent result = configureComponent(myDescriptor.getTextFor(value), myDescriptor.getTooltipFor(value),
                icon, icon, isSelected, hasSeparator,
                caption, -1);
        myCurrentIndex = index;
        myRendererComponent.setBackground(list.getBackground());
        customizeComponent(list, value, isSelected);

        return result;
    }

    protected boolean hasSeparator(E value, int index) {
        String caption = myDescriptor.getCaptionAboveOf(value);
        if (index == 0 && StringUtil.isEmptyOrSpaces(caption)) {
            return false;
        }
        return myDescriptor.hasSeparatorAboveOf(value);
    }

    @Nullable
    protected Icon getItemIcon(E value, boolean isSelected) {
        return myDescriptor.getIconFor(value);
    }

    @Override
    protected JComponent createItemComponent() {
        createLabel();
        return layoutComponent(myTextLabel);
    }

    protected void createLabel() {
        myTextLabel = new ErrorLabel();
        myTextLabel.setBorder(JBUI.Borders.empty());
        myTextLabel.setOpaque(true);
    }

    protected JComponent layoutComponent(JComponent middleItemComponent) {
        myNextStepLabel = new JLabel();
        myNextStepLabel.setOpaque(false);

        return JBUI.Panels.simplePanel(middleItemComponent)
                .addToRight(myNextStepLabel)
                .withBorder(getDefaultItemComponentBorder());

    }

    protected void customizeComponent(JList<? extends E> list, E value, boolean isSelected) {
    }
}
