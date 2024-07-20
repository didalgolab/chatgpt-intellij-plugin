/*
 * Copyright (c) 2024 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.chat;

import com.didalgo.intellij.chatgpt.text.TextContent;
import org.springframework.ai.model.Media;

import javax.swing.*;
import java.util.Optional;
import java.util.function.ToIntFunction;

public interface PromptAttachment {

    Optional<TextContent> getTextContentIfPresent();

    Optional<Media> getMediaContentIfPresent();

    Icon getIcon();

    String getName();

    boolean isPinned();

    void setPinned(boolean pinned);

    int getEstimatedTokenCount(ToIntFunction<? super PromptAttachment> estimator);

}
