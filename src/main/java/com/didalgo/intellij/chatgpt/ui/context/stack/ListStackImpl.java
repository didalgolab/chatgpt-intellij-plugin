/*
 * Copyright 2000-2020 JetBrains s.r.o.
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.ui.context.stack;

import com.intellij.ide.IdeEventQueue;
import com.intellij.ide.ui.UISettings;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.util.PopupUtil;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.statistics.StatisticsInfo;
import com.intellij.psi.statistics.StatisticsManager;
import com.intellij.ui.ScrollingUtil;
import com.intellij.ui.components.JBList;
import com.intellij.ui.popup.HintUpdateSupply;
import com.intellij.ui.popup.util.PopupImplUtil;
import com.intellij.util.ui.JBInsets;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.*;
import java.util.Comparator;
import java.util.Objects;

import static java.awt.event.InputEvent.CTRL_MASK;
import static java.awt.event.InputEvent.META_MASK;

public class ListStackImpl extends WizardStack implements ListStack {
    static final int NEXT_STEP_AREA_WIDTH = 20;

    private static final Logger LOG = Logger.getInstance(ListStackImpl.class);
    protected final NonActionsStackInlineSupport myPopupInlineActionsSupport = new NonActionsStackInlineSupport(this);

    private MyList myList;

    private MyMouseMotionListener myMouseMotionListener;
    private MyMouseListener myMouseListener;

    private ListStackModel myListModel;

    private int myIndexForShowingChild = -1;
    private int myMaxRowCount = 30;

    public ListStackImpl(@Nullable Project project,
                         @NotNull ListStackStep aStep) {
        super(project, aStep);
        replacePasteAction();
    }

    public void setMaxRowCount(int maxRowCount) {
        if (maxRowCount <= 0) return;
        myMaxRowCount = maxRowCount;
    }

    public ListStackModel getListModel() {
        return myListModel;
    }

    @Override
    public boolean beforeShow() {
        myList.addMouseMotionListener(myMouseMotionListener);
        myList.addMouseListener(myMouseListener);
        myList.setVisibleRowCount(myMaxRowCount);

        boolean shouldShow = super.beforeShow();
        return shouldShow;
    }

    protected boolean shouldUseStatistics() {
        return true;
    }

    private boolean autoSelectUsingStatistics() {
        String filter = getSpeedSearch().getFilter();
        if (!StringUtil.isEmpty(filter)) {
            int maxUseCount = -1;
            int mostUsedValue = -1;
            int elementsCount = myListModel.getSize();
            for (int i = 0; i < elementsCount; i++) {
                Object value = myListModel.getElementAt(i);
                if (!isSelectable(value)) continue;
                String text = getListStep().getTextFor(value);
                int count =
                        StatisticsManager.getInstance().getUseCount(new StatisticsInfo("#list_popup:" + myStep.getTitle() + "#" + filter, text));
                if (count > maxUseCount) {
                    maxUseCount = count;
                    mostUsedValue = i;
                }
            }

            if (mostUsedValue > 0) {
                ScrollingUtil.selectItem(myList, mostUsedValue);
                return true;
            }
        }

        return false;
    }

    private void selectFirstSelectableItem() {
        for (int i = 0; i < myListModel.getSize(); i++) {
            if (getListStep().isSelectable(myListModel.getElementAt(i))) {
                myList.setSelectedIndex(i);
                break;
            }
        }
    }

    public JList getList() {
        return myList;
    }

    @Override
    protected JComponent createContent() {
        myMouseMotionListener = new MyMouseMotionListener();
        myMouseListener = new MyMouseListener();

        ListStackStep<Object> step = getListStep();
        myListModel = new ListStackModel(this, getSpeedSearch(), step);
        myList = new MyList();
        if (myStep.getTitle() != null) {
            myList.getAccessibleContext().setAccessibleName(myStep.getTitle());
        }

        myList.setSelectionModel(new MyListSelectionModel());

        selectFirstSelectableItem();
        myList.setBorder(new EmptyBorder(getListInsets()));

        myList.setCellRenderer(getListElementRenderer());

        registerAction("handleSelection1", KeyEvent.VK_ENTER, 0, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleSelect(true, createKeyEvent(e, KeyEvent.VK_ENTER));
            }
        });

        myList.addListSelectionListener(new ListSelectionListener() {
            private int prevItemIndex = -1;

            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (prevItemIndex == myList.getSelectedIndex()) return;
                prevItemIndex = myList.getSelectedIndex();
                myList.setSelectedButtonIndex(null);
            }
        });

        PopupUtil.applyNewUIBackground(myList);

        myList.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return myList;
    }

    protected @NotNull KeyEvent createKeyEvent(@NotNull ActionEvent e, int keyCode) {
        return new KeyEvent(myList, KeyEvent.KEY_PRESSED, e.getWhen(), e.getModifiers(), keyCode, KeyEvent.CHAR_UNDEFINED);
    }

    private boolean isMultiSelectionEnabled() {
        return false;
    }

    protected ListCellRenderer<?> getListElementRenderer() {
        return new StackListElementRenderer(this);
    }

    @Override
    public ListStackStep<Object> getListStep() {
        return (ListStackStep<Object>) myStep;
    }

    @Override
    public void handleSelect(boolean handleFinalChoices) {
        _handleSelect(handleFinalChoices, null);
    }

    @Override
    public void handleSelect(boolean handleFinalChoices, InputEvent e) {
        _handleSelect(handleFinalChoices, e);
    }

    private boolean _handleSelect(boolean handleFinalChoices, @Nullable InputEvent e) {
        if (myList.getSelectedIndex() == -1) return false;

        if (getSpeedSearch().isHoldingFilter() && myList.getModel().getSize() == 0) return false;

        if (myList.getSelectedIndex() == getIndexForShowingChild()) {
            if (myChild != null && !myChild.isVisible()) setIndexForShowingChild(-1);
            return false;
        }

        Object[] selectedValues = myList.getSelectedValues();
        if (selectedValues.length == 0) return false;
        ListStackStep<Object> listStep = getListStep();
        Object selectedValue = selectedValues[0];
        if (!listStep.isSelectable(selectedValue)) return false;

        valuesSelected(selectedValues);

        return false;
    }

    private void valuesSelected(Object[] values) {
        if (shouldUseStatistics()) {
            String filter = getSpeedSearch().getFilter();
            if (!StringUtil.isEmpty(filter)) {
                for (Object value : values) {
                    String text = getListStep().getTextFor(value);
                    StatisticsManager.getInstance().incUseCount(new StatisticsInfo("#list_popup:" + getListStep().getTitle() + "#" + filter, text));
                }
            }
        }
    }

    @Override
    public void addListSelectionListener(ListSelectionListener listSelectionListener) {
        myList.addListSelectionListener(listSelectionListener);
    }

    private enum ExtendMode {
        NO_EXTEND, EXTEND_ON_HOVER
    }

    private class MyMouseMotionListener extends MouseMotionAdapter {

        private int myLastSelectedIndex = -2;
        private Point myLastMouseLocation;

        private boolean isMouseMoved(Point location) {
            if (myLastMouseLocation == null) {
                myLastMouseLocation = location;
                return false;
            }
            Point prev = myLastMouseLocation;
            myLastMouseLocation = location;
            return !prev.equals(location);
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            if (!isMouseMoved(e.getLocationOnScreen())) return;

            Point point = e.getPoint();
            int index = myList.locationToIndex(point);

            if (isSelectableAt(index)) {
                if (index != myLastSelectedIndex) {
                    if (!isMultiSelectionEnabled() || !UIUtil.isSelectionButtonDown(e) && myList.getSelectedIndices().length <= 1) {
                        myList.setSelectedIndex(index);
                    }
                    myLastSelectedIndex = index;
                }

                Object element = myListModel.getElementAt(index);
                if (element != null && myPopupInlineActionsSupport.hasExtraButtons(element)) {
                    Integer buttonIndex = myPopupInlineActionsSupport.calcButtonIndex(element, point);
                    myList.setSelectedButtonIndex(buttonIndex);
                }
            }
            else {
                myList.clearSelection();
                myLastSelectedIndex = -1;
            }
        }
    }

    protected boolean isActionClick(MouseEvent e) {
        return UIUtil.isActionClick(e, MouseEvent.MOUSE_RELEASED, true);
    }

    private class MyMouseListener extends MouseAdapter {

        @Override
        public void mouseReleased(MouseEvent e) {
            if (!isActionClick(e) || isMultiSelectionEnabled() && UIUtil.isSelectionButtonDown(e)) return;
            IdeEventQueue.getInstance().blockNextEvents(e); // sometimes, after popup close, MOUSE_RELEASE event delivers to other components
            Object selectedValue = myList.getSelectedValue();
            ListStackStep<Object> listStep = getListStep();
            handleSelect(handleFinalChoices(e, selectedValue, listStep), e);
        }
    }

    protected boolean handleFinalChoices(MouseEvent e, Object selectedValue, ListStackStep<Object> listStep) {
        return selectedValue == null || !listStep.hasSubStep(selectedValue) || !listStep.isSelectable(selectedValue) || !isOnNextStepButton(e);
    }

    private boolean isOnNextStepButton(MouseEvent e) {
        int index = myList.getSelectedIndex();
        Rectangle bounds = myList.getCellBounds(index, index);
        if (bounds != null) {
            JBInsets.removeFrom(bounds, UIUtil.getListCellPadding());
        }
        Point point = e.getPoint();
        return bounds != null && point.getX() > bounds.width + bounds.getX() - NEXT_STEP_AREA_WIDTH;
    }

    private int getIndexForShowingChild() {
        return myIndexForShowingChild;
    }

    private void setIndexForShowingChild(int aIndexForShowingChild) {
        myIndexForShowingChild = aIndexForShowingChild;
    }

    interface ListWithInlineButtons {
        @Nullable Integer getSelectedButtonIndex();
    }

    private class MyList extends JBList implements DataProvider, ListWithInlineButtons {

        private @Nullable Integer selectedButtonIndex;

        MyList() {
            super(myListModel);
            HintUpdateSupply.installSimpleHintUpdateSupply(this);
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);
        }

        @Override
        public void processKeyEvent(KeyEvent e) {
            e.setSource(this);
            super.processKeyEvent(e);
        }

        @Override
        protected void processMouseEvent(MouseEvent e) {
            if (!isMultiSelectionEnabled() &&
                    (e.getModifiers() & Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) != 0) {
                // do not toggle selection with ctrl+click event in single-selection mode
                e.consume();
            }
            if (UIUtil.isActionClick(e, MouseEvent.MOUSE_PRESSED) && isOnNextStepButton(e)) {
                e.consume();
            }

            boolean isClick = UIUtil.isActionClick(e, MouseEvent.MOUSE_PRESSED) || UIUtil.isActionClick(e, MouseEvent.MOUSE_RELEASED);
            if (!isClick || myList.locationToIndex(e.getPoint()) == myList.getSelectedIndex() ||
                    isMultiSelectionEnabled() && hasMultiSelectionModifier(e)) {
                super.processMouseEvent(e);
            }
        }

        private boolean hasMultiSelectionModifier(@NotNull MouseEvent e) {
            return (e.getModifiers() & (SystemInfo.isMac ? META_MASK : CTRL_MASK)) != 0;
        }

        @Override
        public Object getData(@NotNull String dataId) {
            if (PlatformDataKeys.SPEED_SEARCH_COMPONENT.is(dataId)) {
                if (mySpeedSearchPatternField != null && mySpeedSearchPatternField.isVisible()) {
                    return mySpeedSearchPatternField;
                }
            }
            return PopupImplUtil.getDataImplForList(myList, dataId);
        }

        @Override
        public @Nullable Integer getSelectedButtonIndex() {
            return selectedButtonIndex;
        }

        private boolean setSelectedButtonIndex(@Nullable Integer index) {
            if (Objects.compare(index, selectedButtonIndex, Comparator.nullsFirst(Integer::compare)) == 0) return false;

            selectedButtonIndex = index;
            return true;
        }
    }

    private final class MyListSelectionModel extends DefaultListSelectionModel {
        private MyListSelectionModel() {
            setSelectionMode(isMultiSelectionEnabled() ? MULTIPLE_INTERVAL_SELECTION : SINGLE_SELECTION);
        }

        @Override
        public void clearSelection() {
            super.clearSelection();
            setAnchorSelectionIndex(-1);
            setLeadSelectionIndex(-1);
        }

        @Override
        public void setSelectionInterval(int index0, int index1) {
            if (getSelectionMode() == SINGLE_SELECTION) {
                int index = findSelectableIndex(index0, getLeadSelectionIndex());
                if (0 <= index) super.setSelectionInterval(index, index);
                if (index == 0) fireValueChanged(0, 0); // enforce listeners to be notified about initial selection
            }
            else {
                super.setSelectionInterval(index0, index1); // TODO: support when needed
            }
        }
    }

    private int findSelectableIndex(int index, int lead) {
        int size = myListModel.getSize();
        if (index < 0 || size <= index) return -1;

        // iterate through the first part of the available items
        int found = findSelectableIndexInModel(index, index < lead ? -1 : size);
        if (found >= 0) return found;

        // iterate through the second part of the available items
        UISettings settings = UISettings.getInstanceOrNull();
        return settings != null && settings.getCycleScrolling() && 1 == Math.abs(index - lead)
                ? findSelectableIndexInModel(index < lead ? size - 1 : 0, index)
                : findSelectableIndexInModel(index, lead < -1 ? -1 : Math.min(lead, size));
    }

    private int findSelectableIndexInModel(int index, int stop) {
        while (index != stop) {
            if (getListStep().isSelectable(myListModel.getElementAt(index))) return index;
            index += index > stop ? -1 : 1;
        }
        return -1;
    }

    @Override
    protected void onSpeedSearchPatternChanged() {
        myListModel.refilter();
        if (myListModel.getSize() > 0) {
            if (!(shouldUseStatistics() && autoSelectUsingStatistics())) {
                selectBestMatch();
            }
        }
    }

    private void selectBestMatch() {
        int fullMatchIndex = myListModel.getClosestMatchIndex();
        if (fullMatchIndex != -1 && isSelectableAt(fullMatchIndex)) {
            myList.setSelectedIndex(fullMatchIndex);
        }
        else {
            selectFirstSelectableItem();
        }
    }

    private void replacePasteAction() {
        if (myStep.isSpeedSearchEnabled()) {
            getList().getActionMap().put(TransferHandler.getPasteAction().getValue(Action.NAME), new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    getSpeedSearch().type(CopyPasteManager.getInstance().getContents(DataFlavor.stringFlavor));
                    getSpeedSearch().update();
                }
            });
        }
    }

    private boolean isSelectable(@Nullable Object value) {
        // it is possible to use null elements in list model
        try {
            return getListStep().isSelectable(value);
        }
        catch (Exception exception) {
            LOG.error(getListStep().getClass().getName(), exception);
            return false;
        }
    }

    private boolean isSelectableAt(int index) {
        if (0 <= index && index < myListModel.getSize()) {
            Object value = myListModel.getElementAt(index);
            if (isSelectable(value))
                return true;
        }
        return false;
    }

    private Insets getListInsets() {
        return PopupUtil.getListInsets(StringUtil.isNotEmpty(getStep().getTitle()), false);
    }
}
