package com.hiccup.pxlogout;




import com.hiccup.json.JsonObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

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
    private JTextField versionField;
    private JLabel errorhint;

    public PxLogOutDialog() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);


        versionField.setDocument(new NumberTextField());

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
        contentPane.registerKeyboardAction(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        String serviceAddress = (String) ServiceComboBox.getSelectedItem();
        String name = nameField.getText();
        if (name.isEmpty()){
            errorhint.setText("用户名不能为空");
            return;
        }
        String password = String.valueOf(passwordField.getPassword());
        if (password.isEmpty()){
            errorhint.setText("密码不能为空");
            return;
        }
        String version = versionField.getText();
        if (version.isEmpty()){
            errorhint.setText("版本号不能为空");
            return;
        }
        System.out.println(serviceAddress + "  " + name + "  " + password + "  " +version);

        LogOutService logOutService = new LogOutService(serviceAddress, name, password, version);
        try {
            if (version.equals("2.41")){
                String json = LogOutSocket.doLogOut(serviceAddress, name, password, version);
                JsonObject jsonObject = JsonObject.parse(json);
                boolean success = jsonObject.getBooleanValue("success");
                String code = jsonObject.getString("code");
                String errorCode = jsonObject.getString("errorCode");
                if (success || (code.equals("200") && errorCode.equals("0"))){
                    errorhint.setForeground(new Color(0, 178, 0));
                    errorhint.setText("注销成功");
                } else {
                    errorhint.setForeground(new Color(187, 0, 10));
                    String errMsg = jsonObject.getString("errorMsg");
                    errorhint.setText(errMsg);
                }
            } else {
                JsonObject argObject = new JsonObject();
                argObject.put("server", serviceAddress);
                argObject.put("name", name);
                argObject.put("password", password);
                argObject.put("version", version);
                String json = logOutService.getURLContent(argObject.toString());
                JsonObject jsonObject = JsonObject.parse(json);
                boolean success = jsonObject.getBooleanValue("success");
                if (success){
                    errorhint.setForeground(new Color(0, 178, 0));
                    errorhint.setText("注销成功");
                } else {
                    errorhint.setForeground(new Color(187, 0, 10));
                    String errMsg = jsonObject.getString("errorMsg");
                    int errorIndex = errMsg.indexOf("errorMsg");
                    errMsg = errMsg.substring(errorIndex + 9);
                    errorhint.setText(errMsg);
                }
            }
        } catch (Exception e) {
            errorhint.setText(e.getMessage());
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
