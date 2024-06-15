/*
 * Copyright (c) 2024 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt;

import com.didalgo.intellij.chatgpt.ui.GUIKit;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.MessageType;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

public class ParallelOptions {

    public static void executeOnActionOnPooledThread(JComponent actionSource, Runnable targetAction) {
        executeOnActionOnPooledThread(actionSource, targetAction, () -> {});
    }

    public static void executeOnActionOnPooledThread(
            JComponent actionSource,
            Runnable targetAction,
            Runnable edtActionOnSuccess) {

        actionSource.setEnabled(false);
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            try {
                targetAction.run();
                SwingUtilities.invokeLater(() -> {
                    actionSource.setEnabled(true);
                    edtActionOnSuccess.run();
                });
            }
            catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    actionSource.setEnabled(true);
                    GUIKit.showCallout(actionSource, Errors.getWebClientErrorMessage(e), MessageType.ERROR);
                });
            }
        });
    }
}
