/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.core;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurableEP;
import com.intellij.openapi.project.Project;
import com.didalgo.intellij.chatgpt.settings.ChatGptSettings;
import com.didalgo.intellij.chatgpt.ui.action.editor.ActionsUtil;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;

public class StartUpActivity implements StartupActivity {

    @Override
    public void runActivity(@NotNull Project project) {
        ChatGptSettings.getInstance();
        ActionsUtil.refreshActions();
        //Incubator.registerApplicationConfigurables();
    }

    static class Incubator {

        //         <applicationConfigurable parentId="com.didalgo.chatgpt.settings.OpenAISettings" instance="com.didalgo.intellij.chatgpt.settings.GPT4_Panel"
        //                                 id="com.didalgo.chatgpt.settings.GPT4"
        //                                 displayName="GPT 4"/>
        protected static void registerApplicationConfigurables() {
            IdeaPluginDescriptor pluginDesc = PluginManager.getInstance().findEnabledPlugin(PluginId.getId("com.didalgo.chatgpt"));
            ConfigurableEP<Configurable> bean = new ConfigurableEP<>(pluginDesc);
            bean.parentId = "com.didalgo.chatgpt.settings.OpenAISettings";
            bean.instanceClass = "com.didalgo.intellij.chatgpt.settings.GPT4o_Panel";
            bean.id = "com.didalgo.chatgpt.settings.GPT4o";
            bean.displayName = "GPT-4o";

            ApplicationManager.getApplication().getExtensionArea()
                    .getExtensionPoint(Configurable.APPLICATION_CONFIGURABLE)
                    .registerExtension(bean);

        }

    }
}
