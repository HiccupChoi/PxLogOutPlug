package com.hiccup.pxlogout;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.vcs.log.ui.frame.MainFrame;

import java.awt.*;

/**
 * @Author: Hiccup
 * @Date: 2019/9/23 20:42
 */
public class LogoutAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        PxLogOutDialog dialog = new PxLogOutDialog();

        //定义工具包
        Toolkit kit = Toolkit.getDefaultToolkit();
        //获取屏幕的尺寸
        Dimension screenSize = kit.getScreenSize();
        //获取屏幕的宽
        int screenWidth = screenSize.width;
        //获取屏幕的高
        int screenHeight = screenSize.height;
        //设置窗口居中显示
        dialog.setLocation(screenWidth/2 - 230, screenHeight/2- 124);
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }


}
