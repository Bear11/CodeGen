package com.zcc.codergen;

import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.components.ApplicationComponent;

/**
 * @author zcc
 */
public class CodeGen implements ApplicationComponent {
    public CodeGen() {
    }

    @Override
    public void initComponent() {
    }

    @Override
    public void disposeComponent() {
    }

    @Override
    @NotNull
    public String getComponentName() {
        return "com.zcc.codergen.CodeGen";
    }
}
