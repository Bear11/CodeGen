package com.zcc.codergen.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.JavaProjectRootsUtil;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.compiled.ClsFileImpl;
import com.intellij.psi.impl.source.PsiJavaFileImpl;
import com.intellij.refactoring.PackageWrapper;
import com.intellij.refactoring.move.moveClassesOrPackages.MoveClassesOrPackagesUtil;
import com.zcc.codergen.CreateFileAction;
import com.zcc.codergen.ui.CodeGenForm;
import com.zcc.codergen.util.*;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CodeGenAction extends AnAction implements DumbAware {

    private static final Logger log = Logger.getInstance(CodeGenAction.class);

    // 全限定类名
    public static String targetPath = "";
    // 前缀
    public static String prefix = "";
    // 后缀
    public static String suffix = "";

    public static boolean cancelStatus = false;

    public CodeGenAction() {
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        Project project = anActionEvent.getProject();
        if (project == null) {
            return;
        }
        // 开启窗体
        try{
            CodeGenForm dialog = new CodeGenForm();
            dialog.pack();
            JPanel rootPane = dialog.getMainPane();
            dialog.setLocationRelativeTo(rootPane);
            dialog.setSize(600,240);
            dialog.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
            log.info("窗口打开失败");
            Messages.showMessageDialog(project, "Window open failed", "Generate Failed", null);
            return;
        }
        if (cancelStatus) { // 取消
            return;
        }
        // 检查用户输入是否非法
        if (!check(anActionEvent)) {
            return;
        }
        // 目标包名
        String pkgName = targetPath;


        // 根据类的全限定名查询PsiClass，下面这个方法是查询Project域
        PsiFile psiFile = anActionEvent.getData(LangDataKeys.PSI_FILE);
        if (psiFile == null) {
            Messages.showMessageDialog(project, "No Classes found", "Generate Failed", null);
            return;
        }
        PsiJavaFile psiJavaFile = (PsiJavaFile)psiFile;
        PsiClass psiClass = psiJavaFile.getClasses()[0];
        // 目标类名
        String targetClassName = "";
        if (StringUtil.isNotEmpty(prefix)||StringUtil.isNotEmpty(suffix)) {
            targetClassName = prefix+psiClass.getName()+suffix;
        } else {
            Messages.showMessageDialog(project, "Please enter either prefix or suffix for the target class", "Generate Failed", null);
            return;
        }

        String qualifiedName = psiClass.getQualifiedName();
        if (StringUtils.isEmpty(qualifiedName)) {
            Messages.showMessageDialog(project, "Cant't find selected file.Please try again.", "Generate Failed", null);
            return;
        }

        try {
            ClassLoader classLoader = this.getClass().getClassLoader();
            VirtualFile sourceRoot = findSourceRoot(qualifiedName, project, psiClass.getContainingFile());
            String sourcePath = "";
            if (sourceRoot != null) {
                 sourcePath = sourceRoot.getPath();
            }
            //AutoGenerator.loadClazz(qualifiedName, sourcePath);
            Class cl = Class.forName(qualifiedName);
            AutoGenerator.generator(pkgName, targetClassName, prefix, suffix, cl);
        } catch (ClassNotFoundException e) {
            Messages.showMessageDialog(project, e.getMessage(), "Generate Failed", null);
            e.printStackTrace();
            return;
        }
        Messages.showMessageDialog("create target class success", "Generate Success", null);
    }

    /**
     * 检验用户输入是否合法
     * @param anActionEvent
     * @return
     */
    private boolean check(AnActionEvent anActionEvent) {
        Project project = anActionEvent.getProject();
        if (StringUtil.isEmpty(targetPath)) {
            Messages.showMessageDialog(project, "Please enter the class path.", "Generate Failed", null);
            return false;
        }
        if (StringUtil.isEmpty(prefix) && StringUtil.isEmpty(suffix)) {
            Messages.showMessageDialog(project, "Please enter either prefix or suffix for the target class", "Generate Failed", null);
            return false;
        }
        PsiFile psiFile = anActionEvent.getData(LangDataKeys.PSI_FILE);
        if (psiFile == null) {
            Messages.showMessageDialog(project, "Please choose a target package.", "Generate Failed", null);
            return false;
        }
        return true;
    }
    /**
     * allow user to select the generated code source root
     * @param packageName
     * @param project
     * @param psiFile 修改部分
     * @return
     */
    private VirtualFile findSourceRoot(String packageName, Project project, PsiFile psiFile) {
        //String packageName = classEntry.getPackageName();
        final PackageWrapper targetPackage = new PackageWrapper(PsiManager.getInstance(project), packageName);
        List<VirtualFile> suitableRoots = JavaProjectRootsUtil.getSuitableDestinationSourceRoots(project);
        if (suitableRoots.size() > 1) {
            return MoveClassesOrPackagesUtil.chooseSourceRoot(targetPackage, suitableRoots,
                    psiFile.getContainingFile().getContainingDirectory());

        } else if (suitableRoots.size() == 1) {
            return suitableRoots.get(0);
        }
        return null;
    }
}
