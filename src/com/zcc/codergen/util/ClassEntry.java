package com.zcc.codergen.util;

import com.intellij.psi.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Collections;
import java.util.List;

/**
 * 类实体模板类
 */
//@Data
//@AllArgsConstructor
public class ClassEntry {

    private String className;

    private String packageName;

    private List<String> importList;

    private List<Field> fields;

    private List<Field> allFields;

    private List<Method> methods;

    private List<Method> allMethods;

    private List<String> typeParams = Collections.emptyList();

    //@Data
    //@AllArgsConstructor
    public static class Method {
        /**
         * method name
         */
        private String name;

        /**
         * the method modifier, like "private",or "@Setter private" if include annotations
         */
        private String modifier;

        /**
         * the method returnType
         */
        private String returnType;

        /**
         * the method params, like "(String name)"
         */
        private String params;

        public Method(String name, String modifier, String returnType, String params) {
            this.name = name;
            this.modifier = modifier;
            this.returnType = returnType;
            this.params = params;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getModifier() {
            return modifier;
        }

        public void setModifier(String modifier) {
            this.modifier = modifier;
        }

        public String getReturnType() {
            return returnType;
        }

        public void setReturnType(String returnType) {
            this.returnType = returnType;
        }

        public String getParams() {
            return params;
        }

        public void setParams(String params) {
            this.params = params;
        }
    }

    //@Data
    //@AllArgsConstructor
    public static class Field {
        /**
         * field type
         */
        private String type;

        /**
         * field name
         */
        private String name;

        /**
         * the field modifier, like "private",or "@Setter private" if include annotations
         */
        private String modifier;

        /**
         * field doc comment
         */
        private String comment;

        public Field(String type, String name, String modifier, String comment) {
            this.type = type;
            this.name = name;
            this.modifier = modifier;
            this.comment = comment;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getModifier() {
            return modifier;
        }

        public void setModifier(String modifier) {
            this.modifier = modifier;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }
    }

    private ClassEntry() {

    }

    public ClassEntry(String className, String packageName, List<String> importList, List<Field> fields, List<Field> allFields, List<Method> methods, List<Method> allMethods, List<String> typeParams) {
        this.className = className;
        this.packageName = packageName;
        this.importList = importList;
        this.fields = fields;
        this.allFields = allFields;
        this.methods = methods;
        this.allMethods = allMethods;
        this.typeParams = typeParams;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public List<String> getImportList() {
        return importList;
    }

    public void setImportList(List<String> importList) {
        this.importList = importList;
    }

    public List<Field> getFields() {
        return fields;
    }

    public void setFields(List<Field> fields) {
        this.fields = fields;
    }

    public List<Field> getAllFields() {
        return allFields;
    }

    public void setAllFields(List<Field> allFields) {
        this.allFields = allFields;
    }

    public List<Method> getMethods() {
        return methods;
    }

    public void setMethods(List<Method> methods) {
        this.methods = methods;
    }

    public List<Method> getAllMethods() {
        return allMethods;
    }

    public void setAllMethods(List<Method> allMethods) {
        this.allMethods = allMethods;
    }

    public List<String> getTypeParams() {
        return typeParams;
    }

    public void setTypeParams(List<String> typeParams) {
        this.typeParams = typeParams;
    }

    /**
     * 类的创建过程
     * @param psiClass
     * @param filePath
     * @return
     */
    public static ClassEntry create(PsiClass psiClass, String filePath, String prefix, String suffix) {
        PsiFile psiFile = psiClass.getContainingFile();
        ClassEntry classEntry = new ClassEntry();
        classEntry.setClassName(prefix+psiClass.getName()+suffix);
        classEntry.setPackageName(((PsiClassOwner)psiFile).getPackageName());
        classEntry.setPackageName(filePath);
        if(psiFile instanceof PsiJavaFile)
        {
            classEntry.setFields(CodeGenUtil.getFields(psiClass));
            classEntry.setImportList(CodeGenUtil.getImportList((PsiJavaFile) psiFile));
            //classEntry.setAllFields(CodeGenUtil.getAllFields(psiClass));
        }
//        classEntry.setMethods(CodeGenUtil.getMethods(psiClass));
//        classEntry.setAllMethods(CodeGenUtil.getAllMethods(psiClass));
//        classEntry.setTypeParams(CodeGenUtil.getClassTypeParameters(psiClass));
        return classEntry;
    }

}
