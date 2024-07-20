/*
 * Copyright (c) 2024 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.chat.messages;

import org.springframework.ai.model.Media;
import org.springframework.util.MimeTypeUtils;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

public class MediaSupport {

    public static Media fromRenderedImageAsCompressedMedia(RenderedImage image) {
        var png = imageToPng(image);
        var jpeg = imageToJpeg(image);

        if (png.size() > jpeg.size()) {
            return new MediaHandle(MimeTypeUtils.IMAGE_JPEG, jpeg.toByteArray());
        } else {
            return new MediaHandle(MimeTypeUtils.IMAGE_PNG, png.toByteArray());
        }
    }

    private static ByteArrayOutputStream imageToPng(RenderedImage image) {
        try {
            var pngOutputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "png", pngOutputStream);
            return pngOutputStream;
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static ByteArrayOutputStream imageToJpeg(RenderedImage image) {
        try {
            var jpgOutputStream = new ByteArrayOutputStream();
            var jpgWriter = ImageIO.getImageWritersByFormatName("jpg").next();
            var jpgWriteParam = jpgWriter.getDefaultWriteParam();
            jpgWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            jpgWriteParam.setCompressionQuality(0.9f);

            jpgWriter.setOutput(new MemoryCacheImageOutputStream(jpgOutputStream));
            var outputImage = new IIOImage(removeAlphaChannel(image), null, null);
            jpgWriter.write(null, outputImage, jpgWriteParam);
            jpgWriter.dispose();
            return jpgOutputStream;
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static RenderedImage removeAlphaChannel(RenderedImage image) {
        if (!image.getColorModel().hasAlpha()) {
            return image;
        }

        var rgbImage = new BufferedImage(
                image.getWidth(),
                image.getHeight(),
                BufferedImage.TYPE_INT_RGB // No alpha
        );
        Graphics2D g = rgbImage.createGraphics();
        g.drawRenderedImage(image, new AffineTransform());
        g.dispose();
        return rgbImage;
    }

    public static BufferedImage resizeImage(BufferedImage image, int maxWidth, int maxHeight) {
        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();

        double oversize = Math.max((double) imageWidth / maxWidth, (double) imageHeight / maxHeight);
        if (oversize <= 1.0) {
            return image;
        }

        int newWidth = (int) (imageWidth / oversize);
        int newHeight = (int) (imageHeight / oversize);
        Image resizedImage = image.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
        BufferedImage resizedBufferedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);

        Graphics2D g2d = resizedBufferedImage.createGraphics();
        g2d.drawImage(resizedImage, 0, 0, null);
        g2d.dispose();
        return resizedBufferedImage;
    }
}
