package com.hiccup.pxlogout;

import com.hiccup.json.JsonObject;
import com.intellij.ui.JBColor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeoutException;

public class PxLogOutDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JPanel Name;
    private JTextField nameField;
    private JPanel Passward;
    private JPasswordField passwordField;
    private JPanel Service;
    private JComboBox ServiceComboBox;
    private JPanel Version;
    private JLabel errorHint;
    private JRadioButton oldVersionRadio;
    private JRadioButton newVersionRadio;
    /**
     * 是否是新版本注销
     */
    private boolean newVersion;

    PxLogOutDialog() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(e -> onOK());

        buttonCancel.addActionListener(e -> onCancel());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        String serviceAddress = (String) ServiceComboBox.getSelectedItem();
        String name = nameField.getText();
        if (name.isEmpty()){
            errorHint.setText("用户名不能为空");
            return;
        }
        String password = String.valueOf(passwordField.getPassword());
        if (password.isEmpty()){
            errorHint.setText("密码不能为空");
            return;
        }
        oldVersionRadio.addItemListener(e -> newVersion = false);
        newVersionRadio.addItemListener(e -> newVersion = true);

        Color rightColor = new JBColor(new Color(0, 255, 0), new Color(0, 150, 0));
        Color errorColor = new JBColor(new Color(255, 0, 0), new Color(150, 0, 0));
        try {
            if (newVersion){
                String json = LogOutSocket.doLogOut(serviceAddress, name, password);
                JsonObject jsonObject = JsonObject.parse(json);
                if (jsonObject == null){
                    errorHint.setForeground(errorColor);
                    errorHint.setText(json);
                }
                boolean success = jsonObject.getBooleanValue("success");
                String code = jsonObject.getString("code");
                String errorCode = jsonObject.getString("errorCode");
                if (success || (code.equals("200") && errorCode.equals("0"))){
                    errorHint.setForeground(rightColor);
                    errorHint.setText("注销成功");
                } else {
                    errorHint.setForeground(errorColor);
                    String errMsg = jsonObject.getString("errorMsg");
                    errorHint.setText(errMsg);
                }
            } else {
                JsonObject argObject = new JsonObject();
                argObject.put("server", serviceAddress);
                argObject.put("name", name);
                argObject.put("password", password);
                argObject.put("version", "2.30.02");
                String json = LogOutService.getURLContent(argObject.toString());
                JsonObject jsonObject = JsonObject.parse(json);
                boolean success = jsonObject.getBooleanValue("success");
                if (success){
                    errorHint.setForeground(rightColor);
                    errorHint.setText("注销成功");
                } else {
                    errorHint.setForeground(errorColor);
                    String errMsg = jsonObject.getString("errorMsg");
                    int errorIndex = errMsg.indexOf("errorMsg");
                    errMsg = errMsg.substring(errorIndex + 9);
                    errorHint.setText(errMsg);
                }
            }
        } catch (SocketTimeoutException timeOutException){
            errorHint.setText("链接超时！");
        } catch (Exception e) {
            errorHint.setText(e.getMessage());
        }


    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    public static void main(String[] args) {
        PxLogOutDialog dialog = new PxLogOutDialog();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}
