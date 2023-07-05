/*
 * Copyright 2000-2020 JetBrains s.r.o.
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.ui.context.stack;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.keymap.KeymapUtil;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.UserDataHolder;
import com.intellij.ui.*;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ui.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.accessibility.AccessibleContext;
import javax.swing.*;
import java.awt.*;
import java.util.Collections;

public class StackListElementRenderer<E> extends GroupedItemsListRenderer<E> {

    public static final Key<String> CUSTOM_KEY_STROKE_TEXT = new Key<>("CUSTOM_KEY_STROKE_TEXT");
    protected final ListStackImpl myPopup;
    private JLabel myShortcutLabel;
    private @Nullable JLabel myValueLabel;
    protected JLabel myMnemonicLabel;
    protected JLabel myIconLabel;

    protected JPanel myButtonPane;
    protected JComponent myMainPane;
    protected JComponent myButtonsSeparator;
    protected JComponent myIconBar;

    private final NonActionsStackInlineSupport myInlineActionsSupport;

    public StackListElementRenderer(ListStackImpl aStack) {
        super(new ListItemDescriptorAdapter<>() {
            @Override
            public String getTextFor(E value) {
                return aStack.getListStep().getTextFor(value);
            }

            @Override
            public Icon getIconFor(E value) {
                return aStack.getListStep().getIconFor(value);
            }

            @Override
            public Icon getSelectedIconFor(E value) {
                return aStack.getListStep().getSelectedIconFor(value);
            }

            @Override
            public boolean hasSeparatorAboveOf(E value) {
                return aStack.getListModel().isSeparatorAboveOf(value);
            }

            @Override
            public String getCaptionAboveOf(E value) {
                return aStack.getListModel().getCaptionAboveOf(value);
            }

            @Nullable
            @Override
            public String getTooltipFor(E value) {
                ListStackStep<Object> listStep = aStack.getListStep();
                return listStep.getTooltipTextFor(value);
            }
        });
        myPopup = aStack;
        myInlineActionsSupport = new NonActionsStackInlineSupport(myPopup);
    }

    public ListStackImpl getPopup() {
        return myPopup;
    }

    @Override
    protected SeparatorWithText createSeparator() {
        Insets labelInsets = getDefaultItemComponentBorder().getBorderInsets(new JLabel());
        return new GroupHeaderSeparator(labelInsets);
    }

    @Override
    protected Color getBackground() {
        return super.getBackground();
    }

    @Override
    protected JComponent createItemComponent() {
        createLabel();
        JPanel panel = new JPanel(new BorderLayout()) {
            private final AccessibleContext myAccessibleContext = myTextLabel.getAccessibleContext();

            @Override
            public AccessibleContext getAccessibleContext() {
                if (myAccessibleContext == null) {
                    return super.getAccessibleContext();
                }
                return myAccessibleContext;
            }
        };
        panel.add(myTextLabel, BorderLayout.WEST);

        myValueLabel = new JLabel();
        myValueLabel.setEnabled(false);
        JBEmptyBorder valueBorder = JBUI.Borders.empty(0, 8, 1, 0);
        myValueLabel.setBorder(valueBorder);
        myValueLabel.setForeground(UIManager.getColor("MenuItem.acceleratorForeground"));
        panel.add(myValueLabel, BorderLayout.CENTER);

        myShortcutLabel = new JLabel();
        JBEmptyBorder shortcutBorder = JBUI.Borders.empty(0,0,1,3);
        myShortcutLabel.setBorder(shortcutBorder);
        myShortcutLabel.setForeground(UIManager.getColor("MenuItem.acceleratorForeground"));
        panel.add(myShortcutLabel, BorderLayout.EAST);

        myMnemonicLabel = new JLabel();
        myMnemonicLabel.setFont(JBUI.CurrentTheme.ActionsList.applyStylesForNumberMnemonic(myMnemonicLabel.getFont()));

        Insets insets = JBUI.CurrentTheme.ActionsList.numberMnemonicInsets();
        myMnemonicLabel.setBorder(new JBEmptyBorder(insets));
        //noinspection HardCodedStringLiteral
        Dimension preferredSize = new JLabel("W").getPreferredSize();
        JBInsets.addTo(preferredSize, insets);
        myMnemonicLabel.setPreferredSize(preferredSize);

        myMnemonicLabel.setVisible(false);

        myIconBar = createIconBar();

        return layoutComponent(panel);
    }

    @Override
    protected void createLabel() {
        super.createLabel();
        myIconLabel = new JLabel();
    }

    @Override
    protected JComponent layoutComponent(JComponent middleItemComponent) {
        myNextStepLabel = new JLabel();

        JPanel left = new JPanel(new BorderLayout());
        left.add(middleItemComponent, BorderLayout.CENTER);

        JPanel right = new JPanel(new GridBagLayout());

        myButtonsSeparator = createButtonsSeparator();
        left.add(myButtonsSeparator, BorderLayout.EAST);

        if (myIconBar != null) {
            left.add(myIconBar, BorderLayout.WEST);
        }

        JPanel result;
        result = new JPanel();
        result.setBorder(JBUI.Borders.empty());
        result.setLayout(new GridBagLayout());

        Insets insets = getDefaultItemComponentBorder().getBorderInsets(result);
        int leftRightInset = (ListStackImpl.NEXT_STEP_AREA_WIDTH - AllIcons.Icons.Ide.MenuArrow.getIconWidth()) / 2;
        left.setBorder(JBUI.Borders.empty(insets.top, insets.left, insets.bottom, 0));
        right.setBorder(JBUI.Borders.empty(insets.top, leftRightInset, insets.bottom, insets.right));

        GridBag gbc = new GridBag()
                .setDefaultAnchor(0, GridBagConstraints.WEST)
                .setDefaultWeightX(0, 1)
                .setDefaultAnchor(GridBagConstraints.CENTER)
                .setDefaultWeightX(0)
                .setDefaultWeightY(1)
                .setDefaultPaddingX(0)
                .setDefaultPaddingY(0)
                .setDefaultInsets(0, 0, 0, 0)
                .setDefaultFill(GridBagConstraints.BOTH);

        result.add(left, gbc.next());
        result.add(right, gbc.next());

        myMainPane = left;
        myButtonPane = right;

        return result;
    }

    @Override
    protected void setComponentIcon(Icon icon, Icon disabledIcon) {
        if (myIconLabel == null) return;
        myIconLabel.setIcon(icon);
        myIconLabel.setDisabledIcon(disabledIcon);
    }

    @NotNull
    protected static JComponent createButtonsSeparator() {
        SeparatorComponent separator = new SeparatorComponent(JBUI.CurrentTheme.List.buttonSeparatorColor(), SeparatorOrientation.VERTICAL);
        separator.setHGap(1);
        separator.setVGap(JBUI.CurrentTheme.List.buttonSeparatorInset());
        return separator;
    }

    @Override
    protected void customizeComponent(JList<? extends E> list, E value, boolean isSelected) {
        if (mySeparatorComponent.isVisible() && mySeparatorComponent instanceof GroupHeaderSeparator) {
            ((GroupHeaderSeparator)mySeparatorComponent).setHideLine(myCurrentIndex == 0);
        }

        ListStackStep<Object> step = myPopup.getListStep();
        boolean isSelectable = step.isSelectable(value);
        myTextLabel.setEnabled(isSelectable);

        myMainPane.setOpaque(false);
        myButtonPane.setOpaque(false);


        boolean nextStepButtonSelected = false;
        boolean showNextStepLabel = step.hasSubStep(value) && !myInlineActionsSupport.hasExtraButtons(value);
        if (showNextStepLabel) {
            myNextStepLabel.setVisible(isSelectable);
            myNextStepLabel.setIcon(isSelectable && isSelected ? AllIcons.Icons.Ide.MenuArrowSelected : AllIcons.Icons.Ide.MenuArrow);
            getItemComponent().setBackground(calcBackground(isSelected && isSelectable));
            setForegroundSelected(myTextLabel, isSelected && isSelectable);
        }
        else {
            myNextStepLabel.setVisible(false);
        }

        boolean hasNextIcon = myNextStepLabel.isVisible();
        boolean hasInlineButtons = updateExtraButtons(list, value, step, isSelected, hasNextIcon);

        if (step instanceof BaseListStackStep) {
            Color bg = ((BaseListStackStep<E>)step).getBackgroundFor(value);
            Color fg = ((BaseListStackStep<E>)step).getForegroundFor(value);
            if (!isSelected && fg != null) myTextLabel.setForeground(fg);
            if (!isSelected && bg != null) UIUtil.setBackgroundRecursively(getItemComponent(), bg);
            if (bg != null && mySeparatorComponent.isVisible() && myCurrentIndex > 0) {
                E prevValue = list.getModel().getElementAt(myCurrentIndex - 1);
                // separator between 2 colored items shall get color too
                if (Comparing.equal(bg, ((BaseListStackStep<E>)step).getBackgroundFor(prevValue))) {
                    myRendererComponent.setBackground(bg);
                }
            }
        }

        myTextLabel.setDisplayedMnemonicIndex(-1);

        if (myShortcutLabel != null) {
            myShortcutLabel.setEnabled(isSelectable);
            myShortcutLabel.setText("");
            if (value instanceof ShortcutProvider) {
                ShortcutSet set = ((ShortcutProvider)value).getShortcut();
                String shortcutText = null;
                if (set != null) {
                    Shortcut shortcut = ArrayUtil.getFirstElement(set.getShortcuts());
                    if (shortcut != null) {
                        shortcutText = KeymapUtil.getShortcutText(shortcut);
                    }
                }
                if (shortcutText == null && value instanceof AnActionHolder) {
                    AnAction action = ((AnActionHolder)value).getAction();
                    if (action instanceof UserDataHolder) {
                        shortcutText = ((UserDataHolder)action).getUserData(CUSTOM_KEY_STROKE_TEXT);
                    }
                }
                if (shortcutText != null) {
                    myShortcutLabel.setText("     " + shortcutText);
                }
            }
            myShortcutLabel.setForeground(isSelected && isSelectable && !nextStepButtonSelected
                    ? UIManager.getColor("MenuItem.acceleratorSelectionForeground")
                    : UIManager.getColor("MenuItem.acceleratorForeground"));
        }

        if (myValueLabel != null) {
            String valueLabelText = step.getValueFor(value);
            myValueLabel.setText(valueLabelText);
            boolean selected = isSelected && isSelectable && !nextStepButtonSelected;
            setForegroundSelected(myValueLabel, selected);
        }
    }

    private boolean updateExtraButtons(JList<? extends E> list, E value, ListStackStep<Object> step, boolean isSelected, boolean hasNextIcon) {
        myButtonPane.removeAll();
        GridBag gb = new GridBag().setDefaultFill(GridBagConstraints.BOTH)
                .setDefaultAnchor(GridBagConstraints.CENTER)
                .setDefaultWeightX(1.0)
                .setDefaultWeightY(1.0);

        boolean isSelectable = step.isSelectable(value);
        java.util.List<JComponent> extraButtons;
        if (!isSelected || !isSelectable) {
            extraButtons = Collections.emptyList();
        } else {
            extraButtons = myInlineActionsSupport.getExtraButtons(list, value, true);
        }

        if (!extraButtons.isEmpty()) {
            myButtonsSeparator.setVisible(true);
            extraButtons.forEach(comp -> myButtonPane.add(comp, gb.next()));
            Integer activeButtonIndex = myInlineActionsSupport.getActiveButtonIndex(list);
            // We ONLY need to update the tooltip if there's an active inline action button.
            // Otherwise, it's set earlier from the main action.
            // If there is an active button without a tooltip, we still need to set the tooltip
            // to null, otherwise it'll look ugly, as if the inline action button has the same
            // tooltip as the main action.
            if (activeButtonIndex != null) {
                myRendererComponent.setToolTipText(myInlineActionsSupport.getActiveExtraButtonToolTipText(list, value));
            }
        }
        else if (!hasNextIcon && myInlineActionsSupport.hasExtraButtons(value)){
            myButtonsSeparator.setVisible(false);
            myButtonPane.add(Box.createHorizontalStrut(NonActionsStackInlineSupport.buttonWidth()), gb.next());
        }
        else {
            myButtonsSeparator.setVisible(false);
            myButtonPane.add(myNextStepLabel, gb.next());
        }

        return !extraButtons.isEmpty();
    }

    protected JComponent createIconBar() {
        Box res = Box.createHorizontalBox();
        res.add(myIconLabel);

        res.setBorder(JBUI.Borders.emptyRight(JBUI.CurrentTheme.ActionsList.elementIconGap()));
        res.add(myMnemonicLabel);

        return res;
    }

    private Color calcBackground(boolean selected) {
        return selected ? getSelectionBackground() : getBackground();
    }

    @NotNull
    static Insets getListCellPadding() {
        return UIUtil.getListCellPadding();
    }

    public JComponent getItemComponent() {
        return myComponent;
    }
}
