/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.chat;

public interface ChatMessageListener {

    void exchangeStarting(ChatMessageEvent.Starting event) throws ChatExchangeAbortException;

    void exchangeStarted(ChatMessageEvent.Started event);

    void responseArriving(ChatMessageEvent.ResponseArriving event);

    void responseArrived(ChatMessageEvent.ResponseArrived event);

    void exchangeFailed(ChatMessageEvent.Failed event);

    void exchangeCancelled(ChatMessageEvent.Cancelled event);

}
