package com.zcc.codergen.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.intellij.lang.jvm.JvmModifier;
import com.intellij.psi.*;
//import org.apache.oro.text.regex.*;

import com.google.common.collect.Lists;
import com.intellij.ide.util.TreeClassChooser;
import com.intellij.ide.util.TreeClassChooserFactory;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilBase;

/**
 *
 */
public class CodeGenUtil {
    private static final Logger LOGGER = Logger.getInstance(CodeGenUtil.class);

    public static Logger getLogger(Class clazz) {
        return Logger.getInstance(clazz);
    }

    public static PsiClass chooseClass(Project project, PsiClass defaultClass) {
        TreeClassChooser chooser = TreeClassChooserFactory.getInstance(project)
            .createProjectScopeChooser("Select a class", defaultClass);

        chooser.showDialog();

        return chooser.getSelected();
    }

    public static String getSourcePath(PsiClass clazz) {
        PsiFile containingFile = clazz.getContainingFile();
        return getSourcePath(containingFile);
    }

    public static String getSourcePath(PsiFile psiFile) {
        String classPath = psiFile.getVirtualFile().getPath();
        return classPath.substring(0, classPath.lastIndexOf('/'));
    }

    public static String generateClassPath(String sourcePath, String className) {
        return generateClassPath(sourcePath, className, "java");
    }

    public static String generateClassPath(String sourcePath, String className, String extension) {
        return sourcePath + "/" + className + "." + extension;
    }

    public static List<String> getImportList(PsiJavaFile javaFile) {
        PsiImportList importList = javaFile.getImportList();
        if (importList == null) {
            return new ArrayList<>();
        }
        return Arrays.stream(importList.getImportStatements())
            .map(PsiImportStatement::getQualifiedName).collect(Collectors.toList());
    }


    public static List<ClassEntry.Field> getFields(PsiClass psiClass) {
        List<ClassEntry.Field> list =  Arrays.stream( psiClass.getFields())
            .filter(x->x.getModifierList()!=null
                    && !x.getModifierList().hasModifierProperty(JvmModifier.STATIC.name().toLowerCase())
                    && !x.getModifierList().hasModifierProperty(JvmModifier.FINAL.name().toLowerCase())
                    && !"head".equalsIgnoreCase(x.getName()))
            .map(psiField -> new ClassEntry.Field(psiField.getType().getPresentableText(),
                psiField.getName(),
                psiField.getModifierList() == null ? "" : filterModifierList(psiField),
                //psiField.getModifierList() == null ? "" : psiField.getModifierList().getText(),
                getDocComment(psiField)))
            .collect(Collectors.toList());
        return list;
    }

    public static String getDocCommentText(PsiField psiField) {
        if (psiField.getDocComment() == null) {
            return "";
        }
        StringBuilder content = new StringBuilder();
        for (PsiElement element : psiField.getDocComment().getDescriptionElements()) {
            content.append(element.getText());
        }
        return content.toString();
    }

    public static List<ClassEntry.Field> getAllFields(PsiClass psiClass) {
        return Arrays.stream(psiClass.getAllFields())
            .map(psiField -> new ClassEntry.Field(psiField.getType().getPresentableText(),
                psiField.getName(),
                psiField.getModifierList() == null ? "" : psiField.getModifierList().getText(),
                getDocCommentText(psiField)))
            .collect(Collectors.toList());
    }

    public static List<ClassEntry.Method> getMethods(PsiClass psiClass) {
        return Arrays.stream(psiClass.getMethods()).map(psiMethod -> {
            String returnType = psiMethod.getReturnType() == null ? ""
                : psiMethod.getReturnType().getPresentableText();
            return new ClassEntry.Method(psiMethod.getName(), psiMethod.getModifierList().getText(),
                returnType, psiMethod.getParameterList().getText());
        }).collect(Collectors.toList());
    }

    public static List<ClassEntry.Method> getAllMethods(PsiClass psiClass) {
        return Arrays.stream(psiClass.getAllMethods()).map(psiMethod -> {
            String returnType = psiMethod.getReturnType() == null ? ""
                : psiMethod.getReturnType().getPresentableText();
            return new ClassEntry.Method(psiMethod.getName(), psiMethod.getModifierList().getText(),
                returnType, psiMethod.getParameterList().getText());
        }).collect(Collectors.toList());
    }

    /**
     * find the method belong to name
     * @return null if not found
     */
    public static String findClassNameOfSuperMethod(PsiMethod psiMethod) {
        PsiMethod[] superMethods = psiMethod.findDeepestSuperMethods();
        if (superMethods.length == 0 || superMethods[0].getContainingClass() == null) {
            return null;
        }
        return superMethods[0].getContainingClass().getQualifiedName();
    }

    /**
     * Gets all classes in the element.
     *
     * @param element the Element
     * @return the Classes
     */
    public static List<PsiClass> getClasses(PsiElement element) {
        List<PsiClass> elements = Lists.newArrayList();
        List<PsiClass> classElements = PsiTreeUtil.getChildrenOfTypeAsList(element, PsiClass.class);
        elements.addAll(classElements);
        for (PsiClass classElement : classElements) {
            elements.addAll(getClasses(classElement));
        }
        return elements;
    }

    public static boolean isNumeric(String str) {
        if (str == null) {
            return false;
        }

        int length = str.length();

        for (int i = 0; i < length; i++) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    public static List<String> getClassTypeParameters(PsiClass psiClass) {
        return Arrays.stream(psiClass.getTypeParameters()).map(PsiNamedElement::getName)
            .collect(Collectors.toList());
    }

    public static List<Character> markdownChars = Lists.newArrayList('<', '>', '`', '*', '_', '{',
        '}', '[', ']', '(', ')', '#', '+', '-', '.', '!');

    public static String escapeMarkdown(String str) {
        StringBuilder result = new StringBuilder();
        for (char ch : str.toCharArray()) {
            if (markdownChars.contains(ch)) {
                result.append('\\');
            }
            result.append(ch);
        }
        return result.toString();
    }

    public static int findJavaDocTextOffset(PsiElement theElement) {
        PsiElement javadocElement = theElement.getFirstChild();
        if (!(javadocElement instanceof PsiDocComment)) {
            throw new IllegalStateException("Cannot find element of type PsiDocComment");
        }
        return javadocElement.getTextOffset();
    }

    public static int findJavaCodeTextOffset(PsiElement theElement) {
        if (theElement.getChildren().length < 2) {
            throw new IllegalStateException("Can not find offset of java code");
        }
        return theElement.getChildren()[1].getTextOffset();
    }

    /**
     * save the current change
     * @param element
     */
    public static void pushPostponedChanges(PsiElement element) {
        Editor editor = PsiUtilBase.findEditor(element.getContainingFile());
        if (editor != null) {
            PsiDocumentManager.getInstance(element.getProject())
                .doPostponedOperationsAndUnblockDocument(editor.getDocument());
        }
    }

    public static void reformatJavaDoc(PsiElement theElement) {
        CodeStyleManager codeStyleManager = CodeStyleManager.getInstance(theElement.getProject());
        try {
            int javadocTextOffset = findJavaDocTextOffset(theElement);
            int javaCodeTextOffset = findJavaCodeTextOffset(theElement);
            codeStyleManager.reformatText(theElement.getContainingFile(), javadocTextOffset,
                javaCodeTextOffset + 1);
        } catch (Exception e) {
            LOGGER.error("reformat code failed", e);
        }
    }

    public static void reformatJavaFile(PsiElement theElement) {
        CodeStyleManager codeStyleManager = CodeStyleManager.getInstance(theElement.getProject());
        try {
            codeStyleManager.reformat(theElement);
        } catch (Exception e) {
            LOGGER.error("reformat code failed", e);
        }
    }

    /**
     * 过滤类名
     * @param psiField
     * @return
     */
    public static String filterFiledName(PsiField psiField) {
        boolean isCustomClass = false;
        if (psiField != null) {
            isCustomClass = isJavaClass(psiField.getContainingClass().getClass());
        }
        if (!isCustomClass) {
            assert psiField != null;
            return Objects.requireNonNull(psiField.getName()).concat("Dto");
        }
        return psiField.getName();
    }
    public static String filterModifierList(PsiField psiField) {
        boolean bol = psiField.getModifierList().hasExplicitModifier("private");
        if (bol) {
            return JvmModifier.PRIVATE.name().toLowerCase();
        }
        bol = psiField.getModifierList().hasExplicitModifier("public");
        if (bol) {
            return JvmModifier.PUBLIC.name().toLowerCase();
        }
        bol = psiField.getModifierList().hasExplicitModifier("protected");
        if (bol) {
            return JvmModifier.PROTECTED.name().toLowerCase();
        }
        return JvmModifier.PRIVATE.name().toLowerCase();
    }

    public static String getDocComment(PsiField psiField) {
        String doc = "";
        String modifyTest = Objects.requireNonNull(psiField.getModifierList()).getText();
        int n = KmpUtil.search("@FieldDoc",modifyTest);
        if (n<0) {
            n = 0;
        }
        modifyTest = modifyTest.substring(n);
        // 从内容上截取路径数组
        Pattern pattern = Pattern.compile("(?<=\\()[^\\)]+");
        Matcher matcher = pattern.matcher(modifyTest);
        if(matcher.find()){
            modifyTest = matcher.group(0);
        }
        if (modifyTest.length()>0) {
            modifyTest = modifyTest.substring(1,modifyTest.length()-1);
        }
        return modifyTest;
    }

    /**
     * 判断一个类是JAVA类型还是用户自定义类型
     * @param clz
     * @return
     */
    public static boolean isJavaClass(Class<?> clz) {
        return clz != null && clz.getClassLoader() == null;
    }



}
