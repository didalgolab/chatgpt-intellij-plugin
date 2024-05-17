/*
 * Copyright (c) 2024 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.ui;

import java.awt.datatransfer.Transferable;

public interface PromptAttachmentHandler {

    boolean handleTransferable(Transferable attachment);

}
