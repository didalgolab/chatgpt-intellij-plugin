/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.text.encryption;

import javax.crypto.spec.SecretKeySpec;
import javax.crypto.Cipher;
import java.util.Base64;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Provides encryption and decryption of text using the AES algorithm.
 * <p>
 * To use this class, call the static {@link #encrypt(String)} method to encrypt plain text
 * and the {@link #decrypt(String)} method to decrypt previously encrypted text. The encrypted
 * output will be prefixed with "{AES}", which is used as a marker to indicate that the output
 * text has been encrypted using this class.
 * <p>
 * Note that to simplify its usage this class uses a hard-coded encryption key, which is not
 * considered very secure. For more secure usage, consider using an externally generated
 * encryption key or implementing a more secure key generation algorithm.
 *
 * @author Mariusz Bernacki
 *
 */
public class AES {
    private static final String ALGORITHM = "AES";
    private static final String ALGORITHM_MARK = "{AES}";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";
    private static final SecretKeySpec SECRET_KEY = new SecretKeySpec(new byte[] {
            42, -16, 90, -13, -4, 71, -21, 55, 9, 25, 12, -71, 34, -83, 91, -117,
            41, -126, 26, -41, 46, -38, 75, 1, -36, -90, 110, 58, -41, -126, -122, -51
    }, ALGORITHM);

    public static String encrypt(String plain) {
        if (plain.isEmpty())
            return plain;

        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, SECRET_KEY);
            return ALGORITHM_MARK + Base64.getEncoder().encodeToString(cipher.doFinal(plain.getBytes(UTF_8)));
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    public static String decrypt(String encrypted) {
        if (!encrypted.startsWith(ALGORITHM_MARK))
            return encrypted;

        try {
            encrypted = encrypted.substring(ALGORITHM_MARK.length());
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, SECRET_KEY);
            byte[] decryptedData = cipher.doFinal(Base64.getDecoder().decode(encrypted));
            return new String(decryptedData, UTF_8);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }
}