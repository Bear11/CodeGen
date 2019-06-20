package com.zcc.codergen.ui;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.zcc.codergen.CodeGenSettings;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class CodeGenConfigurable implements SearchableConfigurable {

    private CodeGenSettings settings;

    private CodeGenForm configuration;

    public CodeGenConfigurable() {
        this.settings = ServiceManager.getService(CodeGenSettings.class);
    }

    @NotNull
    @Override
    public String getId() {
        return "plugins.codegen";
    }

    @Nullable
    @Override
    public Runnable enableSearch(String option) {
        return null;
    }

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "CodeGen";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        if (configuration == null) {
            configuration = new CodeGenForm();
        }
        return configuration.getMainPane();
    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public void apply() throws ConfigurationException {

    }
}
