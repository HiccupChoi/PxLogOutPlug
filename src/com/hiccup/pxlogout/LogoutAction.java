package com.hiccup.pxlogout;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiFile;

/**
 * @Author: Hiccup
 * @Date: 2019/9/23 20:42
 */
public class LogoutAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getData(PlatformDataKeys.PROJECT);
        DialogBuilder dialogBuilder = new DialogBuilder(project);
        Messages.showInputDialog(
                project,
                "What is your name?",
                "Input your name",
                Messages.getQuestionIcon());
    }

    /**
     * 显示提示对话框
     *
     * @param file
     * @param prefix
     * @param project
     */
    private void showHintDialog(PsiFile file, String prefix, Project project) {

    }

}
