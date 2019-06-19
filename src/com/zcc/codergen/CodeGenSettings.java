package com.zcc.codergen;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.zcc.codergen.util.CodeTemplate;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 进行持久化状态
 */
//@State(name = "CodeGenSettings", storages = {@Storage(value = "app-default", file = "$APP_CONFIG$/CodeGen-settings.xml")})
@State(name = "CodeGenSettings", storages = {@Storage("$APP_CONFIG$/CodeGen-settings.xml")})
public class CodeGenSettings implements PersistentStateComponent<CodeGenSettings> {

    /**
     * The constant LOGGER.
     */
    private static final Logger LOGGER = Logger.getInstance(CodeGenSettings.class);

    public CodeGenSettings() {
    }

    private void loadDefaultSettings() {
        try {
            Map<String, CodeTemplate> codeTemplates = new HashMap<>();
            codeTemplates.put("Model",
                    createCodeTemplate("Model.vm",
                            "${class0.className}Model", 1, CodeTemplate.DEFAULT_ENCODING));
            this.codeTemplates = codeTemplates;
        } catch (Exception e) {
            LOGGER.error("loadDefaultSettings failed", e);
        }
    }

    @NotNull
    private CodeTemplate createCodeTemplate(String sourceTemplateName, String classNameVm, int classNumber, String fileEncoding) throws IOException {
        String velocityTemplate = FileUtil.loadTextAndClose(CodeGenSettings.class.getResourceAsStream("/template/" + sourceTemplateName));
        return new CodeTemplate(sourceTemplateName,
                classNameVm, velocityTemplate, classNumber, fileEncoding);
    }

    /**
     * Getter method for property <tt>codeTemplates</tt>.
     *
     * @return property value of codeTemplates
     */
    public Map<String, CodeTemplate> getCodeTemplates() {
        if (codeTemplates == null) {
            loadDefaultSettings();
        }
        return codeTemplates;
    }

    @Setter
    private Map<String, CodeTemplate> codeTemplates;

    @Nullable
    @Override
    public CodeGenSettings getState() {
        if (this.codeTemplates == null) {
            loadDefaultSettings();
        }
        return this;
    }

    @Override
    public void loadState(CodeGenSettings codeMakerSettings) {
        XmlSerializerUtil.copyBean(codeMakerSettings, this);
    }

    public CodeTemplate getCodeTemplate(String template) {
        return codeTemplates.get(template);
    }

    public void removeCodeTemplate(String template) {
        codeTemplates.remove(template);
    }

}
