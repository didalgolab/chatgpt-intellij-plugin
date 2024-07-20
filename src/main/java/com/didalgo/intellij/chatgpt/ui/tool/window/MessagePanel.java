/*
 * Copyright (c) 2024 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.ui.tool.window;

import com.didalgo.intellij.chatgpt.text.TextFragment;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.panels.VerticalLayout;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.model.Media;
import org.springframework.ai.model.MediaContent;

import javax.swing.*;
import java.util.List;
import java.util.Optional;

public class MessagePanel extends JBPanel<MessagePanel> {

    private final MessageTextPanel textPanel;


    public MessagePanel(Message message, MessageTextPanel textPanel) {
        super(new VerticalLayout(0));
        this.textPanel = textPanel;
        setOpaque(false);
        createImageListPanel(message).ifPresent(this::add);
        add(textPanel);
    }

    protected Optional<JComponent> createImageListPanel(Message message) {
        List<Media> images = (message instanceof MediaContent content)
                ? content.getMedia().stream()
                        .filter(media -> "image".equals(media.getMimeType().getType()))
                        .toList()
                : List.of();
        return images.isEmpty() ? Optional.empty() : Optional.of(new CollapsibleImagePanel(images));
    }

    public void updateTextContent(TextFragment newContent) {
        textPanel.updateMessage(newContent);
    }
}
