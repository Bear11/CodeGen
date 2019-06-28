package com.zcc.codergen.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
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
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.refactoring.PackageWrapper;
import com.intellij.refactoring.move.moveClassesOrPackages.MoveClassesOrPackagesUtil;
import com.zcc.codergen.CreateFileAction;
import com.zcc.codergen.ui.CodeGenForm;
import com.zcc.codergen.util.ClassEntry;
import com.zcc.codergen.util.CodeGenUtil;
import com.zcc.codergen.util.CodeTemplate;
import com.zcc.codergen.util.VelocityUtil;
import org.apache.commons.lang.time.DateFormatUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CodeMakerAction extends AnAction implements DumbAware {

    private static final Logger log = Logger.getInstance(CodeMakerAction.class);
    // 全限定类名
    public static String calssPath = "";
    // 前缀
    public static String prefix = "";
    // 后缀
    public static String suffix = "";
    public CodeMakerAction() {
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
            //dialog.setSize(600,250);
            JPanel rootPane = dialog.getMainPane();
            dialog.setLocationRelativeTo(rootPane);
            dialog.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
            log.info("窗口打开失败");
            Messages.showMessageDialog(project, "Window open failed", "Generate Failed", null);
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
        String classQualifiedName = "com.zcc.entry.Student";
        // 实际短类名
        String classSourceName = getRealClassName(classQualifiedName);
        // 根据类的全限定名查询PsiClass，下面这个方法是查询Project域
        PsiClass psiClass = JavaPsiFacade.getInstance(project).findClass(classQualifiedName, GlobalSearchScope.projectScope(project));

        if (psiClass == null) {
            Messages.showMessageDialog(project, "No Classes found", "Generate Failed", null);
            return;
        }
        // 获取Java类所在的Package
        PsiJavaFile javaFile = (PsiJavaFile) psiClass.getContainingFile();
        PsiPackage psiPackage = JavaPsiFacade.getInstance(project).findPackage(javaFile.getPackageName());
        // TODO 目标类生成文件夹对应包名，后面需修改
        String pakagename = "com.zcc.entry";
        String pakagePath = pakagename.replace(".","/");
        // 目标类名
        String targetClassName = "";
        if (StringUtil.isNotEmpty(prefix)||StringUtil.isNotEmpty(suffix)) {
            targetClassName = prefix+classSourceName+suffix;
        } else {
            Messages.showMessageDialog(project, "Please enter either prefix or suffix for the target class", "Generate Failed", null);
            return;
        }
        // 生成目标类Entry
        ClassEntry currentClass = ClassEntry.create(psiClass, pakagename, suffix);
        VirtualFile sourceRoot = findSourceRoot(currentClass, project, psiClass.getContainingFile());
        CodeTemplate codeTemplate = null;
        try {
            // 从整个class文件夹去找
            InputStream in = this.getClass().getResourceAsStream( "../template/" + "Model.vm");
            String velocityTemplate = FileUtil.loadTextAndClose(in);
            // 创建模板
            codeTemplate = createCodeTemplate(velocityTemplate,"Model.vm",
                    targetClassName, 1, CodeTemplate.DEFAULT_ENCODING);
        } catch (IOException e) {
            log.info("创建模板失败");
            Messages.showMessageDialog(project, "create template failed", "Generate Failed", null);
            e.printStackTrace();
        }

        try {
            Map<String, Object> map = new HashMap<>();
            Date now = new Date();
            map.put("class", currentClass);
            map.put("YEAR", DateFormatUtils.format(now, "yyyy"));
            map.put("TIME", DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
            map.put("USER", System.getProperty("user.name"));
            map.put("ClassName", targetClassName);
            String content = "";
            if (codeTemplate!=null && StringUtil.isNotEmpty(codeTemplate.getCodeTemplate())) {
                content = VelocityUtil.evaluate(codeTemplate.getCodeTemplate(), map);
            }

            if (sourceRoot != null) {
                String sourcePath = sourceRoot.getPath() + "/" + currentClass.getPackageName().replace(".", "/");
                String targetPath = CodeGenUtil.generateClassPath(sourcePath, targetClassName, "java");

                VirtualFileManager manager = VirtualFileManager.getInstance();
                VirtualFile virtualFile = manager
                        .refreshAndFindFileByUrl(VfsUtil.pathToUrl(targetPath));

                if (virtualFile == null || !virtualFile.exists()) {
                    // async write action
                    ApplicationManager.getApplication().runWriteAction(
                            new CreateFileAction(targetPath, content, codeTemplate.getFileEncoding(), anActionEvent.getDataContext()));
                }
            }
        } catch (Exception e) {
            Messages.showMessageDialog(project, e.getMessage(), "Generate Failed", null);
            return;
        }
        Messages.showMessageDialog("create target class success", "Generate Success", null);
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

    /**
     * 创建模板方法
     * @param velocityTemplate
     * @param sourceTemplateName
     * @param classNameVm
     * @param classNumber
     * @param fileEncoding
     * @return
     * @throws IOException
     */
    private CodeTemplate createCodeTemplate(String velocityTemplate,String sourceTemplateName, String classNameVm, int classNumber, String fileEncoding) throws IOException {
        return new CodeTemplate(sourceTemplateName,
                classNameVm, velocityTemplate, classNumber, fileEncoding);
    }
}
