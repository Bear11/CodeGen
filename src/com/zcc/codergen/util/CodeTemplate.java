package com.zcc.codergen.util;

import com.intellij.openapi.util.text.StringUtil;

/**
 * 通用代码模板
 */
public class CodeTemplate {

    public static final String DEFAULT_ENCODING = "UTF-8";

    public CodeTemplate() {}

    /**
     * template name
     */
    private String name;

    /**
     * the generated class name, support velocity
     */
    private String classNameVm;

    /**
     * code template in velocity
     */
    private String codeTemplate;

    /**
     * the number of template context class
     */
    private int classNumber;

    /**
     * the encoding of the generated file
     */
    private String fileEncoding;

    public boolean isValid() {
        return StringUtil.isNotEmpty(getClassNameVm()) && StringUtil.isNotEmpty(getName())
                && StringUtil.isNotEmpty(getCodeTemplate()) && classNumber != -1 && StringUtil.isNotEmpty(getFileEncoding());
    }

    public static final CodeTemplate EMPTY_TEMPLATE = new CodeTemplate("", "", "", 1, DEFAULT_ENCODING);

    public CodeTemplate(String name, String classNameVm, String codeTemplate, int classNumber, String fileEncoding) {
        this.name = name;
        this.classNameVm = classNameVm;
        this.codeTemplate = codeTemplate;
        this.classNumber = classNumber;
        this.fileEncoding = fileEncoding;
    }

    public static String getDefaultEncoding() {
        return DEFAULT_ENCODING;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClassNameVm() {
        return classNameVm;
    }

    public void setClassNameVm(String classNameVm) {
        this.classNameVm = classNameVm;
    }

    public String getCodeTemplate() {
        return codeTemplate;
    }

    public void setCodeTemplate(String codeTemplate) {
        this.codeTemplate = codeTemplate;
    }

    public int getClassNumber() {
        return classNumber;
    }

    public void setClassNumber(int classNumber) {
        this.classNumber = classNumber;
    }

    public String getFileEncoding() {
        return fileEncoding;
    }

    public void setFileEncoding(String fileEncoding) {
        this.fileEncoding = fileEncoding;
    }

    public static CodeTemplate getEmptyTemplate() {
        return EMPTY_TEMPLATE;
    }
}
