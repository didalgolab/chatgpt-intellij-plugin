/*
 * Copyright 2000-2020 JetBrains s.r.o.
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.ui.prompt.context;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.speedSearch.ElementFilter;
import com.intellij.ui.speedSearch.SpeedSearch;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.*;

public abstract class WizardStack extends AbstractStack implements ActionListener, ElementFilter {
    private static final Logger LOG = Logger.getInstance(WizardStack.class);

    protected final StackStep<Object> myStep;
    protected WizardStack myChild;

    private final ActionMap myActionMap = new ActionMap();
    private final InputMap myInputMap = new InputMap();

    public WizardStack(@Nullable Project project, @NotNull StackStep<Object> aStep) {
        myStep = aStep;

        mySpeedSearch.setEnabled(myStep.isSpeedSearchEnabled());

        final JComponent content = createContent();
        JComponent popupComponent = createPopupComponent(content);

        init(project, popupComponent, true, true, aStep.getTitle(), true);
    }

    @NotNull
    protected JComponent createPopupComponent(JComponent content) {
        JScrollPane scrollPane = createScrollPane(content);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getHorizontalScrollBar().setBorder(null);

        scrollPane.getActionMap().get("unitScrollLeft").setEnabled(false);
        scrollPane.getActionMap().get("unitScrollRight").setEnabled(false);

        scrollPane.setBorder(JBUI.Borders.empty());
        return scrollPane;
    }

    @NotNull
    protected JScrollPane createScrollPane(JComponent content) {
        return ScrollPaneFactory.createScrollPane(content);
    }

    protected abstract JComponent createContent();

    public final void registerAction(@NonNls String aActionName, int aKeyCode, int aModifier, Action aAction) {
        myInputMap.put(KeyStroke.getKeyStroke(aKeyCode, aModifier), aActionName);
        myActionMap.put(aActionName, aAction);
    }

    public StackStep getStep() {
        return myStep;
    }

    @Override
    public final void actionPerformed(ActionEvent e) {
    }

    @Override
    public boolean shouldBeShowing(Object value) {
        if (!myStep.isSpeedSearchEnabled()) return true;
        SpeedSearchFilter<Object> filter = myStep.getSpeedSearchFilter();
        if (filter == null) return true;
        if (!filter.canBeHidden(value)) return true;
        if (!mySpeedSearch.isHoldingFilter()) return true;
        String text = filter.getIndexedString(value);
        return mySpeedSearch.shouldBeShowing(text);
    }

    public SpeedSearch getSpeedSearch() {
        return mySpeedSearch;
    }
}
