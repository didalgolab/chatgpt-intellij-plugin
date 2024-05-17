/*
 * Copyright (c) 2024 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.chat.messages;

import org.springframework.ai.chat.messages.Media;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.util.MimeType;

import java.io.IOException;
import java.io.UncheckedIOException;

public class MediaHandle extends Media {

    private final Resource resource;


    public MediaHandle(MimeType mimeType, byte[] bytes) {
        this(mimeType, new ByteArrayResource(bytes));
    }

    public MediaHandle(MimeType mimeType, Resource resource) {
        super(mimeType, new ByteArrayResource(new byte[0]));
        this.resource = resource;
    }

    @Override
    public Object getData() {
        try {
            return resource.getContentAsByteArray();
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
