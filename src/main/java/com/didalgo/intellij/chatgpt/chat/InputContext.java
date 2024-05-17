/*
 * Copyright (c) 2024 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.chat;

import java.util.List;

public interface InputContext {

    void addListener(InputContextListener listener);

    void removeListener(InputContextListener listener);

    void addAttachment(PromptAttachment attachment);

    void removeAttachment(PromptAttachment attachment);

    List<PromptAttachment> getAttachments();

    boolean isEmpty();

    void clear();

}
