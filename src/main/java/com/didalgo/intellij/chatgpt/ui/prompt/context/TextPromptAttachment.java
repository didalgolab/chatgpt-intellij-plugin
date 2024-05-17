/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.ui.prompt.context;

import com.didalgo.intellij.chatgpt.chat.PromptAttachment;
import com.didalgo.intellij.chatgpt.text.TextContent;
import org.springframework.ai.chat.messages.Media;

import javax.swing.*;
import java.util.Optional;
import java.util.function.ToIntFunction;

public class TextPromptAttachment extends AbstractPromptAttachment {
    private final TextContent content;


    public TextPromptAttachment(Icon icon, String name, TextContent content) {
        super(icon, name);
        this.content = content;
    }

    @Override
    public Optional<TextContent> getTextContentIfPresent() {
        return Optional.of(content);
    }

    @Override
    public Optional<Media> getMediaContentIfPresent() {
        return Optional.empty();
    }

    @Override
    protected int estimateTokenCount(ToIntFunction<? super PromptAttachment> estimator) {
        return estimator.applyAsInt(this);
    }
}
