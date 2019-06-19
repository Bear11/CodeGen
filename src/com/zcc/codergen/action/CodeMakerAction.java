package com.zcc.codergen.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.JavaProjectRootsUtil;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.refactoring.PackageWrapper;
import com.intellij.refactoring.move.moveClassesOrPackages.MoveClassesOrPackagesUtil;
import com.zcc.codergen.CodeGen;
import com.zcc.codergen.CodeGenSettings;
import com.zcc.codergen.CreateFileAction;
import com.zcc.codergen.util.*;
import org.apache.commons.lang.time.DateFormatUtils;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CodeMakerAction extends AnAction implements DumbAware {

    private static final Logger log = Logger.getInstance(CodeMakerAction.class);
    private CodeGenSettings settings;

    private String templateKey;

    public CodeMakerAction() {
    }

    CodeMakerAction(String templateKey) {
        this.settings = ServiceManager.getService(CodeGenSettings.class);
        this.templateKey = templateKey;
        getTemplatePresentation().setDescription("description");
        getTemplatePresentation().setText(templateKey, false);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        Project project = anActionEvent.getProject();
        if (project == null) {
            return;
        }
        // 获取数据上下文
        //DataContext dataContext = anActionEvent.getDataContext();

        // 获取到数据上下文后，通过CommonDataKeys对象可以获得该File的所有信息
        //PsiFile psiFile = CommonDataKeys.PSI_FILE.getData(dataContext);
        // 操作的文件路径
        //VirtualFile virtualFile = CommonDataKeys.VIRTUAL_FILE.getData(dataContext);
        //String ss = virtualFile.getName();

        // 项目路径
        String basePath = project.getBasePath();

        String url = anActionEvent.getPlace();
        // 获得的全限定类名
        String classQualifiedName = "com.zcc.entry.People";

        // 实际短类名
        //String className = getRealClassName(classQualifiedName);

        // 根据类的全限定名查询PsiClass，下面这个方法是查询Project域
        PsiClass psiClass = JavaPsiFacade.getInstance(project).findClass(classQualifiedName, GlobalSearchScope.projectScope(project));


        if (psiClass == null) {
            Messages.showMessageDialog(project, "No Classes found", "Generate Failed", null);
            return;
        }

        // 获取Java类所在的Package
        PsiJavaFile javaFile = (PsiJavaFile) psiClass.getContainingFile();
        PsiPackage pkg = JavaPsiFacade.getInstance(project).findPackage(javaFile.getPackageName());

        // PsiPackage psiPackage = JavaPsiFacade.getInstance(project).findPackage(classQualifiedName);
        String pakagename = "com.zcc.entry";
        String suffix = "Dto";
        ClassEntry currentClass = ClassEntry.create(psiClass, pakagename, suffix);

        CodeTemplate codeTemplate = settings.getCodeTemplate(templateKey);
        try {
            Map<String, Object> map = new HashMap<>();
            //map.put("class" , selectClasses.get(i));
            Date now = new Date();
            map.put("class", currentClass);
            map.put("YEAR", DateFormatUtils.format(now, "yyyy"));
            map.put("TIME", DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
            map.put("USER", System.getProperty("user.name"));
            String className = VelocityUtil.evaluate(codeTemplate.getClassNameVm(), map);
            map.put("ClassName", className);

            String content = VelocityUtil.evaluate(codeTemplate.getCodeTemplate(), map);
            //ClassEntry currentClass = selectClasses.get(0);
            VirtualFile sourceRoot = findSourceRoot(currentClass, project, psiClass.getContainingFile());

            if (sourceRoot != null) {
                String sourcePath = sourceRoot.getPath() + "/" + currentClass.getPackageName().replace(".", "/");
                String targetPath = CodeGenUtil.generateClassPath(sourcePath, className, "java");


                //VelocityInfoOp.generatorCode("model.vm", map, sourcePath + table.getPackagePath() + "/model", table.getClassName() + ".java");

                VirtualFileManager manager = VirtualFileManager.getInstance();
                VirtualFile virtualFile = manager
                        .refreshAndFindFileByUrl(VfsUtil.pathToUrl(targetPath));

                if (virtualFile == null || !virtualFile.exists()) {
                    // async write action
                    ApplicationManager.getApplication().runWriteAction(
                            new CreateFileAction(targetPath, content, codeTemplate.getFileEncoding(), anActionEvent.getDataContext()));
                }
            }






            /*String content = VelocityUtil.evaluate(codeTemplate.getCodeTemplate(), map);
            //ClassEntry currentClass = selectClasses.get(0);
            VirtualFile sourceRoot = findSourceRoot(currentClass, project, psiClass.getContainingFile());

            if (sourceRoot != null) {
                String sourcePath = sourceRoot.getPath() + "/" + currentClass.getPackageName().replace(".", "/");
                String targetPath = CodeGenUtil.generateClassPath(sourcePath, className, "java");

                VirtualFileManager manager = VirtualFileManager.getInstance();
                VirtualFile virtualFile = manager
                        .refreshAndFindFileByUrl(VfsUtil.pathToUrl(targetPath));

                if (virtualFile == null || !virtualFile.exists()) {
                    // async write action
                    ApplicationManager.getApplication().runWriteAction(
                            new CreateFileAction(targetPath, content, codeTemplate.getFileEncoding(), anActionEvent.getDataContext()));
                }
            }*/

        } catch (Exception e) {
            Messages.showMessageDialog(project, e.getMessage(), "Generate Failed", null);
        }
        //List<ClassEntry> selectClasses = getClasses(project, codeTemplate.getClassNumber(), psiClass);

//        if (selectClasses.size() < 1) {
//            Messages.showMessageDialog(project, "No Classes found", "Generate Failed", null);
//            return;
//        }

        // 获取Java类所在的Package
       // PsiPackage psiPackage = JavaPsiFacade.getInstance(project).findPackage(classQualifiedName);

        // 获取该包下的具体类
       // PsiClass[] psiClasses = psiPackage.getClasses(GlobalSearchScope.projectScope(project));

        // 通过获取到PsiElementFactory来创建相应的Element，包括字段，方法，注解，类，内部类等等
//        PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(project);
//        // 创建类
//        PsiClass aClass = elementFactory.createClass(className);

        // 通过给定名称（不包含具体路径）搜索对应文件, 传入3个参数 Project, FileName, GlobalSearchScope;
        // GlobalSearchScope中有Project域，Moudule域，File域等等
//        PsiFile[] psiFiles = FilenameIndex.getFilesByName(project, "People", GlobalSearchScope.projectScope(project));
//
//        if (psiFiles == null || psiFiles.length==0) {
//            Messages.showMessageDialog(project, "找不到jar包该类文件", "Generate Failed", null);
//            return ;
//        }
//        // 查找的类
//        PsiFile choosePisFile = psiFiles[0];




//        String filePath = project.getBasePath() + File.separator + "src" + File.separator + "com" + File.separator + "zcc" + File.separator + "entry" + File.separator;
//
//        VirtualFile virtualFi = LocalFileSystem.getInstance().findFileByPath(filePath);
//        if(virtualFile == null) return ;
//
//        // 获取Psi文件
//        PsiFile psiFi = PsiManager.getInstance(project).findFile(virtualFile);
//
//        //PsiClass psiChooseClass = psiFile.
//        PsiElement[] psiElements = psiFile.getChildren();

    }

    /**
     * 获取非限定类名
     * @param classQualifiedName
     * @return
     */
    protected String getRealClassName(String classQualifiedName){
        String[] str = classQualifiedName.split("[.]");
        // 获得类名
        String className = "";
        if (str!=null && str.length>0) {
            return str[str.length-1];
        } else {
            return null;
        }
    }

    /**
     * allow user to select the generated code source root
     * @param classEntry
     * @param project
     * @param psiFile 修改部分
     * @return
     */
    private VirtualFile findSourceRoot(ClassEntry classEntry, Project project, PsiFile psiFile) {
        String packageName = classEntry.getPackageName();
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
