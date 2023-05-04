/* Copyright (c) 2023 Mariusz Bernacki <didalgo@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0 */
package com.didalgo.intellij.chatgpt.util;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class ImgUtils {

    public static Image iconToImage(Icon icon) {
        if (icon instanceof ImageIcon) {
            return ((ImageIcon)icon).getImage();
        } else {
            int w = icon.getIconWidth();
            int h = icon.getIconHeight();
            GraphicsEnvironment ge =
                    GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice gd = ge.getDefaultScreenDevice();
            GraphicsConfiguration gc = gd.getDefaultConfiguration();
            BufferedImage image = gc.createCompatibleImage(w, h);
            Graphics2D g = image.createGraphics();
            icon.paintIcon(null, g, 0, 0);
            g.dispose();
            return image;
        }
    }
}
