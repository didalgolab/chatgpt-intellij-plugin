/*
 * Copyright (c) 2024 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.ui;

import com.intellij.openapi.project.Project;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public class ChatGptPluginTestCase {

    protected CodeInsightTestFixture myFixture;
    protected Project project;

    @BeforeEach
    public void setUp() throws Exception {
        var fixtureBuilder = IdeaTestFixtureFactory.getFixtureFactory().createFixtureBuilder(getClass().getSimpleName());
        myFixture = IdeaTestFixtureFactory.getFixtureFactory().createCodeInsightFixture(fixtureBuilder.getFixture());
        myFixture.setUp();
        project = myFixture.getProject();
    }

    @AfterEach
    public void tearDown() throws Exception {
        myFixture.tearDown();
    }

    public Project getProject() {
        return myFixture.getProject();
    }
}
