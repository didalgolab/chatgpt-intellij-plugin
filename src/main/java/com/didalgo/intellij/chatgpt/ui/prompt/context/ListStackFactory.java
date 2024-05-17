/*
 * Copyright 2000-2020 JetBrains s.r.o.
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.ui.prompt.context;

import com.didalgo.intellij.chatgpt.chat.InputContext;
import com.didalgo.intellij.chatgpt.chat.PromptAttachment;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.ActionCallback;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.Key;
import com.intellij.ui.*;
import com.intellij.util.ui.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.ToIntFunction;

public class ListStackFactory {
    private static final Key<Integer> HIDDEN_INFOS_SELECT_INDEX_KEY = Key.create("HIDDEN_INFOS_SELECT_INDEX_2");

    private PromptAttachment mySelectedInfo;

    public @Nullable PromptAttachment getSelectedInfo() {
        return mySelectedInfo;
    }

    private void setSelectedInfo(@Nullable PromptAttachment info) {
        mySelectedInfo = info;
    }

    public @NotNull ActionCallback select(@NotNull PromptAttachment info, boolean requestFocus) {
        return new ActionCallback.Done();
    }

    private class HiddenInfosListStackStep extends BaseListStackStep<PromptAttachment> {
        private final PromptAttachment separatorInfo;
        boolean selectTab = true;

        private HiddenInfosListStackStep(@NotNull List<PromptAttachment> values, @Nullable PromptAttachment separatorInfo) {
            super(null, values);
            this.separatorInfo = separatorInfo;
        }

        @Override
        public @Nullable StackStep<?> onChosen(PromptAttachment selectedValue, boolean finalChoice) {
            if (selectTab) {
                select(selectedValue, true);
            }
            else {
                selectTab = true;
            }
            return FINAL_CHOICE;
        }

        @Override
        public @Nullable ListSeparator getSeparatorAbove(PromptAttachment value) {
            return value == separatorInfo ? new ListSeparator() : null;
        }

        @Override
        public Icon getIconFor(PromptAttachment value) {
            return value.getIcon();
        }

        @Override
        public @NotNull String getTextFor(PromptAttachment value) {
            return value.getName();
        }
    }


    public ListStackImpl showListPopup(JComponent frame, Project myProject, InputContext inputContext, ToIntFunction<PromptAttachment> tokenCountCalculator) {
        PromptAttachment separatorInfo = null;

        HiddenInfosListStackStep step = new HiddenInfosListStackStep(inputContext.getAttachments(), separatorInfo);
        Integer selectedIndex = ClientProperty.get(frame, HIDDEN_INFOS_SELECT_INDEX_KEY);
        if (selectedIndex != null) {
            step.setDefaultOptionIndex(selectedIndex);
        }

        ListStackImpl popup = createListStack(myProject, step, renderer -> {
            ListItemDescriptor<PromptAttachment> descriptor = new ListItemDescriptorAdapter<>() {
                @Override
                public @NotNull String getTextFor(PromptAttachment value) {
                    return value.getName();
                }

                @Override
                public @Nullable Icon getIconFor(PromptAttachment value) {
                    return value.getIcon();
                }

                @Override
                public boolean hasSeparatorAboveOf(PromptAttachment value) {
                    return value == separatorInfo;
                }
            };
            return new GroupedItemsListRenderer<>(descriptor) {
                private static final Key<Integer> HOVER_INDEX_KEY = Key.create("HOVER_INDEX");
                private static final Key<PromptAttachment> TAB_INFO_KEY = Key.create("TAB_INFO");
                private static final Key<Boolean> SELECTED_KEY = Key.create("SELECTED");
                private static final NumberFormat tokenCountFormat = NumberFormat.getInstance();
                JPanel component;
                JLabel iconLabel;
                JLabel countLabel;
                JLabel actionLabel;
                MouseAdapter listMouseListener;

                @Override
                public Component getListCellRendererComponent(JList<? extends PromptAttachment> list, PromptAttachment value, int index, boolean isSelected, boolean cellHasFocus) {
                    return super.getListCellRendererComponent(list, value, index, false, cellHasFocus);
                }

                @Override
                protected Color getBackground() {
                    return frame.getBackground();
                }

                @Override
                protected JComponent createItemComponent() {
                    createLabel();

                    component = new JPanel();
                    BoxLayout layout = new BoxLayout(component, BoxLayout.X_AXIS);
                    component.setLayout(layout);

                    boolean closeTabButtonOnTheRight = true;
                    if (!closeTabButtonOnTheRight) {
                        addActionLabel();
                        int gap = JBUI.CurrentTheme.ActionsList.elementIconGap() - 2;
                        component.add(Box.createRigidArea(new Dimension(gap, 0)));
                    }

                    iconLabel = new JLabel();
                    component.add(iconLabel);
                    int gap = JBUI.CurrentTheme.ActionsList.elementIconGap() - 2;
                    component.add(Box.createRigidArea(new Dimension(gap, 0)));

                    component.add(myTextLabel);
                    component.add(Box.createRigidArea(new JBDimension(10, 0)));
                    addTokenCountLabel();

                    if (closeTabButtonOnTheRight) {
                        component.add(Box.createRigidArea(new JBDimension(30, 0)));
                        component.add(Box.createHorizontalGlue());
                        addActionLabel();
                    }

                    JComponent result = layoutComponent(component);
                    return result;
                }

                private void addActionLabel() {
                    actionLabel = new JLabel();
                    component.add(actionLabel);
                }

                private void addTokenCountLabel() {
                    countLabel = new JLabel("", SwingConstants.RIGHT);
                    var currentFont = countLabel.getFont();
                    countLabel.setFont(currentFont.deriveFont(currentFont.getSize2D() - 2f));
                    component.add(countLabel);
                }

                @Override
                protected void customizeComponent(JList<? extends PromptAttachment> list, PromptAttachment info, boolean isSelected) {
                    if (actionLabel != null) {
                        Icon icon = Objects.equals(ClientProperty.get(list, HOVER_INDEX_KEY), myCurrentIndex)
                                ? AllIcons.Actions.CloseHovered
                                : AllIcons.Actions.Close;

                        if (info.isPinned()) {
                            icon = AllIcons.Actions.PinTab;
                        }
                        actionLabel.setIcon(icon);
                        ClientProperty.put(actionLabel, TAB_INFO_KEY, info);

                        addMouseListener(list);
                    }

                    PromptAttachment selectedInfo = getSelectedInfo();
                    Icon icon = info.getIcon();
                    if (icon != null && info != selectedInfo) {
                        icon = IconLoader.getTransparentIcon(icon, JBUI.getFloat("EditorTabs.unselectedAlpha", 0.75f));
                    }
                    iconLabel.setIcon(icon);

                    var tokenCount = info.getEstimatedTokenCount(tokenCountCalculator);
                    if (tokenCount >= 0) {
                        synchronized (tokenCountFormat) {
                            countLabel.setText(tokenCountFormat.format(tokenCount) + " tokens");
                        }
                    }

                    ClientProperty.put(component, SELECTED_KEY, info == selectedInfo ? true : null);

                    component.invalidate();
                }

                @Override
                protected void setComponentIcon(Icon icon, Icon disabledIcon) {
                    // icon will be set in customizeComponent
                }

                @Override
                protected SeparatorWithText createSeparator() {
                    Insets labelInsets = JBUI.CurrentTheme.Popup.separatorLabelInsets();
                    return new GroupHeaderSeparator(labelInsets);
                }

                private void addMouseListener(JList<? extends PromptAttachment> list) {
                    if (listMouseListener != null) return;
                    listMouseListener = new MouseAdapter() {
                        @Override
                        public void mouseMoved(MouseEvent e) {
                            Point point = e.getLocationOnScreen();
                            SwingUtilities.convertPointFromScreen(point, list);
                            int hoveredIndex = list.locationToIndex(point);
                            Component renderer = ListUtil.getDeepestRendererChildComponentAt(list, e.getPoint());
                            updateHoveredIconIndex(ClientProperty.get(renderer, TAB_INFO_KEY) != null ? hoveredIndex : -1);
                        }

                        @Override
                        public void mouseExited(MouseEvent e) {
                            updateHoveredIconIndex(-1);
                        }

                        private void updateHoveredIconIndex(int hoveredIndex) {
                            Integer oldIndex = ClientProperty.get(list, HOVER_INDEX_KEY);
                            ClientProperty.put(list, HOVER_INDEX_KEY, hoveredIndex);
                            if (!Objects.equals(oldIndex, hoveredIndex)) {
                                list.repaint();
                            }
                        }

                        @Override
                        public void mouseReleased(MouseEvent e) {
                            Point point = e.getLocationOnScreen();
                            SwingUtilities.convertPointFromScreen(point, list);
                            int clickedIndex = list.locationToIndex(point);
                            Component renderer = ListUtil.getDeepestRendererChildComponentAt(list, e.getPoint());
                            if (!(renderer instanceof JLabel label)) {
                                return;
                            }

                            PromptAttachment textPromptAttachment = ClientProperty.get(label, TAB_INFO_KEY);
                            if (textPromptAttachment == null) {
                                return;
                            }

                            boolean clickToUnpin = false;
                            if (textPromptAttachment.isPinned()) {
                                textPromptAttachment.setPinned(false);
                                clickToUnpin = true;
                            }
                            if (!clickToUnpin) {
                                inputContext.removeAttachment(textPromptAttachment);
                            }

                            e.consume();
                            int indexToSelect = Math.min(clickedIndex, list.getModel().getSize());
                            ClientProperty.put(frame, HIDDEN_INFOS_SELECT_INDEX_KEY, indexToSelect);
                            step.selectTab = false;  // do not select current tab, because we already handled other action: close or unpin
                        }
                    };
                    MouseListener[] listeners = list.getMouseListeners();
                    MouseMotionListener[] motionListeners = list.getMouseMotionListeners();
                    Arrays.stream(listeners).forEach(list::removeMouseListener);
                    Arrays.stream(motionListeners).forEach(list::removeMouseMotionListener);
                    list.addMouseListener(listMouseListener);
                    list.addMouseMotionListener(listMouseListener);
                    Arrays.stream(listeners).forEach(list::addMouseListener);
                    Arrays.stream(motionListeners).forEach(list::addMouseMotionListener);
                }
            };
        });
        return popup;
    }

    public @NotNull ListStackImpl createListStack(@NotNull Project project,
                                                  @NotNull ListStackStep step,
                                                  @NotNull Function<? super ListCellRenderer, ? extends ListCellRenderer> cellRendererProducer) {
        return new ListStackImpl(project, step) {
            @Override
            protected ListCellRenderer<?> getListElementRenderer() {
                return cellRendererProducer.apply(super.getListElementRenderer());
            }
        };
    }
}
