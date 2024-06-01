/*
 * Copyright (c) 2024 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt;

import org.springframework.web.reactive.function.client.WebClientResponseException;

import static org.apache.commons.lang3.StringUtils.isEmpty;

public final class Errors {

    public static String getWebClientErrorMessage(Throwable cause) {
        String errorMessage = getErrorMessage(cause);
        return errorMessage + (errorMessage.isEmpty() ? "" : "\n\n") + getErrorResponseBody(cause);
    }

    private static String getErrorResponseBody(Throwable cause) {
        var restEx = (cause instanceof WebClientResponseException wcre) ? wcre
                : (cause.getCause() instanceof WebClientResponseException wcre) ? wcre : null;
        return (restEx != null) ? restEx.getResponseBodyAsString() : "";
    }

    private static String getErrorMessage(Throwable cause) {
        if (cause == null)
            return "";

        var errorMessage = isEmpty(cause.getMessage()) ? "" : cause.getMessage();
        var causeMessage = getErrorMessage(cause.getCause());
        if (errorMessage.isEmpty() || causeMessage.contains(errorMessage))
            return causeMessage;
        else
            return errorMessage;
    }

}
