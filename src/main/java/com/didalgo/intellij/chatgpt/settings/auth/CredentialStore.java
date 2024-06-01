/*
 * Copyright (c) 2024 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.settings.auth;

public interface CredentialStore {

    String getPassword(String key);

    String setAndGetPassword(String key, String password);

    static CredentialStore systemCredentialStore() {
        return SystemCredentialStore.INSTANCE;
    }
}
