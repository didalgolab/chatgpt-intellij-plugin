/*
 * Copyright (c) 2024 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.ui;

import com.didalgo.intellij.chatgpt.chat.InputContext;
import com.didalgo.intellij.chatgpt.chat.messages.MediaSupport;
import com.didalgo.intellij.chatgpt.ui.prompt.context.MediaPromptAttachment;
import com.intellij.icons.AllIcons;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.RenderedImage;
import java.io.IOException;

public class InputContextPromptAttachmentHandler implements PromptAttachmentHandler {

    private final InputContext context;

    public InputContextPromptAttachmentHandler(InputContext context) {
        this.context = context;
    }


    @Override
    public boolean handleTransferable(Transferable content) {
        try {
            if (content.isDataFlavorSupported(DataFlavor.imageFlavor) && content.getTransferData(DataFlavor.imageFlavor) instanceof RenderedImage image)
                return handleImageContent(image);

        } catch (IOException | UnsupportedFlavorException ignored) {
        }
        return false;
    }

    protected boolean handleImageContent(RenderedImage image) {
        var icon = AllIcons.Actions.AddFile;
        var attachment = new MediaPromptAttachment(icon, "Image", MediaSupport.fromRenderedImageAsCompressedMedia(image));
        context.addAttachment(attachment);
        return true;
    }
}
