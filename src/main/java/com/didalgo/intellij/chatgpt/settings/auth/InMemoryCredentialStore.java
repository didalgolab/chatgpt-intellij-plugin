/*
 * Copyright (c) 2024 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.settings.auth;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryCredentialStore implements CredentialStore {

    private final Map<String, String> credentials = new ConcurrentHashMap<>();

    @Override
    public String getPassword(String key) {
        return credentials.get(key);
    }

    @Override
    public String setAndGetPassword(String key, String password) {
        credentials.put(key, password);
        return password;
    }
}
